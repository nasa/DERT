/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.util;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.io.QuadTreeTile.DataType;
import gov.nasa.arc.dert.render.BasicTextureRenderer;
import gov.nasa.arc.dert.render.SceneCanvas;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.ApplyMode;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides image conversion and other utility methods.
 *
 */
public class ImageUtil {

	public ImageUtil() {
		// nothing here
	}

	/**
	 * Convert a Java BufferedImage to an Ardor3D Image.
	 * 
	 * @param bImage
	 * @param flip
	 * @return
	 */
	public static Image convertToArdor3DImage(BufferedImage bImage, boolean flip) {
		WritableRaster raster = bImage.getRaster();
		ImageDataFormat format = ImageDataFormat.RGBA;
		if (bImage.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
			swapRGBABytes(raster.getDataBuffer());
			format = ImageDataFormat.BGRA;
			// format = ImageDataFormat.RGBA;
		} else if (bImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
			swapRGBABytes(raster.getDataBuffer());
			format = ImageDataFormat.RGBA;
		} else if ((bImage.getType() == BufferedImage.TYPE_INT_RGB) && Dert.isMac) {
			swapRGBABytes(raster.getDataBuffer());
			format = ImageDataFormat.RGB;
		} else if (bImage.getType() == BufferedImage.TYPE_3BYTE_BGR) {
			format = ImageDataFormat.BGR;
		} else if (bImage.getType() == BufferedImage.TYPE_BYTE_GRAY) {
			format = ImageDataFormat.Luminance;
		}
		return (convertToArdor3DImage(raster, bImage.getColorModel(), format, flip));
	}

	/**
	 * Convert a Java WritableRaster to an Ardor3D Image
	 * 
	 * @param raster
	 * @param colorModel
	 * @param format
	 * @param flip
	 * @return
	 */
	public static Image convertToArdor3DImage(WritableRaster raster, ColorModel colorModel, ImageDataFormat format,
		boolean flip) {
		DataBuffer dataBuffer = raster.getDataBuffer();
		int width = raster.getWidth();
		int height = raster.getHeight();
		ByteBuffer byteBuffer = null;
		PixelDataType type = PixelDataType.Byte;
		int pixelSize = colorModel.getPixelSize();
		int pixelBytes = pixelSize / 8;
		if (dataBuffer instanceof DataBufferInt) {
			int[] buffer = ((DataBufferInt) dataBuffer).getData();
			byteBuffer = BufferUtils.createByteBuffer(buffer.length * 4);
//			byteBuffer = ByteBuffer.allocateDirect(buffer.length * 4);
			IntBuffer intBuffer = byteBuffer.asIntBuffer();
			intBuffer.put(buffer);
			intBuffer.rewind();
			if (flip) {
				doFlip(byteBuffer, width * pixelBytes, height);
			}
			if (format == null) {
				format = ImageDataFormat.RGBA;
			}
			type = PixelDataType.UnsignedByte;
		} else if (dataBuffer instanceof DataBufferFloat) {
			float[] buffer = ((DataBufferFloat) dataBuffer).getData();
			byteBuffer = BufferUtils.createByteBuffer(buffer.length * 4);
//			byteBuffer = ByteBuffer.allocateDirect(buffer.length * 4);
			FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
			floatBuffer.put(buffer);
			floatBuffer.rewind();
			if (flip) {
				doFlip(byteBuffer, width * pixelBytes, height);
			}
			format = ImageDataFormat.Luminance;
			type = PixelDataType.Float;
		} else if (dataBuffer instanceof DataBufferShort) {
			short[] buffer = ((DataBufferShort) dataBuffer).getData();
			byteBuffer = BufferUtils.createByteBuffer(buffer.length * 2);
//			byteBuffer = ByteBuffer.allocateDirect(buffer.length * 2);
			ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
			shortBuffer.put(buffer);
			shortBuffer.rewind();
			if (flip) {
				doFlip(byteBuffer, width * pixelBytes, height);
			}
			format = ImageDataFormat.Luminance;
			type = PixelDataType.UnsignedShort;
		} else if (dataBuffer instanceof DataBufferUShort) {
			short[] buffer = ((DataBufferUShort) dataBuffer).getData();
			byteBuffer = BufferUtils.createByteBuffer(buffer.length * 2);
//			byteBuffer = ByteBuffer.allocateDirect(buffer.length * 2);
			ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
			shortBuffer.put(buffer);
			shortBuffer.rewind();
			if (flip) {
				doFlip(byteBuffer, width * pixelBytes, height);
			}
			format = ImageDataFormat.Luminance;
			type = PixelDataType.UnsignedShort;
		} else if ((dataBuffer instanceof DataBufferByte)) {
			byte[] buffer = ((DataBufferByte) dataBuffer).getData();
			int bytesPerPixel = buffer.length / (width * height);
			int scanWidth = width * bytesPerPixel;
			byteBuffer = BufferUtils.createByteBuffer(buffer.length);
//			byteBuffer = ByteBuffer.allocateDirect(buffer.length);
			byteBuffer.put(buffer);
			byteBuffer.rewind();
			if (flip) {
				doFlip(byteBuffer, scanWidth, height);
			}
			if (format == null) {
				if (pixelSize == 24) {
					format = ImageDataFormat.RGB;
				} else if (pixelSize == 32) {
					format = ImageDataFormat.RGBA;
				} else if (pixelSize == 16) {
					format = ImageDataFormat.LuminanceAlpha;
				} else {
					format = ImageDataFormat.Luminance;
				}
			}
			type = PixelDataType.UnsignedByte;
		}
		if (byteBuffer == null) {
			throw new IllegalArgumentException("Unable to convert Raster with DataBuffer of "
				+ dataBuffer.getClass().getName() + " to Ardor3D Image");
		}
		byteBuffer.rewind();
		Image theImage = null;
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
		list.add(byteBuffer);
		theImage = new Image(format, type, width, height, list, null);
		return (theImage);
	}

