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

/**
 * An <code>AudioOutput</code> is used to generate audio with
 * <code>AudioSignal</code>s. Well, strictly speaking, the
 * <code>AudioSynthesizer</code> it is constructed with generates the signals
 * and <code>AudioOutput</code> merely delegates to the synth when signals are
 * added. You can get an <code>AudioOutput</code> from <code>Minim</code> by
 * calling one of the <code>getLineOut</code> methods.
 * 
 * @author Damien Di Fede
 * 
 */
public class AudioOutput extends AudioSource implements Polyphonic
{
  // the synth we'll delegate to
  private AudioSynthesizer synth;

  /**
   * Constructs an <code>AudioOutput</code> that will subscribe its buffers to
   * <code>synthesizer</code> and be able to control the <code>DataLine</code>
   * the synthesizer uses for output. If the synth does not have an associated
   * <code>DataLine</code>, then calls to <code>Controller</code>'s
   * methods will result in a <code>NullPointerException</code>.
   * 
   * @param synthesizer
   *          the <code>AudioSynthesizer</code> to subscribe to
   */
  public AudioOutput(AudioSynthesizer synthesizer)
  {
    super(synthesizer);
    synth = synthesizer;
  }

  public void addSignal(AudioSignal signal)
  {
    synth.addSignal(signal);
  }

  public AudioSignal getSignal(int i)
  {
    return synth.getSignal(i);
  }

  public void removeSignal(AudioSignal signal)
  {
    synth.removeSignal(signal);
  }

  public AudioSignal removeSignal(int i)
  {
    return synth.removeSignal(i);
  }

  public void clearSignals()
  {
    synth.clearSignals();
  }

  public void disableSignal(int i)
  {
    synth.disableSignal(i);
  }

  public void disableSignal(AudioSignal signal)
  {
    synth.disableSignal(signal);
  }

  public void enableSignal(int i)
  {
    synth.enableSignal(i);
  }

  public void enableSignal(AudioSignal signal)
  {
    synth.enableSignal(signal);
  }

  public boolean isEnabled(AudioSignal signal)
  {
    return synth.isEnabled(signal);
  }

  public boolean isSounding()
  {
    return synth.isSounding();
  }

  public void noSound()
  {
    synth.noSound();
  }

  public int signalCount()
  {
    return synth.signalCount();
  }

  public void sound()
  {
    synth.sound();
  }

  public boolean hasSignal(AudioSignal signal)
  {
    return synth.hasSignal(signal);
  }
}
