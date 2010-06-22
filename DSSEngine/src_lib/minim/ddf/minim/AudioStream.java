package ddf.minim;

import javax.sound.sampled.DataLine;

/**
 * An <code>AudioStream</code> is a stream of samples that is coming from 
 * somewhere. Users of an <code>AudioStream</code> don't really need to know
 * where the samples are coming from. However, typically they will be read 
 * from a <code>Line</code> or a file. An <code>AudioStream</code> needs to 
 * be opened before being used and closed when you are finished with it.
 * 
 * @author Damien Di Fede
 *
 */
public interface AudioStream extends Effectable, Recordable
{
  /**
   * Opens the stream for reading.
   *
   */
  void open();
  
  /**
   * Closes the stream, you will no longer be able to get 
   * audio from it.
   *
   */
  void close();
  
  /**
   * Returns the <code>DataLine</code> being used by this 
   * <code>AudioStream</code>, if it exists.
   * 
   * @return the <code>DataLine</code> being used by this stream, 
   *         or null if it is not using a <code>DataLine</code> 
   */
  DataLine getDataLine();
}
