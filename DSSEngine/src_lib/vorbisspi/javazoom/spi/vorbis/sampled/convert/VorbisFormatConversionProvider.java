/*
 *   VorbisFormatConversionProvider.
 * 
 *   JavaZOOM : vorbisspi@javazoom.net
 *              http://www.javazoom.net
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
 *
 */
 
package javazoom.spi.vorbis.sampled.convert;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import javazoom.spi.vorbis.sampled.file.VorbisEncoding;

import org.tritonus.share.sampled.Encodings;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

/**
 * ConversionProvider for VORBIS files.
 */
public class VorbisFormatConversionProvider extends TEncodingFormatConversionProvider
{
	private static final AudioFormat.Encoding	PCM_SIGNED = Encodings.getEncoding("PCM_SIGNED");

	private static final AudioFormat[]	INPUT_FORMATS =
	{
		// mono
		new AudioFormat(VorbisEncoding.VORBISENC, -1.0F, -1, 1, -1, -1.0F, false),
		new AudioFormat(VorbisEncoding.VORBISENC, -1.0F, -1, 1, -1, -1.0F, true),
		// stereo
		new AudioFormat(VorbisEncoding.VORBISENC, -1.0F, -1, 2, -1, -1.0F, false),
		new AudioFormat(VorbisEncoding.VORBISENC, -1.0F, -1, 2, -1, -1.0F, true),
	};

	private static final AudioFormat[]	OUTPUT_FORMATS =
	{
		// mono, 16 bit signed
		new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
		new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),
		// stereo, 16 bit signed
		new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, false),
		new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, true),
	};
	
  /**
   * Constructor.
   */
  public VorbisFormatConversionProvider()
  {
		super(Arrays.asList(INPUT_FORMATS), Arrays.asList(OUTPUT_FORMATS));
  }

  /**
   * Returns converted AudioInputStream.
   */
  public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream)
  {
	  System.out.println(targetFormat+" "+audioInputStream.getFormat());
    if (isConversionSupported(targetFormat, audioInputStream.getFormat()))
    {
      return new DecodedVorbisAudioInputStream(targetFormat, audioInputStream);
    }
    else
    {
      throw new IllegalArgumentException("conversion not supported");
    }
  }
}