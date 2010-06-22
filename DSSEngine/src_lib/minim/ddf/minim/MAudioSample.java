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

final class MAudioSample extends Thread
                         implements AudioStream
{
  private SignalSplitter splitter;
  private EffectsChain effects;
  
  // sample stuff
  private AudioFormat format;
  private FloatSampleBuffer samples;
  private int[] marks;
  private int   markAt;
  
  // line writing stuff
  private SourceDataLine line;
  private FloatSampleBuffer buffer;
  private boolean finished;
  
  MAudioSample(FloatSampleBuffer samps, SourceDataLine sdl, int bufferSize)
  {
    super();
    format = sdl.getFormat();
    splitter = new SignalSplitter(format, bufferSize);
    effects = new EffectsChain();
    
    marks = new int[20];
    for (int i = 0; i < marks.length; i++)
      marks[i] = -1;
    markAt = 0;
    samples = samps;
    
    line = sdl;
    buffer = new FloatSampleBuffer(format.getChannels(), 
                                   bufferSize,
                                   format.getSampleRate());
    bytes = new byte[bufferSize*4];
    finished = false;
  }
  
  public void trigger()
  {
    marks[markAt] = 0;
    markAt++;
    if ( markAt == marks.length ) markAt = 0;
  } 
  
  private byte[] bytes;
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
      // clear the buffer
      buffer.makeSilence();
      // build our signal from all the marks
      for (int i = 0; i < marks.length; i++)
      {
        int begin = marks[i];
        if (begin == -1) continue;
        //Minim.debug("Sample trigger in process at marks[" + i + "] = " + marks[i]);
        int j, k;
        for (j = begin, k = 0; j < samples.getSampleCount()
                            && k < buffer.getSampleCount(); j++, k++)
        {
          if ( type() == Minim.MONO )
          {
            buffer.getChannel(0)[k] += samples.getChannel(0)[j];
          }
          else
          {
            buffer.getChannel(0)[k] += samples.getChannel(0)[j];           
            buffer.getChannel(1)[k] += samples.getChannel(1)[j];
          }
        }
        if ( j < samples.getSampleCount() )
        {
          marks[i] = j;
        }
        else
        {
          //Minim.debug("Sample trigger ended.");
          marks[i] = -1;
        }
      }
      // apply effects and broadcast samples to our listeners
      if ( type() == Minim.MONO )
      {
        if ( effects.hasEnabled() ) 
        {
          effects.process(buffer.getChannel(0));
        }
        splitter.samples(buffer.getChannel(0));
      }
      else
      {
        if ( effects.hasEnabled() )
        {
          effects.process(buffer.getChannel(0), buffer.getChannel(1));
        }
        splitter.samples(buffer.getChannel(0), buffer.getChannel(1));
      }
      // write to the line
      int wrote = buffer.convertToByteArray(bytes,0,format);
      line.write(bytes, 0, wrote);
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
}