	public static void swapRGBABytes(DataBuffer dataBuffer) {
		if (dataBuffer instanceof DataBufferInt) {
			int[] buffer = ((DataBufferInt) dataBuffer).getData();
			for (int i = 0; i < buffer.length; ++i) {
				int alpha = ((buffer[i] >> 24) & 0x000000ff);
				int rgb = ((buffer[i] << 8) & 0xffffff00);
				buffer[i] = rgb | alpha;
			}
		} else if (dataBuffer instanceof DataBufferByte) {
			byte[] buffer = ((DataBufferByte) dataBuffer).getData();
			for (int i = 0; i < buffer.length; i += 4) {
				byte b = buffer[i];
				buffer[i] = buffer[i + 1];
				buffer[i + 1] = buffer[i + 2];
				buffer[i + 2] = buffer[i + 3];
				buffer[i + 3] = b;
			}

		}
	}

	public static void swapRGBBytes(ByteBuffer buffer) {
		buffer.rewind();
		int l = buffer.limit();
		for (int i = 0; i < l; i += 3) {
			byte b = buffer.get(i);
			buffer.put(i, buffer.get(i+2));
			buffer.put(i+2, b);
		}
	}

	/**
	 * Convert a ByteBuffer to an Ardor3D Image
	 * 
	 * @param raster
	 * @param pixelSize
	 * @param dataType
	 * @param width
	 * @param height
	 * @return
	 */
	public Image convertToArdor3DImage(ByteBuffer raster, int pixelSize, DataType dataType, int width, int height) {
		return (convertToArdor3DImage(raster, pixelSize, null, dataType, width, height));
	}

	/**
	 * Convert a ByteBuffer to an Ardor3D Image
	 * 
	 * @param raster
	 * @param pixelSize
	 * @param format
	 * @param dataType
	 * @param width
	 * @param height
	 * @return
	 */
	public Image convertToArdor3DImage(ByteBuffer raster, int pixelSize, ImageDataFormat format, DataType dataType,
		int width, int height) {
		ByteBuffer byteBuffer = null;
		PixelDataType type = PixelDataType.Byte;
		switch (dataType) {
		case Integer:
		case UnsignedInteger:
			if (format == null) {
				format = ImageDataFormat.RGBA;
			}
			type = PixelDataType.UnsignedByte;
			break;
		case Float:
			format = ImageDataFormat.Luminance;
			type = PixelDataType.Float;
			break;
		case Short:
		case UnsignedShort:
			format = ImageDataFormat.Luminance;
			type = PixelDataType.UnsignedShort;
			break;
		case Byte:
		case UnsignedByte:
			if (format == null) {
				if (pixelSize == 24) {
					format = ImageDataFormat.RGB;
				} else if (pixelSize == 32) {
					format = ImageDataFormat.RGBA;
				} else if (pixelSize == 16) {
					format = ImageDataFormat.LuminanceAlpha;
				} else {
					format = ImageDataFormat.Luminance;
				}
			}
			type = PixelDataType.UnsignedByte;
			break;
		case Double:
		case Long:
		case Unknown:
			return (null);
		}
		byteBuffer = BufferUtils.createByteBuffer(raster.limit());
//		byteBuffer = ByteBuffer.allocateDirect(raster.limit());
		byteBuffer.put(raster);
		byteBuffer.rewind();
		Image theImage = null;
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
		list.add(byteBuffer);
		theImage = new Image(format, type, width, height, list, null);
		return (theImage);
	}

