package ddf.minim;

import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javazoom.spi.vorbis.sampled.convert.DecodedVorbisAudioInputStream;

import org.tritonus.share.sampled.FloatSampleBuffer;

import processing.core.PApplet;

class VorbisOggFile extends Thread
implements AudioRecording
{
	private SignalSplitter splitter;
	private EffectsChain effects;

	// file reading stuff
	private String fileName;
	private Map properties;
	private long bytesRead;
	private long startedWritingBytes = -1;
	private long lengthInMillis;
	private boolean play;
	private boolean loop;
	private int numLoops;
	private DecodedVorbisAudioInputStream ais;
	private byte[] rawBytes,rawBytes2,byteBufferConverted;

	// line writing stuff
	private AudioFormat format;
	private SourceDataLine line;
	private FloatSampleBuffer buffer;
	private boolean finished; //Sources are invalid now
	private boolean STEREO_OUTPUT = true; //Bypass stereoline for faster samples to listeners
	private boolean atEOF; //Symbolizes whether the "cursor" is at the end of the file.

	VorbisOggFile(String fn, Map props, AudioInputStream stream, 
			SourceDataLine sdl, int bufferSize)
			{
		format = sdl.getFormat();
		line = sdl;
		buffer = new FloatSampleBuffer(format.getChannels(), 
				bufferSize,
				format.getSampleRate());
		Minim.debug("FloatSampleBuffer has " 
				+ buffer.getSampleCount() 
				+ " samples.");
		finished = false;

		fileName = fn;
		properties = props;
		play = loop = false;
		numLoops = 0;
		ais = (DecodedVorbisAudioInputStream)stream;
		rawBytes = new byte[buffer.getByteArrayBufferSize(format)];
		rawBytes2 = new byte[rawBytes.length];
		byteBufferConverted = new byte[rawBytes.length];
		
		splitter = new SignalSplitter(format, bufferSize);
		effects = new EffectsChain();
			}

	public void run()
	{
		try
		{
			line.open(format, bufferSize() * format.getFrameSize() * 4);
		}
		catch (LineUnavailableException e)
		{
			Minim.error("Error opening SourceDataLine: " + e.getMessage());
		}
		line.start();
		while ( !finished )
		{
			byte[] temp = rawBytes2;
			rawBytes2 = rawBytes;
			rawBytes = temp;
			atEOF = false;
			try
			{       
				// read bytes if we're playing
				if (play)
				{
					int toRead = rawBytes.length;
					int bytesRead = 0;
					while ( bytesRead < toRead )
					{   
						int actualRead = ais.read(rawBytes, bytesRead, toRead - bytesRead);
						// -1 means end of file
						if ( actualRead == -1 )
						{
							if ( loop )
							{
								// reset the stream
								if (numLoops == Clip.LOOP_CONTINUOUSLY)
								{
									rewind();
								}
								// reset the stream, decrement loop count
								else if (numLoops > 0)
								{
									rewind();
									numLoops--;
								}
								// otherwise just stop playing
								else
								{
									loop = false;
									play = false;
									atEOF = true;
								}
							} else {
								atEOF = true;
							}
						} // if bytesRead == -1
						else
						{
							atEOF = false;
							bytesRead += actualRead;
						}
					}// while bytesRead < toRead
				} // if play
			}
			catch (IOException e)
			{
				Minim.error("AudioPlayer: error reading from the file - " + e.getMessage());
			}

			// convert the bytes to floating point samples
			int frameCount = rawBytes.length / format.getFrameSize();
			buffer.setSamplesFromBytes(rawBytes, 0, format, 0, frameCount);
			//Rawbytes now free!
			
			// process the samples and broadcast them to our listeners
			if ( type() == Minim.MONO )
			{
				float[] samp = buffer.getChannel(0);
				if ( effects.hasEnabled() )
				{
					effects.process(samp);
				}
				splitter.samples(samp);
			}
			else
			{
				float [] sampL = buffer.getChannel(0);
				float [] sampR = buffer.getChannel(1);
				if ( effects.hasEnabled() )
				{
					effects.process(sampL, sampR);
				}
				splitter.samples(sampL,sampR);
			}
			// finally convert them back to bytes and write to our line
			int wrote = buffer.convertToByteArray(byteBufferConverted, 0, format);
			if (STEREO_OUTPUT)
				line.write(byteBufferConverted, 0, byteBufferConverted.length);
		}
		line.drain();
		line.stop();
		line.close();
		line = null;
		try
		{
			ais.close();
		}
		catch (IOException e)
		{
			Minim.error("Couldn't close the stream");
		}
		ais = null;
	}

	public void play()
	{
		play = true;
		loop = false;
	}

	public void play(int millis)
	{
		cue(millis);
		play();
	}

	public boolean isPlaying()
	{
		return play;
	}

	public void pause()
	{
		play = false;
	}

	public void rewind()
	{
		Minim.debug("Rewinding...");
		try
		{
			ais.close();
		}
		catch (IOException e)
		{
			Minim.error("Couldn't close the stream.");
		}
		AudioInputStream encIn = Minim.getAudioInputStream(fileName);
		// converts the stream to PCM audio from mp3 audio
		ais = (DecodedVorbisAudioInputStream)Minim.getAudioInputStream(format, encIn);
	}

	public void loop()
	{
		loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void loop(int n)
	{
		loop = true;
		numLoops = n;
		play = true;
	}

	/**
	 * May or may not be available until you start playing.
	 */
	public int length()
	{
		if ( properties != null && properties.containsKey("ogg.length.bytes") )
		{
			lengthInMillis = VobisBytes2Millis((Integer)properties.get("ogg.length.bytes"));
			System.out.println("GOT LENGTH: "+lengthInMillis);
		}
		else
		{
			lengthInMillis = -1;
		}
		return (int)lengthInMillis;
	}

	public int position()
	{
		Map prop = ais.properties();
		long getBytesRead = (Long)prop.get("ogg.position.byte");
		long getBytesLastUpdated = (Long)prop.get("ogg.position.byte.update");
		long getByteUpdateSize = (Long)prop.get("ogg.position.byte.updateSize");
		if (isPlaying()){
			int expectedSize = (int) VobisBytes2Millis(getByteUpdateSize);
			int diff = (int)((System.nanoTime()-getBytesLastUpdated)/1e6);
			diff = (int) (VobisBytes2Millis(getBytesRead)+PApplet.constrain(diff, 0, expectedSize));
			//System.out.println((int)(System.nanoTime()/1e6)+" "+diff);
			return diff;
		} else {
			return (int)VobisBytes2Millis(getBytesRead);
		}
	}
	public long VobisBytes2Millis(long bytes){
		long res = (long)(.5f*1000*bytes/(double)format.getSampleRate()*format.getChannels());
		return res;
	}
	public long VobisMillis2Bytes(long millis){
		long res = (long)(2*millis*(double)format.getSampleRate()/1000/format.getChannels());
		return res;
	}

	public void cue(int millis)
	{
		if ( millis < 0 )
		{
			rewind();
			return;
		}
		/**
		 * The length comes seemingly late; we should be able to safely seek before then. Hopefully~
		 */
		if ( millis > length() && length() >= 0) millis = length();
		if ( millis > position() )
		{
			Minim.debug("Skipping for cue to "+millis+" from " + position());
			skip(millis - position());
		}
		else
		{
			Minim.debug("Rewind and skip for cue.");
			rewind();
			skip(millis);
		}
	}

	public void skip(int millis)
	{
		if ( millis > 0 )
		{
			if (false){ //HACKED WAY:
				boolean playstate = play;
				play = false;
				long toSkip = position()-millis;
				try {
					ais.skip(-toSkip);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				play = playstate;
				return;
			}
			Minim.debug("Skipping forward by " + millis + " milliseconds.");
			// if it puts us past the end of the file, only skip what's left
			if ( position() + millis > length() && length()>=0 )
			{
				millis = length() - position();
			}
			int targetMs = millis+position();
			// don't want the io thread to read while we're skipping
			boolean playstate = play;
			play = false;
			long toSkip = VobisMillis2Bytes(millis);
			Minim.debug("Skipping " + millis + " millis " + toSkip+" bytes at a time.");
			byte[] skipBytes = new byte[(int)toSkip];
			int totalSkipped = 0;
			try
			{
				// it's only able to read about 2 seconds at a time
				// so we've got to loop until we've skipped the requested amount
				while (position()<targetMs)
				{
					totalSkipped += ais.read(skipBytes, 0, skipBytes.length);
				}
			}
			catch (IOException e)
			{
				Minim.error("Unable to skip due to read error: " + e.getMessage());
			}
			Minim.debug("Total actually skipped was " + totalSkipped + ", which is "
					+ VobisBytes2Millis(totalSkipped) + " milliseconds.");
			// restore the play state
			play = playstate;
		}
		else if ( millis < 0 )
		{
			Minim.debug("Skipping backwards by " + (-millis) + " milliseconds.");
			// to skip backwards we need to rewind
			// and then cue to the new position
			// remember that millis is negative, that's why we add
			if ( position() > 0 )
			{
				int pos = position() + millis;
				rewind();
				if ( pos > 0 )
				{
					cue(pos);
				}
			}
		}
	}

	public boolean isLooping()
	{
		return loop;
	}

	public int type()
	{
		return format.getChannels();
	}

	public void open()
	{
		start();
	}

	public void close()
	{
		finished = true;
	}

	public void addEffect(AudioEffect effect)
	{
		effects.add(effect);    
	}

	public void clearEffects()
	{
		effects.clear();    
	}

	public void disableEffect(int i)
	{
		effects.disable(i);
	}

	public void disableEffect(AudioEffect effect)
	{
		effects.disable(effect);    
	}

	public int effectCount()
	{
		return effects.size();
	}

	public void effects()
	{
		effects.enableAll();    
	}

	public boolean hasEffect(AudioEffect e)
	{
		return effects.contains(e);
	}

	public void enableEffect(int i)
	{
		effects.enable(i); 
	}

	public void enableEffect(AudioEffect effect)
	{
		effects.enable(effect);
	}

	public AudioEffect getEffect(int i)
	{
		return effects.get(i);
	}

	public boolean isEffected()
	{
		return effects.hasEnabled();
	}

	public boolean isEnabled(AudioEffect effect)
	{
		return effects.isEnabled(effect);
	}

	public void noEffects()
	{
		effects.disableAll();
	}

	public void removeEffect(AudioEffect effect)
	{
		effects.remove(effect);   
	}

	public AudioEffect removeEffect(int i)
	{
		return effects.remove(i);
	}

	private boolean hasListeners = false;
	public void addListener(AudioListener listener)
	{
		hasListeners = true;
		splitter.addListener(listener);    
	}

	public int bufferSize()
	{
		return splitter.bufferSize();
	}

	public AudioFormat getFormat()
	{
		return format;
	}

	public void removeListener(AudioListener listener)
	{
		splitter.removeListener(listener);
	}

	public DataLine getDataLine()
	{
		return line;
	}

	public float sampleRate()
	{
		return splitter.sampleRate();
	}

	public Map getProperties() {
		return properties;
	}

	public AudioInputStream getAudioInputStream() {
		return ais;
	}

	public void hearNot(boolean ByPassSpeakerSystem) {
		STEREO_OUTPUT = !ByPassSpeakerSystem;
	}
	public boolean isAtEndOfStream(){
		return atEOF;
	}
}
