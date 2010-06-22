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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.file.MpegAudioFormat;

import org.tritonus.share.sampled.AudioUtils;
import org.tritonus.share.sampled.FloatSampleBuffer;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import processing.core.PApplet;

/**
 * The <code>Minim</code> class is how you get what you want from JavaSound.
 * There are methods for obtaining objects for playing audio files:
 * {@link AudioSample}, {@link AudioSnippet}, and {@link AudioPlayer}. There
 * are methods for obtaining an {@link AudioRecorder}, which is how you record
 * audio to disk. There are methods for obtaining an {@link AudioInput}, which
 * is how you can monitor the computer's line-in or microphone, depending on
 * what the user has set as the record source. Finally there are methods for
 * obtaining an {@link AudioOutput}, which is how you can play audio generated
 * by your program. All of these classes are given {@link AudioStream AudioStreams} 
 * by <code>Minim</code>, which are <code>Thread</code>s that do the actual work 
 * of audio I/O. Because of this, you should always call the <code>close</code> 
 * method of an AudioXXX when you are finished with it.
 * <p>
 * <code>Minim</code> needs to know about your sketch so that it can load files 
 * from the sketches data directory. For this reason, before you do anything with
 * <code>Minim</code>, you must call {@link #start(PApplet) start}. 
 * 
 * @author Damien Di Fede
 */

public class Minim
{
	/** Specifies that you want a MONO AudioInput or AudioOutput */
	public static final int MONO = 1;
	/** Specifies that you want a STEREO AudioInput or AudioOutput */
	public static final int STEREO = 2;

	/** The .wav file format. */
	public static AudioFileFormat.Type WAV = AudioFileFormat.Type.WAVE;
	/** The .aiff file format. */
	public static AudioFileFormat.Type AIFF = AudioFileFormat.Type.AIFF;
	/** The .aifc file format. */
	public static AudioFileFormat.Type AIFC = AudioFileFormat.Type.AIFC;
	/** The .au file format. */
	public static AudioFileFormat.Type AU = AudioFileFormat.Type.AU;
	/** The .snd file format. */
	public static AudioFileFormat.Type SND = AudioFileFormat.Type.SND;

	private PApplet p;
	private static boolean DEBUG;

	public static int millis()
	{
		return (int)(System.nanoTime()/1e6);
	}

	/**
	 * Used internally to report error messages. These error messages will appear
	 * in the console area of the PDE if you are running a sketch from the PDE,
	 * otherwise they will appear in the Java Console.
	 * 
	 * @param s
	 *          the error message to report
	 */
	public static void error(String s)
	{
		PApplet.println("=== Minim Error ===");
		PApplet.println("=== " + s);
		PApplet.println();
	}

	/**
	 * Displays a debug message, but only if {@link #debugOn()} has been called. 
	 * The message will be displayed in the console area of the PDE, 
	 * if you are running your sketch from the PDE. 
	 * Otherwise, it will be displayed in the Java Console.
	 * 
	 * @param s
	 *          the message to display
	 * @see #debugOn()
	 */
	public static void debug(String s)
	{
		if (DEBUG)
		{
			String[] lines = s.split("\n");
			PApplet.println("=== Minim Debug ===");
			for (int i = 0; i < lines.length; i++)
				PApplet.println("=== " + lines[i]);
			PApplet.println();
		}
	}

	/**
	 * Turns on debug messages.
	 */
	public void debugOn()
	{
		DEBUG = true;
	}

	/**
	 * Turns off debug messages.
	 * 
	 */
	public void debugOff()
	{
		DEBUG = false;
	}

	/**
	 * Starts Minim.
	 * 
	 * It is necessary to call this so that Minim can properly open files.
	 * 
	 * @param pro
	 *          the sketch that is going to be using Minim
	 */
	public Minim(PApplet pro)
	{
		p = pro;
		DEBUG = true;
	}

