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

package ddf.minim.signals;

/**
 * A square wave alternates between 1 and -1 at regular intervals.
 * 
 * @author ddf
 * @see <a href="http://en.wikipedia.org/wiki/Square_wave">Square Wave</a>
 */
public class SquareWave extends Oscillator
{

  /**
   * Constructs a square wave with the given frequency, amplitude and sample
   * rate.
   * 
   * @param frequency
   *          the frequency of the pulse wave
   * @param amplitude
   *          the amplitude of the pulse wave
   * @param sampleRate
   *          the sample rate of the pulse wave
   */
  public SquareWave(float frequency, float amplitude, float sampleRate)
  {
    super(frequency, amplitude, sampleRate);
  }

  protected float value(float step)
  {
    float v = 0;
    if (step < period() / 2)
      v = 1;
    else
      v = -1;
    return v;
  }

}
