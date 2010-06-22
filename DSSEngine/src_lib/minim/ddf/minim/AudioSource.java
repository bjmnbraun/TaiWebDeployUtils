package ddf.minim;

import javax.sound.sampled.AudioFormat;

/**
 * An <code>AudioSource</code> is a kind of wrapper around an
 * <code>AudioStream</code>. In Minim, the classes that implement
 * <code>AudioStream</code> are <code>Thread</code>s and handle all of the
 * actual work of reading from files and writing to <code>DataLine</code>s.
 * An <code>AudioSource</code> will add its <code>AudioBuffer</code>s as
 * listeners on the stream so that you can access the stream's samples without
 * having to implement <code>AudioListener</code> yourself. It also provides
 * the same <code>Effectable</code> and <code>Recordable</code> interface
 * that the stream has so that you can treat an <code>AudioSource</code> as if
 * it was the stream itself. Because an <code>AudioStream</code> must be
 * closed when you are finished with it, you must remember to call
 * {@link #close()} on any <code>AudioSource</code>s you obtain from Minim,
 * such as <code>AudioInput</code>s, <code>AudioOutput</code>s, and
 * <code>AudioPlayer</code>s.
 * 
 * @author Damien Di Fede
 * 
 */
public class AudioSource extends Controller implements Effectable, Recordable
{
  private AudioStream stream;
  // the StereoBuffer that will subscribe to synth
  private StereoBuffer buffer;

  /**
   * The buffer containing the left channel samples. If this is a mono
   * <code>AudioSource</code>, it contains the single channel of audio.
   */
  public final AudioBuffer left;

  /**
   * The buffer containing the right channel samples. If this is a mono
   * <code>AudioSource</code>, <code>right</code> contains the same samples
   * as <code>left</code>.
   */
  public final AudioBuffer right;

  /**
   * The buffer containing the mix of the left and right channels. If this is a
   * mono <code>AudioSource</code>, <code>mix</code> contains the same
   * samples as <code>left</code>.
   */
  public final AudioBuffer mix;

  /**
   * Constructs an <code>AudioSource</code> that will subscribe to the samples
   * in <code>stream</code>. It is expected that the stream is using a
   * <code>DataLine</code> for playback. If it is not, calls to
   * <code>Controller</code>'s methods will result in a
   * <code>NullPointerException</code>.
   * 
   * @param stream
   *          the <code>AudioStream</code> to subscribe to and wrap
   */
  public AudioSource(AudioStream stream)
  {
    super(stream.getDataLine());
    this.stream = stream;
    this.stream.open();
    buffer = new StereoBuffer(type(), bufferSize(), this);
    this.stream.addListener(buffer);
    left = buffer.left;
    right = buffer.right;
    mix = buffer.mix;
  }

  /**
   * Closes the <code>AudioStream</code> this was constructed with.
   * 
   */
  public void close()
  {
    stream.close();
  }

  public void addEffect(AudioEffect effect)
  {
    stream.addEffect(effect);
  }

  public void clearEffects()
  {
    stream.clearEffects();
  }

  public void disableEffect(int i)
  {
    stream.disableEffect(i);
  }

  public void disableEffect(AudioEffect effect)
  {
    stream.disableEffect(effect);
  }

  public int effectCount()
  {
    return stream.effectCount();
  }

  public void effects()
  {
    stream.effects();
  }
  
  public boolean hasEffect(AudioEffect e)
  {
    return stream.hasEffect(e);
  }

  public void enableEffect(int i)
  {
    stream.enableEffect(i);
  }

  public void enableEffect(AudioEffect effect)
  {
    stream.enableEffect(effect);
  }

  public AudioEffect getEffect(int i)
  {
    return stream.getEffect(i);
  }

  public boolean isEffected()
  {
    return stream.isEffected();
  }

  public boolean isEnabled(AudioEffect effect)
  {
    return stream.isEnabled(effect);
  }

  public void noEffects()
  {
    stream.noEffects();
  }

  public void removeEffect(AudioEffect effect)
  {
    stream.removeEffect(effect);
  }

  public AudioEffect removeEffect(int i)
  {
    return stream.removeEffect(i);
  }

  public void addListener(AudioListener listener)
  {
    stream.addListener(listener);
  }

  public int bufferSize()
  {
    return stream.bufferSize();
  }

  public AudioFormat getFormat()
  {
    return stream.getFormat();
  }

  public void removeListener(AudioListener listener)
  {
    stream.removeListener(listener);
  }

  public int type()
  {
    return stream.type();
  }
  
  public float sampleRate()
  {
    return stream.getFormat().getSampleRate();
  }
}
