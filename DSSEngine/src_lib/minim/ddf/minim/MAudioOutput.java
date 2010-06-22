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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.tritonus.share.sampled.FloatSampleBuffer;

final class MAudioOutput extends Thread
implements AudioSynthesizer
{
	private SignalSplitter splitter;
	private SignalChain signals;
	private EffectsChain effects;

	private SourceDataLine line;
	private AudioFormat format;
	private FloatSampleBuffer buffer;
	private boolean finished;


	MAudioOutput(SourceDataLine sdl, int bufferSize)
	{
		super();
		format = sdl.getFormat();
		splitter = new SignalSplitter(format, bufferSize);
		signals = new SignalChain();
		effects = new EffectsChain();

		buffer = new FloatSampleBuffer(format.getChannels(), 
				bufferSize,
				format.getSampleRate());
		finished = false;
		line = sdl;
	}

	public void run()
	{
		try
		{
			line.open(format, bufferSize() * format.getFrameSize() * 4);
		}
		catch (LineUnavailableException e)
		{
			Minim.error("Error acquiring SourceDataLine: " + e.getMessage());
		}
		line.start();
		while ( !finished )
		{ 
			buffer.makeSilence();
			if ( type() == Minim.MONO )
			{
				signals.generate(buffer.getChannel(0));
				if ( effects.hasEnabled() )
				{
					effects.process(buffer.getChannel(0));
				}
				splitter.samples(buffer.getChannel(0));
			}
			else
			{
				signals.generate(buffer.getChannel(0), buffer.getChannel(1));
				if ( effects.hasEnabled() )
				{
					effects.process(buffer.getChannel(0), buffer.getChannel(1));
				}
				splitter.samples(buffer.getChannel(0), buffer.getChannel(1));
			}
			byte[] bytes = buffer.convertToByteArray(format);
			line.write(bytes, 0, bytes.length);
		}
		line.drain();
		line.stop();
		line.close();
		line = null;
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

	public void addSignal(AudioSignal signal)
	{
		signals.add(signal);    
	}

	public void clearSignals()
	{
		signals.clear();
	}

	public void disableSignal(int i)
	{
		signals.disable(i);   
	}

	public void disableSignal(AudioSignal signal)
	{
		signals.disable(signal);    
	}

	public void enableSignal(int i)
	{
		signals.enable(i);
	}

	public void enableSignal(AudioSignal signal)
	{
		signals.enable(signal);
	}

	public AudioSignal getSignal(int i)
	{
		return signals.get(i);
	}

	public boolean isEnabled(AudioSignal signal)
	{
		return signals.isEnabled(signal);
	}

	public boolean isSounding()
	{
		return signals.hasEnabled();
	}

	public void noSound()
	{
		signals.disableAll();
	}

	public void removeSignal(AudioSignal signal)
	{
		signals.remove(signal);    
	}

	public AudioSignal removeSignal(int i)
	{
		return signals.remove(i);
	}

	public int signalCount()
	{
		return signals.size();
	}

	public void sound()
	{
		signals.enableAll();    
	}

	public DataLine getDataLine()
	{
		return line;
	}

	public float sampleRate()
	{
		return splitter.sampleRate();
	}

	public boolean hasEffect(AudioEffect effect)
	{
		return effects.contains(effect);
	}

	public boolean hasSignal(AudioSignal signal)
	{
		// TODO Auto-generated method stub
		return signals.contains(signal);
	}
}
