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
 * An <code>AudioSample</code> is a special kind of file playback that allows
 * you to repeatedly <i>trigger</i> an audio file. It does this by keeping the
 * entire file in an internal buffer and then keeping a list of trigger points.
 * It is {@link Recordable} and {@link Effectable} so access to the samples is
 * available and <code>AudioEffect</code>s can be attached to it, but there
 * are not the cueing abilities found on an <code>AudioSnippet</code> and
 * <code>AudioPlayer</code>. All you can do is <code>trigger()</code> the
 * sound. However, you can trigger the sound even if it is still playing back.
 * <code>AudioSample</code> supports up to 20 overlapping triggers, which
 * should be plenty for short sounds. It is not advised that you use this class
 * for long sounds (like entire songs, for example) because the entire file is
 * kept in memory.
 * 
 * @author Damien Di Fede
 * 
 */
public class AudioSample extends AudioSource {
	// the class that does the real work
	private MAudioSample sample;

	/**
	 * Constructs an <code>AudioSample</code> that sends samples to
	 * <code>line</code> using <code>samp</code>.
	 * 
	 * @param line
	 *          the <code>Line</code> to control
	 * @param samp
	 *          the <code>MAudioSample</code> to trigger
	 */
	AudioSample(MAudioSample samp) {
		super(samp);
		sample = samp;
	}

	/**
	 * Triggers the sample. This may be called while the sound is still playing
	 * back.
	 * 
	 */
	public void trigger() {
		sample.trigger();
	}
}
