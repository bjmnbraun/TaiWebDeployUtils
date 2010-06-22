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
import javax.sound.sampled.TargetDataLine;

import org.tritonus.share.sampled.FloatSampleBuffer;

// now, if I make an interface that is the aggregate of 
// Recordable and Effectable, or maybe have one extend the other,
// AudioInput can keep a reference to an interface, instead 
// of a concrete class. with that in place, I could make the 
// constructor public, opening up the possibility that someone 
// would write their own line reader construct.
// It also means that this concrete class could be the 
// reader thread, thereby eliminating one level of callback.
final class MAudioInput extends Thread
                        implements AudioStream
{
  private SignalSplitter splitter;
  private EffectsChain effects;
  
  // line reading variables 
  private TargetDataLine line;
  private FloatSampleBuffer buffer;
  private boolean finished;
  private boolean mono;
  private byte[] rawBytes;
  
  MAudioInput(TargetDataLine tdl, int bufferSize)
  {
    splitter = new SignalSplitter(tdl.getFormat(), bufferSize);
    effects = new EffectsChain();
    line = tdl;
    buffer = new FloatSampleBuffer(tdl.getFormat().getChannels(), 
        bufferSize,
        tdl.getFormat().getSampleRate());
    finished = false;
    mono = ( buffer.getChannelCount() == 1 );
    int byteBufferSize = buffer.getByteArrayBufferSize(line.getFormat());
    Minim.debug("byteBufferSize is " + byteBufferSize);
    rawBytes = new byte[byteBufferSize];
  }
  
  public void run()
  {
    line.start();
    while ( !finished )
    {
      // read from the line
      line.read(rawBytes, 0, rawBytes.length);
      // convert to float samples
      buffer.setSamplesFromBytes(rawBytes, 0, line.getFormat(), 
                                 0, buffer.getSampleCount());
      // apply effects, if any, and broadcast the result
      // to all listeners
      if ( mono )
      {
        float[] samp = buffer.getChannel(0);
        if ( effects.hasEnabled() )
        {
          float[] tmp = new float[samp.length];
          System.arraycopy(samp, 0, tmp, 0, tmp.length);
          effects.process(tmp);
          samp = tmp;
        }
        splitter.samples(samp);
      }
      else
      {
        float[] sampL = buffer.getChannel(0);
        float[] sampR = buffer.getChannel(1);
        if ( effects.hasEnabled() )
        {
          float[] tl = new float[sampL.length];
          float[] tr = new float[sampR.length];
          System.arraycopy(sampL, 0, tl, 0, tl.length);
          System.arraycopy(sampR, 0, tr, 0, tr.length);
          effects.process(tl, tr);
          sampL = tl;
          sampR = tr;
        }
        splitter.samples(sampL, sampR);
      }
    }
    // we are done, clean up the line
    line.flush();
    line.stop();
    line.close();
    line = null;
  }
  
  public void open()
  {
    start();
  }
  
  public void close()
  {
    finished = true;
  }

  public void addListener(AudioListener listener)
  {
    splitter.addListener(listener);    
  }
  
  public void removeListener(AudioListener listener)
  {
    splitter.addListener(listener);   
  }

  public int bufferSize()
  {
   return splitter.bufferSize();
  }

  public AudioFormat getFormat()
  {
    return splitter.getFormat();
  } 
  
  public int type()
  {
    return splitter.type();
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

  public AudioEffect getEffect(int i)
  {
    return effects.get(i);
  }

  public boolean isEffected()
  {
    return effects.hasEnabled();
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

  public void disableEffect(AudioEffect effect)
  {
    effects.disable(effect);    
  }

  public void enableEffect(AudioEffect effect)
  {
    effects.enable(effect);    
  }

  public boolean isEnabled(AudioEffect effect)
  {
    return effects.isEnabled(effect);
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