	/**
	 * Stops Minim.
	 * 
	 * A call to this method should be placed inside of the stop() function of
	 * your sketch. All of the audio input and output is handled in separate
	 * threads, calling this method will cause all of those threads to finish
	 * executing. Not calling this method could result in errors.
	 * 
	 * @deprecated This does nothing now, be sure to call <code>close</code> on 
	 * anything you get from Minim.
	 * 
	 */
	public static void stop()
	{
	}

	/**
	 * Loads the requested file into an {@link AudioSample}.
	 * 
	 * @param filename
	 *          the file or URL that you want to load
	 * @return an <code>AudioSample</code> with a 1024 sample buffer
	 * @see #loadSample(String, int)
	 * @see AudioSample
	 */
	public AudioSample loadSample(String filename)
	{
		return loadSample(filename, 512);
	}

	/**
	 * Loads the requested file into an {@link AudioSample}.
	 * 
	 * @param filename
	 *          the file or URL that you want to load
	 * @param bufferSize
	 *          the sample buffer size you want
	 * @return an <code>AudioSample</code> with a sample buffer of the requested size
	 */
	public AudioSample loadSample(String filename, int bufferSize)
	{
		AudioSample s = null;
		AudioInputStream ais = getAudioInputStream(filename);
		if (ais != null)
		{
			AudioFormat format = ais.getFormat();
			FloatSampleBuffer samples = new FloatSampleBuffer();
			if ( format instanceof MpegAudioFormat )
			{
				AudioFormat baseFormat = format;
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
						baseFormat.getSampleRate(), 16, 
						baseFormat.getChannels(), 
						baseFormat.getChannels() * 2, 
						baseFormat.getSampleRate(), false);
				// converts the stream to PCM audio from mp3 audio
				ais = getAudioInputStream(format, ais);
				// get a map of properties so we can find out how long it is
				Map props = getPropertiesMpeg(filename);
				// there is a property called mp3.length.bytes, but that is 
				// the length in bytes of the mp3 file, which will of course 
				// be much shorter than the decoded version. so we use the 
				// duration of the file to figure out how many bytes the 
				// decoded file will be.
				long dur = ((Long)props.get("duration")).longValue();
				int toRead = (int)AudioUtils.millis2Bytes(dur/1000, format);
				int totalRead = 0;
				byte[] rawBytes = new byte[toRead];
				try
				{
					// we have to read in chunks because the decoded stream won't 
					// read more than about 2000 bytes at a time
					while ( totalRead < toRead )
					{
						int actualRead = ais.read(rawBytes, totalRead, toRead - totalRead);
						if ( actualRead < 1 ) break;
						totalRead += actualRead;
					}
					ais.close();
				}
				catch ( Exception ioe )
				{
					error("Minim.loadSample: Error loading file into memory: " 
							+ ioe.getMessage());
				}
				debug("Needed to read " + toRead + " actually read " + totalRead);
				samples.initFromByteArray(rawBytes, 0, totalRead, format);
			}
			else
			{
				try
				{
					byte[] bytes = new byte[ais.available()];
					ais.read(bytes, 0, bytes.length);
					ais.close();
					samples.initFromByteArray(bytes, 0, bytes.length, format);
				}
				catch ( IOException ioe )
				{
					Minim.error("Minim.loadSample: Error loading file into memory: " 
							+ ioe.getMessage());         
				}
			}
			SourceDataLine sdl = getSourceDataLine(format);
			if ( sdl != null )
			{
				MAudioSample ms = new MAudioSample(samples, sdl, bufferSize);
				s = new AudioSample(ms);
			}
			else
			{
				error("Minim.loadSample: Couldn't acquire a SourceDataLine.");
			}
		}
		return s;
	}

	/**
	 * Loads the requested file into an {@link AudioSnippet}
	 * 
	 * @param filename
	 *          the file or URL you want to load
	 * @return an <code>AudioSnippet</code> of the requested file or URL
	 */
	static public AudioSnippet loadSnippet(String filename)
	{
		AudioSnippet s = null;
		AudioInputStream ais = getAudioInputStream(filename);
		if (ais != null)
		{
			AudioFormat format = ais.getFormat();
			if ( format instanceof MpegAudioFormat )
			{
				AudioFormat baseFormat = format;
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
						baseFormat.getSampleRate(), 16, 
						baseFormat.getChannels(), 
						baseFormat.getChannels() * 2, 
						baseFormat.getSampleRate(), false);
				// converts the stream to PCM audio from mp3 audio
				ais = getAudioInputStream(format, ais);
			}
			Clip clip = null;
			DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
			if (AudioSystem.isLineSupported(info))
			{
				// Obtain and open the line.
				try
				{
					clip = (Clip) AudioSystem.getLine(info);
					clip.open(ais);
					s = new AudioSnippet(clip);
				}
				catch (Exception e)
				{
					error("Error obtaining clip: " + e.getMessage());
				}
			}
			else
			{
				error("File format not supported.");
			}
		}
		return s;
	}

	/**
	 * Loads the requested file into an {@link AudioPlayer} 
	 * with a buffer size of 1024 samples.
	 * 
	 * @param filename
	 *          the file or URL you want to load
	 * @return an <code>AudioPlayer</code> with a 1024 sample buffer
	 * 
	 * @see #loadFile(String, int)
	 */
	public AudioPlayer loadFile(String filename)
	{
		return loadFile(filename, 1024);
	}

	/**
	 * Loads the requested file into an {@link AudioPlayer} with 
	 * the request buffer size.
	 * 
	 * @param filename
	 *          the file or URL you want to load
	 * @param bufferSize
	 *          the sample buffer size you want
	 *          
	 * @return an <code>AudioPlayer</code> with a sample buffer of the requested size
	 */
	public AudioPlayer loadFile(String filename, int bufferSize)
	{
		AudioPlayer file = null;
		AudioInputStream ais = getAudioInputStream(filename); //MPEG!
		if (ais != null)
		{
			debug("File format is: " + ais.getFormat().toString());
			AudioFormat format = ais.getFormat();
			// special handling for mp3 files because 
			// they need to be converted to PCM
			if ( format instanceof MpegAudioFormat )
			{
				Map props = getPropertiesMpeg(filename);
				AudioFormat baseFormat = format;
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
						baseFormat.getSampleRate(), 16, 
						baseFormat.getChannels(), 
						baseFormat.getChannels() * 2, 
						baseFormat.getSampleRate(), false);
				// converts the stream to PCM audio from mp3 audio
				ais = getAudioInputStream(format, ais);
				//				source data line is for sending the file audio out to the speakers
				SourceDataLine line = getSourceDataLine(format);
				if ( ais != null && line != null )
				{
					MMP3File mfile = new MMP3File(filename, props, ais, line, bufferSize);
					file = new AudioPlayer(mfile);
				}
			} // format instanceof MpegAudioFormat
			else {
				Map props = getPropertiesVorbis(filename);
				if (props!=null){
					AudioFormat baseFormat = format;
					format = new AudioFormat(
							AudioFormat.Encoding.PCM_SIGNED,
							baseFormat.getSampleRate(), 16, format.getChannels(),
							baseFormat.getChannels() * 2, format.getSampleRate(),
							false);
					ais = getAudioInputStream(format, ais, false);
					SourceDataLine line = getSourceDataLine(format);
					if ( ais != null && line != null )
					{
						//!!!
						//OptimizedOggAudio mfile = new OptimizedOggAudio(filename, props, ais, line, bufferSize);
						VorbisOggFile mfile = new VorbisOggFile(filename,props,ais,line,bufferSize);
						file = new AudioPlayer(mfile);
					}
				}  //end "not a vorbis"
				else 
				{
					AudioFormat baseFormat = format;
					format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
							baseFormat.getSampleRate(), baseFormat.getSampleSizeInBits(), 
							baseFormat.getChannels(), 
							baseFormat.getFrameSize(), 
							baseFormat.getSampleRate(), false);
					// source data line is for sending the file audio out to the speakers
					SourceDataLine line = getSourceDataLine(format);
					if ( line != null )
					{
						MAudioFile mfile = new MAudioFile(ais, line, bufferSize);
						file = new AudioPlayer(mfile);
					}
				} // else
			}
		} // ais != null
		return file;
	}

	private Map getPropertiesMpeg(String filename)
	{
		debug("Getting the properties.");
		Map props = null;
		try
		{
			MpegAudioFileReader reader = new MpegAudioFileReader();
			InputStream stream = p.openStream(filename);
			AudioFileFormat baseFileFormat = reader.getAudioFileFormat(stream, stream.available());
			if ( baseFileFormat instanceof TAudioFileFormat )
			{
				TAudioFileFormat fileFormat = (TAudioFileFormat)baseFileFormat;
				props = fileFormat.properties();
				if ( props == null )
				{
					error("No file properties available for " + filename + ".");
				}
				else
				{
					debug("File properties: " + props.toString());
				}
			}
		}
		catch (UnsupportedAudioFileException e)
		{
			error("Couldn't get the file format for " + filename + ": " 
					+ e.getMessage());
		}
		catch (IOException e)
		{
			error("Couldn't access " + filename + ": " + e.getMessage());
		}
		return props;
	}


	private static Map getPropertiesVorbis(String filename)
	{
		debug("Getting the vorbital properties.");
		Map props = null;
		try
		{
			//VorbisAudioFileReader reader = new VorbisAudioFileReader();
			//Class.forName("javazoom.spi.mpeg.sampled.file.VorbisAudioFileReader");
			InputStream stream;
			if ( filename.startsWith("http") || filename.startsWith("file:") )
			{
				stream = new BufferedInputStream(new URL(filename).openStream());
			} else {
				stream = new BufferedInputStream(new FileInputStream(filename));
			}
			AppletVorbisSPIWorkaround reader = new AppletVorbisSPIWorkaround();
			AudioFileFormat baseFileFormat = reader.getAudioFileFormat(stream);
			//reader.getAudioFileFormat(stream, stream.available());
			if ( baseFileFormat instanceof TAudioFileFormat )
			{
				TAudioFileFormat fileFormat = (TAudioFileFormat)baseFileFormat;
				props = fileFormat.properties();
				if ( props == null )
				{
					error("No file properties available for " + filename + ".");
				}
				else
				{
					debug("File properties: " + props.toString());
				}
			}
		}
		catch (UnsupportedAudioFileException e)
		{
			error("Couldn't get the file format for " + filename + ": " 
					+ e.getMessage());
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
			error("Couldn't access " + filename + ": " + e.getMessage());
		}
		return props;
	}

	/**
	 * Creates an {@link AudioRecorder} that will use <code>source</code> as its 
	 * record source and that will save to the file name specified. The format of the 
	 * file will be inferred from the extension in the file name. If the extension is 
	 * not a recognized file type, this will return null. Be aware that if you choose 
	 * buffered recording the call to {@link AudioRecorder#save()} will block until 
	 * the entire buffer is written to disk. In the event that the buffer is very large, 
	 * your sketch will noticably hang. 
	 * 
	 * @param source
	 *          the <code>Recordable</code> object you want to use as a record source
	 * @param fileName
	 *          the name of the file to record to
	 * @param buffered
	 *          whether or not to use buffered recording
	 *          
	 * @return an <code>AudioRecorder</code> for the record source
	 */
	public AudioRecorder createRecorder(Recordable source,
			String fileName, 
			boolean buffered)
	{
		AudioRecorder rec = null;
		String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		Minim.debug("createRecorder: file extension is " + ext + ".");
		AudioFileFormat.Type fileType = null;
		if (ext.equals(WAV.getExtension()))
		{
			fileType = WAV;
		}
		else if (ext.equals(AIFF.getExtension()) || ext.equals("aif"))
		{
			fileType = AIFF;
		}
		else if (ext.equals(AIFC.getExtension()))
		{
			fileType = AIFC;
		}
		else if (ext.equals(AU.getExtension()))
		{
			fileType = AU;
		}
		else if (ext.equals(SND.getExtension()))
		{
			fileType = SND;
		}
		else
		{
			Minim.error("The extension " + ext + " is not a recognized audio file type.");
			return null;
		}
		rec = new AudioRecorder(source, p.sketchPath(fileName), fileType, buffered);
		return rec;
	}

	/**
	 * Gets an {@link AudioInput}, to which you can attach {@link AudioEffect AudioEffects}.
	 * 
	 * @return an STEREO <code>AudioInput</code> with a 1024 sample buffer, a sample rate of
	 *         44100 and a bit depth of 16
	 * @see #getLineIn(int, int, float, int)
	 */
	static public AudioInput getLineIn()
	{
		return getLineIn(STEREO);
	}

	/**
	 * Gets an {@link AudioInput}, to which you can attach {@link AudioEffect AudioEffects}.
	 * 
	 * @param type
	 *          Minim.MONO or Minim.STEREO
	 * @return an <code>AudioInput</code> with the requested type, a 1024 sample buffer, a
	 *         sample rate of 44100 and a bit depth of 16
	 * @see #getLineIn(int, int, float, int)
	 */
	static public AudioInput getLineIn(int type)
	{
		return getLineIn(type, 1024, 44100, 16);
	}

	/**
	 * Gets an {@link AudioInput}, to which you can attach {@link AudioEffect AudioEffects}.
	 * 
	 * @param type
	 *          Minim.MONO or Minim.STEREO
	 * @param bufferSize
	 *          how long you want the <code>AudioInput</code>'s sample buffer to be
	 * @return an <code>AudioInput</code> with the requested attributes, a sample rate of 44100
	 *         and a bit depth of 16
	 * @see #getLineIn(int, int, float, int)
	 */
	static public AudioInput getLineIn(int type, int bufferSize)
	{
		return getLineIn(type, bufferSize, 44100, 16);
	}

	/**
	 * Gets an {@link AudioInput}, to which you can attach {@link AudioEffect AudioEffects}.
	 * 
	 * @param type
	 *          Minim.MONO or Minim.STEREO
	 * @param bufferSize
	 *          how long you want the <code>AudioInput</code>'s sample buffer to be
	 * @param sampleRate
	 *          the desired sample rate in Hertz (typically 44100)
	 * @return an <code>AudioInput</code> with the requested attributes and a bit depth of 16
	 * @see #getLineIn(int, int, float, int)
	 */
	static public AudioInput getLineIn(int type, int bufferSize, 
			float sampleRate)
	{
		return getLineIn(type, bufferSize, sampleRate, 16);
	}

	/**
	 * Gets an {@link AudioInput}, to which you can attach {@link AudioEffect AudioEffects}.
	 * 
	 * @param type
	 *          Minim.MONO or Minim.STEREO
	 * @param bufferSize
	 *          how long you want the <code>AudioInput</code>'s sample buffer to be
	 * @param sampleRate
	 *          the desired sample rate in Hertz (typically 44100)
	 * @param bitDepth
	 *          the desired bit depth (typically 8)
	 * @return an <code>AudioInput</code> with the requested attributes
	 */
	static public AudioInput getLineIn(int type, int bufferSize,
			float sampleRate, int bitDepth)
	{
		if (bitDepth != 8 && bitDepth != 16)
			throw new IllegalArgumentException(
			"Unsupported bit depth, use either 8 or 16.");
		AudioInput in = null;
		AudioFormat format = new AudioFormat(sampleRate, bitDepth, type, true,
				false);
		TargetDataLine line = getTargetDataLine(format, bufferSize * 4);
		if (line != null)
		{
			MAudioInput min = new MAudioInput(line, bufferSize);
			in = new AudioInput(min);
		}
		else
		{
			error("Minim.getLineIn: attempt failed, could not secure an AudioInput.");
		}
		return in;
	}

	/**
	 * Gets an {@link AudioOutput}, to which you can attach 
	 * {@link AudioSignal AudioSignals} and {@link AudioEffect AudioEffects}.
	 * 
	 * @return a STEREO <code>AudioOutput</code> with a 1024 sample buffer, a sample rate of
	 *         44100 and a bit depth of 16
	 * @see #getLineOut(int, int, float, int)
	 */
	static public AudioOutput getLineOut()
	{
		return getLineOut(STEREO);
	}

	/**
	 * Gets an {@link AudioOutput}, to which you can attach 
	 * {@link AudioSignal AudioSignals} and {@link AudioEffect AudioEffects}.
	 * 
	 * @param type
	 *          Minim.MONO or Minim.STEREO
	 * @return an <code>AudioOutput</code> with the requested type, a 1024 sample buffer, a
	 *         sample rate of 44100 and a bit depth of 16
	 * @see #getLineOut(int, int, float, int)
	 */
	static public AudioOutput getLineOut(int type)
	{
		return getLineOut(type, 1024, 44100, 16);
	}

	/**
	 * Gets an {@link AudioOutput}, to which you can attach 
	 * {@link AudioSignal AudioSignals} and {@link AudioEffect AudioEffects}.
	 * 
	 * @param type
	 *          Minim.MONO or Minim.STEREO
	 * @param bufferSize
	 *          how long you want the <code>AudioOutput</code>'s sample buffer to be
	 * @return an <code>AudioOutput</code> with the requested attributes, a sample rate of
	 *         44100 and a bit depth of 16
	 * @see #getLineOut(int, int, float, int)
	 */
	static public AudioOutput getLineOut(int type, int bufferSize)
	{
		return getLineOut(type, bufferSize, 44100, 16);
	}

	/**
	 * Gets an {@link AudioOutput}, to which you can attach 
	 * {@link AudioSignal AudioSignals} and {@link AudioEffect AudioEffects}.
	 * 
	 * @param type
	 *          Minim.MONO or Minim.STEREO
	 * @param bufferSize
	 *          how long you want the <code>AudioOutput</code>'s sample buffer to be
	 * @param sampleRate
	 *          the desired sample rate in Hertz (typically 44100)
	 * @return an <code>AudioOutput</code> with the requested attributes and a bit depth of 16
	 * @see #getLineOut(int, int, float, int)
	 */
	static public AudioOutput getLineOut(int type, int bufferSize,
			float sampleRate)
	{
		return getLineOut(type, bufferSize, sampleRate, 16);
	}

	/**
	 * Gets an {@link AudioOutput}, to which you can attach 
	 * {@link AudioSignal AudioSignals} and {@link AudioEffect AudioEffects}.
	 * 
	 * @param type
	 *          Minim.MONO or Minim.STEREO
	 * @param bufferSize
	 *          how long you want the <code>AudioOutput</code>'s sample buffer to be
	 * @param sampleRate
	 *          the desired sample rate in Hertz (typically 44100)
	 * @param bitDepth
	 *          the desired bit depth (typically 8)
	 * @return an <code>AudioOutput</code> with the requested attributes
	 */
	static public AudioOutput getLineOut(int type, int bufferSize,
			float sampleRate, int bitDepth)
	{
		if (bitDepth != 8 && bitDepth != 16)
			throw new IllegalArgumentException(
			"Unsupported bit depth, use either 8 or 16.");
		AudioFormat format = new AudioFormat(sampleRate, bitDepth, type, true,
				false);
		SourceDataLine sdl = getSourceDataLine(format);
		AudioOutput out = null;
		if (sdl != null)
		{
			MAudioOutput mout = new MAudioOutput(sdl, bufferSize);
			out = new AudioOutput(mout);
		}
		else
		{
			error("Minim.getLineOut: attempt failed, could not secure a LineOut.");
		}
		return out;
	}

	public static AudioInputStream getAudioInputStream(String filename){

		AudioInputStream ais = null;
		BufferedInputStream bis = null;
		if ( filename.startsWith("http") || filename.startsWith("file:") || filename.startsWith("jar:") )
		{
			try
			{
				ais = getAudioInputStream( new URL(filename));
			}
			catch (MalformedURLException e)
			{
				Minim.error("Bad URL: " + e.getMessage());
			}
			catch (UnsupportedAudioFileException e)
			{
				Minim.error("URL is in an unsupported audio file format: " 
						+ e.getLocalizedMessage());
			}
			catch (IOException e)
			{
				Minim.error("Error reading the URL: " + e.getMessage());
			}      
		}
		else
		{
			try
			{
				/*
				bis = new BufferedInputStream( p.openStream(filename) );
				ais = getAudioInputStream(bis);
				 */
				try {
					ais = getAudioInputStream(new BufferedInputStream(
							new FileInputStream(filename)));
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
				ais.mark((int) ais.available());
				debug("Acquired AudioInputStream "+ais+" .\n" + "It is " + ais.getFrameLength()
						+ " frames long.\n" + "Marking support: " + ais.markSupported());
			}
			catch (IOException ioe)
			{
				error("IOException: " + ioe.getMessage());
			}
		}
		return ais;
	}

	/**
	 * This method is also part of AppletMpegSPIWorkaround, which uses yet 
	 * another workaround to load an internet radio stream.
	 * 
	 * @param url the URL of the stream
	 * @return an AudioInputStream of the streaming audio
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public static AudioInputStream getAudioInputStream(URL url) 
	throws UnsupportedAudioFileException, IOException
	{
		return getAudioInputStream(new BufferedInputStream(url.openStream()));
		/*
		//alexey fix: we use MpegAudioFileReaderWorkaround with URL and user agent
		try 
		{
			Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");
			return new MpegAudioFileReaderWorkaround().getAudioInputStream(url, null);
		} 
		catch (ClassNotFoundException cnfe) 
		{
			throw new IllegalArgumentException("Mpeg codec not properly installed");
		}
		 */

	}

	/**
	 * This method is a replacement for AudioSystem.getAudioInputStream(InputStream),
	 * which includes workaround for getting an mp3 AudioInputStream when sketch is 
	 * running in an applet. The workaround was developed by the Tritonus team 
	 * and originally comes from the package javazoom.jlgui.basicplayer
	 * 
	 * @param is The stream to convert to an AudioInputStream
	 * @return an AudioInputStream that will read from is
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public static AudioInputStream getAudioInputStream(InputStream is) 
	throws UnsupportedAudioFileException, IOException
	{
		try
		{
			Minim.debug("Trying just normal audio system...");
			return AudioSystem.getAudioInputStream(is);
		}
		catch (Exception iae)
		{
			Minim.debug("Preparing to try mp3 & ogg codecs:");
			AudioInputStream mp3Ais = null;
			try
			{
				Minim.debug("Using AppletMpegSPIWorkaround to get mp3 codec");
				Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");
				mp3Ais = new javazoom.spi.mpeg.sampled.file.MpegAudioFileReader().getAudioInputStream(is);
			}
			catch (ClassNotFoundException cnfe)
			{
				throw new IllegalArgumentException("Mpeg codec not properly installed");
			} catch (Exception e){
				//
			}
			if (mp3Ais!=null){
				return mp3Ais;
			}
			AudioInputStream oggAis = null;
			try
			{
				Minim.debug("Using AppletVorbisSPIWorkaround to get ogg codec");
				oggAis = AppletVorbisSPIWorkaround.getAudioInputStream(is);
			} catch (Exception e){
				//
				e.printStackTrace();
			}
			if (oggAis!=null){
				Minim.debug("Audio was .ogg."+oggAis);
				return oggAis;
			}
			Minim.error("Audio was uncodecable!");
			return null;
		}
	}

	/**
	 * This method is a replacement for 
	 * AudioSystem.getAudioInputStream(AudioFormat, AudioInputStream), which is 
	 * used for audio format conversion at the stream level. This method 
	 * includes a workaround for converting from  an mp3 AudioInputStream 
	 * when the sketch is running in an applet. The workaround was developed 
	 * by the Tritonus team and originally comes from the package 
	 * javazoom.jlgui.basicplayer
	 * 
	 * @param targetFormat the AudioFormat to convert the stream to
	 * @param sourceStream the stream containing the unconverted audio
	 * @return an AudioInputStream in the target format
	 */
	public static AudioInputStream getAudioInputStream(AudioFormat targetFormat,
			AudioInputStream sourceStream)
	{
		return getAudioInputStream(targetFormat,sourceStream,true);
	}
	public static AudioInputStream getAudioInputStream(AudioFormat targetFormat,
			AudioInputStream sourceStream, boolean mpeg){

		try
		{
			return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
		}
		catch (IllegalArgumentException iae)
		{
			if (mpeg){
				Minim.debug("Using AppletMpegSPIWorkaround to get codec");
				try
				{
					Class.forName("javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider");
					return new javazoom.spi.mpeg.sampled.convert.
					MpegFormatConversionProvider().getAudioInputStream(targetFormat, sourceStream);
				}
				catch (ClassNotFoundException cnfe)
				{
					throw new IllegalArgumentException("Mpeg codec not properly installed");
				}
			}else{
				Minim.debug("Using AppletVorbisSPIWorkaround to get codec");
				try
				{
					Class.forName("javazoom.spi.vorbis.sampled.convert.VorbisFormatConversionProvider");
					return new javazoom.spi.vorbis.sampled.convert.
					VorbisFormatConversionProvider().getAudioInputStream(targetFormat, sourceStream);
				}
				catch (ClassNotFoundException cnfe)
				{
					throw new IllegalArgumentException("Vorbis codec not properly installed");
				}
			}
		}
	}

	/**
	 * Gets a SourceDataLine with the requested AudioFormat and bufferSize.
	 * 
	 * This method is used by the getLineIn methods, but can be used externally if
	 * you know what to do with a SourceDataLine.
	 * 
	 * @param format
	 *          the AudioFormat you want the SourceDataLine to have
	 */
	static SourceDataLine getSourceDataLine(AudioFormat format)
	{
		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		if (AudioSystem.isLineSupported(info))
		{
			try
			{
				line = (SourceDataLine) AudioSystem.getLine(info);
				// we're gonna do lazy line opening
				// line.open(format, bufferSize * format.getFrameSize() * 4);
				debug("SourceDataLine buffer size is " + line.getBufferSize() + " bytes.\n"
						+ "SourceDataLine format is " + line.getFormat().toString() + ".\n"
						+ line.getLineInfo().toString() + ".");
			}
			catch (Exception e)
			{
				error("Error acquiring SourceDataLine: " + e.getMessage());
			}
		}
		else
		{
			error("Unable to return a SourceDataLine: unsupported format.");
		}
		return line;
	}

	/**
	 * Gets a TargetDataLine with the requested AudioFormat and bufferSize.
	 * 
	 * This method is used by the getLineOut methods, but can be used externally
	 * if you know what to do with a TargetDataLine.
	 * 
	 * @param format
	 *          the AudioFormat you want the TargetDataLine to have
	 * @param bufferSize
	 *          the buffer size you want the TargetDataLine to have
	 */
	static TargetDataLine getTargetDataLine(AudioFormat format,
			int bufferSize)
	{
		TargetDataLine line = null;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (AudioSystem.isLineSupported(info))
		{
			try
			{
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format, bufferSize * format.getFrameSize());
				debug("TargetDataLine buffer size is " + line.getBufferSize() + "\n"
						+ "TargetDataLine format is " + line.getFormat().toString() + "\n"
						+ "TargetDataLine info is " + line.getLineInfo().toString());
			}
			catch (Exception e)
			{
				error("Error acquiring TargetDataLine: " + e.getMessage());
			}
		}
		else
		{
			error("Unable to return a TargetDataLine: unsupported format.");
		}
		return line;
	}
}
