package gov.nasa.arc.dert.util;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.raster.RasterFile.DataType;
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
			swapBytes(raster.getDataBuffer());
			format = ImageDataFormat.BGRA;
			// format = ImageDataFormat.RGBA;
		} else if (bImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
			swapBytes(raster.getDataBuffer());
			format = ImageDataFormat.RGBA;
		} else if ((bImage.getType() == BufferedImage.TYPE_INT_RGB) && Dert.isMac) {
			swapBytes(raster.getDataBuffer());
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

	private static void swapBytes(DataBuffer dataBuffer) {
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