	/**
	 * Flip an image buffer on the Y axis.
	 * 
	 * @param bBuf
	 * @param width
	 * @param height
	 */
	public static void doFlip(ByteBuffer bBuf, int width, int height) {
		bBuf.rewind();
		byte[] scanLine0 = new byte[width];
		byte[] scanLine1 = new byte[width];
		for (int h = 0; h < height / 2; ++h) {
			bBuf.position(h * width);
			bBuf.get(scanLine0, 0, width);
			bBuf.position((height - h - 1) * width);
			bBuf.get(scanLine1, 0, width);
			bBuf.position((height - h - 1) * width);
			bBuf.put(scanLine0, 0, width);
			bBuf.position(h * width);
			bBuf.put(scanLine1, 0, width);
		}
		bBuf.rewind();
	}

	/**
	 * Swap the bytes aligned on word boundaries in a buffer.
	 * 
	 * @param bBuf
	 */
	public static void doSwap(ByteBuffer bBuf) {
		bBuf.rewind();
		int n = bBuf.limit();
		for (int i = 0; i < n; i += 4) {
			byte b = bBuf.get(i);
			bBuf.put(i, bBuf.get(i + 3));
			bBuf.put(i + 3, b);
			b = bBuf.get(i + 1);
			bBuf.put(i + 1, bBuf.get(i + 2));
			bBuf.put(i + 2, b);
		}
	}

	/**
	 * Given the URL, load an Ardor3D Image.
	 * 
	 * @param imageURL
	 * @param flipIt
	 * @return
	 */
	public static Image loadImage(URL imageURL, boolean flipIt) {
		try {
			BufferedImage bImage = ImageIO.read(imageURL);
			if (bImage == null) {
				System.out.println("Unable to read image.");
				return (null);
			}
			Image image = convertToArdor3DImage(bImage, flipIt);
			return (image);
		} catch (Exception e) {
			System.out.println("Unable to read image, see log.");
			e.printStackTrace();
			return (null);
		}

	}

	/**
	 * Given the file path, load an image file into an Ardor3D Image.
	 * 
	 * @param imagePath
	 * @param flipIt
	 * @return
	 */
	public static Image loadImage(String imagePath, boolean flipIt) {
		try {
			BufferedImage bImage = ImageIO.read(new File(imagePath));
			if (bImage == null) {
				System.out.println("Unable to read image "+imagePath+".");
				return (null);
			}
			Image image = convertToArdor3DImage(bImage, flipIt);
			return (image);
		} catch (Exception e) {
			System.out.println("Unable to read image "+imagePath+", see log.");
			e.printStackTrace();
			return (null);
		}

	}

	/**
	 * Create a texture, given the image file path.
	 * 
	 * @param imagePath
	 * @param flip
	 * @return
	 */
	public static Texture createTexture(String imagePath, boolean flip) {
		Image image = loadImage(getURLFromFilePath(imagePath), flip);
		return (createTexture(image));
	}

	/**
	 * Create a texture, given the image URL.
	 * 
	 * @param url
	 * @param flip
	 * @return
	 */
	public static Texture createTexture(URL url, boolean flip) {
		Image image = loadImage(url, flip);
		return (createTexture(image));
	}

	/**
	 * Create a texture, given the Ardor3D Image.
	 * 
	 * @param image
	 * @return
	 */
	public static Texture createTexture(Image image) {
		Texture texture = null;
		if (image == null) {
			texture = TextureState.getDefaultTexture();
		} else {
			texture = TextureManager.loadFromImage(image, Texture.MinificationFilter.BilinearNoMipMaps);
		}
		texture.setApply(ApplyMode.Modulate);
		return (texture);
	}

	public static int getMaxTextureRendererSize() {
		final ContextCapabilities caps = ContextManager.getCurrentContext().getCapabilities();
		return (caps.getMaxTextureSize() / 2);
	}

	public static BasicTextureRenderer createTextureRenderer(int width, int height, Renderer renderer, boolean isShadow) {

		// Create texture renderer
		final ContextCapabilities caps = ContextManager.getCurrentContext().getCapabilities();
		if (width == 0) {
			width = getMaxTextureRendererSize();
		}
		if (height == 0) {
			height = getMaxTextureRendererSize();
		}
		final DisplaySettings settings = new DisplaySettings(width, height, 0, 0, 0, SceneCanvas.depthBits, 0, 0,
			false, false);
		// TextureRenderer textureRenderer =
		// TextureRendererFactory.INSTANCE.createTextureRenderer(settings,
		// false, renderer, caps);
		BasicTextureRenderer textureRenderer = new BasicTextureRenderer(settings.getWidth(), settings.getHeight(),
			settings.getDepthBits(), settings.getSamples(), renderer, caps);
		textureRenderer.setIsShadow(isShadow);
		return (textureRenderer);
	}

	private static URL getURLFromFilePath(String filePath) {
		try {
			if (filePath.startsWith("file:/")) {
				return (new URL(filePath));
			} else {
				return (new File(filePath).toURI().toURL());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to get URL from file, see log.");
			return (null);
		}
	}
}
