/*
 *  Copyright (c) 2007 by Damien Di Fede <ddf@compartmental.net>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package ddf.minim;

import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.tritonus.share.sampled.AudioUtils;
import org.tritonus.share.sampled.FloatSampleBuffer;

final class MAudioFile extends Thread
implements AudioRecording
{ 
	private SignalSplitter splitter;
	private EffectsChain effects;

	// file reading stuff
	private long lengthInMillis;
	private boolean play;
	private boolean loop;
	private int numLoops;
	private AudioInputStream ais;
	private byte[] rawBytes;

	// line writing stuff
	private AudioFormat format;
	private SourceDataLine line;
	private FloatSampleBuffer buffer;
	private boolean finished; //Sources are invalid now
	private boolean STEREO_OUTPUT = true; //Bypass stereoline for faster samples to listeners
	private boolean atEOF; //Symbolizes whether the "cursor" is at the end of the file.

	MAudioFile(AudioInputStream stream, 
			SourceDataLine sdl, int bufferSize)
			{
		super();
		format = sdl.getFormat();
		splitter = new SignalSplitter(format, bufferSize);
		effects = new EffectsChain();

		buffer = new FloatSampleBuffer(format.getChannels(), 
				bufferSize,
				format.getSampleRate());
		Minim.debug("FloatSampleBuffer has " + buffer.getSampleCount() + " samples.");
		finished = false;
		line = sdl;

		ais = stream;
		lengthInMillis = AudioUtils.frames2Millis(ais.getFrameLength(), format);
		play = loop = false;
		numLoops = 0;
		rawBytes = new byte[buffer.getByteArrayBufferSize(format)];  
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
			//int size = buffer.getByteArrayBufferSize(ais.getFormat());
			rawBytes = new byte[rawBytes.length];

			atEOF = false;
			try
			{
				// read bytes if we're playing
				if (play)
				{
					int bytesRead = ais.read(rawBytes, 0, rawBytes.length);
					// -1 means end of file
					if ( bytesRead == -1 )
					{
						if ( loop )
						{
							// reset the stream, start playing
							if (numLoops == Clip.LOOP_CONTINUOUSLY)
							{
								rewind();
							}
							// reset the stream, start playing, decrement loop count
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
						}  else {
							atEOF = true;
						}
					} else {
						atEOF = false;
					}
				}
			}
			catch (IOException e)
			{
				Minim.error("AudioPlayer: error reading from the file - " + e.getMessage());
			}

			// convert the bytes to floating point samples
			int frameCount = rawBytes.length / format.getFrameSize();
			buffer.setSamplesFromBytes(rawBytes, 0, format, 0, frameCount);

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
				splitter.samples(sampL, sampR);
			}
			// finally convert them back to bytes and write to our line
			byte[] bytes = buffer.convertToByteArray(format);
			if (STEREO_OUTPUT)
			line.write(bytes, 0, bytes.length);
		}
		line.drain();
		line.stop();
		line.close();
		line = null;
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
		try
		{
			ais.reset();
		}
		catch (IOException e)
		{
			Minim.error("Rewinding is not supported with this file type " 
					+ "(probably mp3), no action taken.");
		}
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

	public int length()
	{
		return (int)lengthInMillis;
	}

	public int position()
	{
		try
		{
			int availBytes = ais.available();
			int availMillis = (int)AudioUtils.bytes2Millis(availBytes, format);
			int pos = length() - availMillis;
			return pos;
		}
		catch (IOException e)
		{
			Minim.error("Couldn't calculate position: " + e.getMessage());
		}
		return -1;
	}

	public void cue(int millis)
	{
		if ( millis < 0 ) 
		{
			millis = 0;
			rewind();
			return;
		}
		if ( millis > length() ) millis = length();
		if ( millis > position() )
		{
			skip( millis - position() );
		}
		else
		{
			rewind();
			int bytes = (int) AudioUtils.millis2BytesFrameAligned(millis, format);
			long bytesRead = 0;
			try
			{
				bytesRead = ais.skip(bytes);
			}
			catch (IOException e)
			{
				Minim.error("AudioPlayer: Error setting cue point: " + e.getMessage());
			}
			Minim.debug("Total actually skipped was " + bytesRead + ", which is "
					+ AudioUtils.bytes2Millis(bytesRead, ais.getFormat())
					+ " milliseconds.");
		}
	}


	public void skip(int millis)
	{
		if ( millis > 0 )
		{
			// if it puts us past the end of the file, only skip what's left
			if ( position() + millis > length() )
			{
				millis = length() - position();
			}
			long bytes = AudioUtils.millis2BytesFrameAligned(millis, ais.getFormat());
			long read = 0;
			int currPos = position();
			try
			{
				read = ais.skip(bytes);
			}
			catch (IOException e)
			{
				Minim.debug("AudioPlayer: Error skipping: " + e.getMessage());
				cue(currPos);
			}
			Minim.debug("Total actually skipped was " + read + ", which is "
					+ AudioUtils.bytes2Millis(read, ais.getFormat())
					+ " milliseconds.");
		}
		else if ( millis < 0 )
		{
			// to skip backwards we need to rewind
			// and then cue to the new position
			// remember that millis is negative, so we add
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

	public void addListener(AudioListener listener)
	{
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
		return null;
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
