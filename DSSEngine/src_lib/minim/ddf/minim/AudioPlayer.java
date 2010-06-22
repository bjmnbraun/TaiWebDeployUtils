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

import java.util.Map;

/**
 * An <code>AudioPlayer</code> is used for playing an
 * <code>AudioRecording</code>. Strictly speaking, you don't need an
 * <code>AudioPlayer</code> to play an <code>AudioRecording</code>, because
 * the recording is itself <code>Playable</code>. However, an
 * <code>AudioPlayer</code> does you the favor of providing
 * <code>AudioBuffer</code>s that are sync'd with the recording's output as
 * well as providing direct control over the <code>DataLine</code> being used
 * to transmit the recording to the system. You can get an
 * <code>AudioPlayer</code> by calling {@link Minim#loadFile(String)}, but
 * you can also construct one yourself if you've written your own implementation
 * of <code>AudioRecording</code>.
 * 
 * @author Damien Di Fede
 */

public class AudioPlayer extends AudioSource implements Playable
{
  // the rec that this plays
  private AudioRecording rec;

  /**
   * Constructs an <code>AudioPlayer</code> that plays <code>recording</code>.
   * It is expected that <code>recording</code> will have a
   * <code>DataLine</code> to control. If it doesn't, any calls to
   * <code>Controller</code>'s methods will result in a
   * <code>NullPointerException</code>.
   * 
   * @param recording
   *          the <code>AudioRecording</code> to play
   */
  public AudioPlayer(AudioRecording recording)
  {
    super(recording);
    rec = recording;
  }

  public void cue(int millis)
  {
    rec.cue(millis);
  }
  
  public void skip(int millis)
  {
    rec.skip(millis);
  }

  public boolean isLooping()
  {
    return rec.isLooping();
  }

  public boolean isPlaying()
  {
    return rec.isPlaying();
  }

  public int length()
  {
    return rec.length();
  }

  public void loop()
  {
    rec.loop();
  }

  public void loop(int num)
  {
    rec.loop(num);
  }

  public void pause()
  {
    rec.pause();
  }

  public void play()
  {
    rec.play();
  }

  public void play(int millis)
  {
    rec.play(millis);
  }

  public int position()
  {
    return rec.position();
  }

  public void rewind()
  {
    rec.rewind();
  }
  public AudioRecording getAudioRecording(){
	  return rec;
  }
  
  public Map getProperties(){
	 return rec.getProperties(); 
  }
}
