package ddf.minim;

import java.util.Map;

import javax.sound.sampled.AudioInputStream;


/**
 * An <code>AudioRecording</code> is a <code>Playable</code> 
 * <code>AudioStream</code>. This usually means that the backing audio 
 * is being read from a file.
 * 
 * @author Damien Di Fede
 *
 */
public interface AudioRecording extends AudioStream, Playable
{
	public void hearNot(boolean ByPassSpeakerSystem);
	public Map getProperties();
	public AudioInputStream getAudioInputStream();
	public boolean isAtEndOfStream();
}
