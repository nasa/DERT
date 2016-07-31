package gov.nasa.arc.dert.raster;

import gov.nasa.arc.dert.raster.RasterFile.DataType;
import gov.nasa.arc.dert.util.MathUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

/**
 * Represents a raster file in memory using a MultiMappedByteBuffer.
 *
 */
public class Raster {

	protected static long MAX_SIZE = 4294967296l; // 4096 MB

	// Raster dimensions
	protected int width, length, numBytes;

	// Raster data type
	protected DataType dataType;

	// Raster extrema
	protected double[] minimum, maximum;

	// The memory mapped buffer fields
	protected MultiMappedByteBuffer mmbBuf;
	protected long size;

	// Used to compute mean pixel values
	protected ByteBuffer meanBuf;
	protected byte[] meanArray;

	public Raster(int width, int length, int numBytes, DataType dataType, String path) throws IOException {
		this.width = width;
		this.length = length;
		this.numBytes = numBytes;
		this.dataType = dataType;
		size = width * length * numBytes;

		mmbBuf = new MultiMappedByteBuffer(path, width * numBytes, length);

		meanArray = new byte[width * numBytes];
		meanBuf = ByteBuffer.wrap(meanArray);
		meanBuf.rewind();
	}

	/**
	 * Dispose of resources
	 */
	public void dispose() {
		mmbBuf.dispose();
	}

	/**
	 * Flush the memory mapped buffer
	 */
	public void flush() {
		mmbBuf.flush();
	}

	/**
	 * Get the width of the raster
	 * 
	 * @return
	 */
	public int getWidth() {
		return (width);
	}

	/**
	 * Get the length of the raster
	 * 
	 * @return
	 */
	public int getLength() {
		return (length);
	}

	/**
	 * Get the bytes per pixel of the raster
	 * 
	 * @return
	 */
	public int getBytesPerSample() {
		return (numBytes);
	}

	/**
	 * Fill a buffer with a portion of the raster
	 * 
	 * @param rasterTop
	 * @param rasterLeft
	 * @param rasterWid
	 * @param rasterHgt
	 * @param bBuf
	 * @param bufferTop
	 * @param bufferLeft
	 * @param bufferWid
	 * @param kernelSize
	 */
	public void get(int rasterTop, int rasterLeft, int rasterWid, int rasterHgt, ByteBuffer bBuf, int bufferTop,
		int bufferLeft, int bufferWid, int kernelSize) {
		int rasterBottom = rasterTop + rasterHgt;
		int rasterRight = rasterLeft + rasterWid;
		int bufferRow = bufferTop;
		switch (dataType) {
		case Float:
			FloatBuffer fbuf = bBuf.asFloatBuffer();
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				fbuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					fbuf.put(meanFloat(r, c, kernelSize));
				}
				bufferRow++;
			}
			break;
		case Integer:
			IntBuffer ibuf = bBuf.asIntBuffer();
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				ibuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					ibuf.put(meanInt(r, c, kernelSize));
				}
				bufferRow++;
			}
			break;
		case UnsignedInteger:
			IntBuffer uibuf = bBuf.asIntBuffer();
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				uibuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					uibuf.put((int) (meanUnsignedInt(r, c, kernelSize) & 0xffffffff));
				}
				bufferRow++;
			}
			break;
		case Short:
			IntBuffer sbuf = bBuf.asIntBuffer();
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				sbuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					sbuf.put(meanShort(r, c, kernelSize));
				}
				bufferRow++;
			}
			break;
		case UnsignedShort:
			IntBuffer usbuf = bBuf.asIntBuffer();
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				usbuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					usbuf.put((short) (meanUnsignedShort(r, c, kernelSize) & 0xffff));
				}
				bufferRow++;
			}
			break;
		case Byte:
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				bBuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					bBuf.put(meanByte(r, c, kernelSize));
				}
				bufferRow++;
			}
			break;
		case UnsignedByte:
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				bBuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					bBuf.put((byte) (meanUnsignedByte(r, c, kernelSize) & 0xff));
				}
				bufferRow++;
			}
			break;
		case Double:
			DoubleBuffer dbuf = bBuf.asDoubleBuffer();
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				dbuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					dbuf.put(meanDouble(r, c, kernelSize));
				}
				bufferRow++;
			}
			break;
		case Long:
			LongBuffer lbuf = bBuf.asLongBuffer();
			for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
				lbuf.position(bufferRow * bufferWid + bufferLeft);
				for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
					lbuf.put(meanLong(r, c, kernelSize));
				}
				bufferRow++;
			}
			break;
		case Unknown:
			break;
		}
		bBuf.rewind();
	}

	/**
	 * Fill a color buffer with a portion of the raster
	 * 
	 * @param rasterTop
	 * @param rasterLeft
	 * @param rasterWid
	 * @param rasterHgt
	 * @param bBuf
	 * @param bufferTop
	 * @param bufferLeft
	 * @param bufferWid
	 * @param kernelSize
	 */
	public void getRGBA(int rasterTop, int rasterLeft, int rasterWid, int rasterHgt, ByteBuffer bBuf, int bufferTop,
		int bufferLeft, int bufferWid, int kernelSize) {
		int rasterBottom = rasterTop + rasterHgt;
		int rasterRight = rasterLeft + rasterWid;
		int bufferRow = bufferTop;
		IntBuffer ibuf = bBuf.asIntBuffer();
		for (int r = rasterTop; r < rasterBottom; r += kernelSize) {
			ibuf.position(bufferRow * bufferWid + bufferLeft);
			for (int c = rasterLeft; c < rasterRight; c += kernelSize) {
				ibuf.put(meanRGBA(r, c, kernelSize));
			}
			bufferRow++;
		}
		bBuf.rewind();
	}

	/**
	 * Fill a portion of the raster with a byte array
	 * 
	 * @param row
	 * @param hgt
	 * @param bArray
	 */
	public void set(int row, int hgt, byte[] bArray) {
		int b = 0;
		for (int r = row; r < (row + hgt); ++r) {
			mmbBuf.set(r, 0, width * numBytes, b * width * numBytes, bArray);
			b++;
		}
	}

	/**
	 * Fill a portion of the raster with a buffer
	 * 
	 * @param row
	 * @param column
	 * @param wid
	 * @param hgt
	 * @param bArray
	 */
	public void set(int row, int column, int wid, int hgt, byte[] bArray) {
		int b = 0;
		for (int r = row; r < (row + hgt); ++r) {
			mmbBuf.set(r, column * numBytes, wid * numBytes, b * wid * numBytes, bArray);
			b++;
		}
	}

	/**
	 * Fill the memory mapped buffer with an integer value
	 * 
	 * @param val
	 */
	public void set(int val) {
		mmbBuf.set(val);
	}

	/**
	 * Fill the memory mapped buffer with a float value
	 * 
	 * @param val
	 */
	public void set(float val) {
		mmbBuf.set(val);
	}

	/**
	 * Fill a portion of the raster with floats from a buffer
	 * 
	 * @param top
	 * @param left
	 * @param wid
	 * @param hgt
	 * @param bBuf
	 * @param type
	 * @param scalingFactor
	 * @param min
	 * @param max
	 * @param missing
	 */
	public void setAsFloat(int top, int left, int wid, int hgt, ByteBuffer bBuf, DataType type, float scalingFactor,
		double[] min, double[] max, float missing) {
		byte[] bArray = new byte[wid * 4 * hgt];
		ByteBuffer rBuf = ByteBuffer.wrap(bArray);
		rBuf.rewind();
		// System.err.println("Raster.setAsFloat "+top+" "+left+" "+wid+" "+hgt+" "+bBuf.limit()+" "+type+" "+rBuf.capacity()+" "+rBuf.limit());
		int bottom = top + hgt;
		int right = left + wid;
		switch (type) {
		case Float:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = bBuf.getFloat();
					if (Float.isNaN(val) || (val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat(val * scalingFactor);
					}
				}
			}
			break;
		case Integer:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = bBuf.getInt();
					if ((val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat(val * scalingFactor);
					}
				}
			}
			break;
		case UnsignedInteger:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = MathUtil.unsignedInt(bBuf.getInt());
					if ((val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat(val * scalingFactor);
					}
				}
			}
			break;
		case Short:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = bBuf.getShort();
					if ((val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat(val * scalingFactor);
					}
				}
			}
			break;
		case UnsignedShort:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = MathUtil.unsignedShort(bBuf.getShort());
					if ((val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat(val * scalingFactor);
					}
				}
			}
			break;
		case Byte:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = bBuf.get();
					if ((val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat(val * scalingFactor);
					}
				}
			}
			break;
		case UnsignedByte:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = MathUtil.unsignedByte(bBuf.get());
					if ((val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat(val * scalingFactor);
					}
				}
			}
			break;
		case Double:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					double val = bBuf.getDouble();
					if (Double.isNaN(val) || (val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat((float) val * scalingFactor);
					}
				}
			}
			break;
		case Long:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = bBuf.getLong();
					if ((val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.putFloat(Float.NaN);
					} else {
						rBuf.putFloat(val * scalingFactor);
					}
				}
			}
			break;
		case Unknown:
			break;
		}
		bBuf.rewind();
		rBuf.rewind();
		set(top, left, wid, hgt, bArray);
	}

	/**
	 * Fill a portion of the raster with gray scale values from a buffer
	 * 
	 * @param top
	 * @param left
	 * @param wid
	 * @param hgt
	 * @param bBuf
	 * @param type
	 * @param min
	 * @param max
	 * @param missing
	 */
	public void setAsGray(int top, int left, int wid, int hgt, ByteBuffer bBuf, DataType type, double[] min,
		double[] max, float missing) {
		double range = max[0] - min[0];
//		System.err.println("Raster.setAsGray "+range+" "+type+" "+missing+" "+wid+" "+bBuf.limit());
		byte[] bArray = new byte[bBuf.limit()];
		ByteBuffer rBuf = ByteBuffer.wrap(bArray);
		rBuf.rewind();
		int bottom = top + hgt;
		int right = left + wid;
		switch (type) {
		case Float:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					float val = bBuf.getFloat();
					if (Float.isNaN(val) || (val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						int ival = (int) (254.0 * (val - min[0]) / range + 1);
						rBuf.put((byte) ival);
						if (ival == 0) {
							System.err.println("Raster.setAsGray " + ival + " " + val + " " + min[0] + " " + max[0]
								+ " " + range);
						}
					}
				}
			}
			break;
		case Integer:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					int val = bBuf.getInt();
					if (val == missing || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						rBuf.put((byte) (254.0 * (val - min[0]) / range + 1));
					}
				}
			}
			break;
		case UnsignedInteger:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					long val = MathUtil.unsignedInt(bBuf.getInt());
					if (val == missing || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						rBuf.put((byte) (254.0 * (val - min[0]) / range + 1));
					}
				}
			}
			break;
		case Short:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					short val = bBuf.getShort();
					if (val == missing || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						rBuf.put((byte) (254.0 * (val - min[0]) / range + 1));
					}
				}
			}
			break;
		case UnsignedShort:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					int val = MathUtil.unsignedShort(bBuf.getShort());
					if (val == missing || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						rBuf.put((byte) (254.0 * (val - min[0]) / range + 1));
					}
				}
			}
			break;
		case Byte:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					byte val = bBuf.get();
					if (val == missing || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						rBuf.put((byte) (254.0 * (val - min[0]) / range + 1));
					}
				}
			}
			break;
		case UnsignedByte:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					int val = MathUtil.unsignedByte(bBuf.get());
					if (val == missing || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						rBuf.put((byte) (254.0 * (val - min[0]) / range + 1));
					}
				}
			}
			break;
		case Double:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					double val = bBuf.getDouble();
					if (Double.isNaN(val) || (val == missing) || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						int ival = (int) (254.0 * (val - min[0]) / range + 1);
						rBuf.put((byte) ival);
						if (ival == 0) {
							System.err.println("Raster.setAsGray " + ival + " " + val + " " + min[0] + " " + max[0]
								+ " " + range);
						}
					}
				}
			}
			break;
		case Long:
			for (int r = top; r < bottom; ++r) {
				for (int c = left; c < right; ++c) {
					long val = bBuf.getLong();
					if (val == missing || (val < min[0]) || (val > max[0])) {
						rBuf.put((byte) 0);
					} else {
						rBuf.put((byte) (254.0 * (val - min[0]) / range + 1));
					}
				}
			}
			break;
		case Unknown:
			break;
		}
		bBuf.rewind();
		rBuf.rewind();
		set(top, left, wid, hgt, bArray);
	}

	/**
	 * Compute the minimum and maximum
	 * 
	 * @param samplesPerPixel
	 * @param missing
	 */
	public void computeMinMax(int samplesPerPixel, float missing) {
		minimum = new double[samplesPerPixel];
		maximum = new double[samplesPerPixel];
		Arrays.fill(minimum, Double.MAX_VALUE);
		Arrays.fill(maximum, -Double.MAX_VALUE);
		byte[] bArray = new byte[width * numBytes];
		ByteBuffer bBuf = ByteBuffer.wrap(bArray);
		bBuf.rewind();
		switch (dataType) {
		case Float:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					float val = bBuf.getFloat();
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (!Float.isNaN(val) && (val != missing)) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case Integer:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					int val = bBuf.getInt();
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (val != missing) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case UnsignedInteger:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					long val = MathUtil.unsignedInt(bBuf.getInt());
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (val != missing) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case Short:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					short val = bBuf.getShort();
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (val != missing) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case UnsignedShort:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					int val = MathUtil.unsignedShort(bBuf.getShort());
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (val != missing) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case Byte:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					byte val = bBuf.get();
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (val != missing) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case UnsignedByte:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					int val = MathUtil.unsignedByte(bBuf.get());
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (val != missing) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case Double:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					double val = bBuf.getDouble();
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (!Double.isNaN(val) && (val != missing)) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case Long:
			for (int r = 0; r < length; ++r) {
				mmbBuf.get(r, bArray);
				bBuf.rewind();
				for (int c = 0; c < width; ++c) {
					long val = bBuf.getLong();
					for (int i = 0; i < samplesPerPixel; ++i) {
						if (val != missing) {
							minimum[i] = Math.min(minimum[i], val);
							maximum[i] = Math.max(maximum[i], val);
						}
					}
				}
				bBuf.rewind();
			}
			break;
		case Unknown:
			break;
		}
	}

	/**
	 * Get the minimum values
	 * 
	 * @return
	 */
	public double[] getMinimum() {
		return (minimum);
	}

	/**
	 * Get the maximum values
	 * 
	 * @return
	 */
	public double[] getMaximum() {
		return (maximum);
	}

	/**
	 * Set a raster to the missing value.
	 */
	public void setMissingValuesToNaN(float missing, double[] minimum, double[] maximum) {

		byte[] inArray = new byte[width * numBytes];
		ByteBuffer inBuf = ByteBuffer.wrap(inArray);
		inBuf.rewind();
		byte[] outArray = new byte[width * numBytes];
		ByteBuffer outBuf = ByteBuffer.wrap(outArray);
		outBuf.rewind();

		for (int i = 0; i < length; ++i) {
			mmbBuf.get(i, inArray);
			inBuf.rewind();
			for (int j = 0; j < width; ++j) {
				float val = inBuf.getFloat(); // don't use getFloat(j) - gets
												// every byte as a float
				if (!Float.isNaN(missing) && (val == missing)) {
					outBuf.putFloat(Float.NaN);
				} else if ((minimum != null) && (val < minimum[0])) {
					outBuf.putFloat(Float.NaN);
				} else if ((maximum != null) && (val > maximum[0])) {
					outBuf.putFloat(Float.NaN);
				} else {
					outBuf.putFloat(val);
					// System.err.println("Raster.setMissingValuesToNaN "+val);
				}
			}
			inBuf.rewind();
			outBuf.rewind();
			mmbBuf.set(i, outArray);
		}
	}

	/**
	 * Determine the value used to fill to the edge of the raster.
	 */
	public float computeEdgeFill() {

		if (dataType != DataType.Float) {
			throw new IllegalStateException("Must be a float raster.");
		}

		byte[] bArray1 = new byte[width * numBytes];
		ByteBuffer bBuf1 = ByteBuffer.wrap(bArray1);
		bBuf1.rewind();
		FloatBuffer fBuf1 = bBuf1.asFloatBuffer();
		byte[] bArray2 = new byte[width * numBytes];
		ByteBuffer bBuf2 = ByteBuffer.wrap(bArray2);
		bBuf2.rewind();
		FloatBuffer fBuf2 = bBuf2.asFloatBuffer();

		// find minimum edge value
		float zMin = Float.MAX_VALUE;
		for (int r = 0; r < length; ++r) {
			mmbBuf.get(r, bBuf1.array());
			fBuf1.rewind();
			for (int c = 0; c < width; ++c) {
				float val = fBuf1.get(c);
				if (!Float.isNaN(val)) {
					zMin = Math.min(zMin, val);
					break;
				}
			}
			for (int c = width - 1; c >= 0; c--) {
				float val = fBuf1.get(c);
				if (!Float.isNaN(val)) {
					zMin = Math.min(zMin, val);
					break;
				}
			}
		}
		boolean found = false;
		mmbBuf.get(0, bBuf1.array());
		fBuf1.rewind();
		for (int r = 1; r < length; ++r) {
			boolean done = true;
			mmbBuf.get(r, bBuf2.array());
			fBuf2.rewind();
			for (int c = 0; c < width; ++c) {
				float val1 = fBuf1.get(c);
				float val2 = fBuf2.get(c);
				if (Float.isNaN(val1)) {
					if (!Float.isNaN(val2)) {
						zMin = Math.min(zMin, val2);
						done = false;
						found = true;
					}
				} else {
					found = true;
					if (Float.isNaN(val2)) {
						done = false;
					}
				}
			}
			fBuf2.rewind();
			FloatBuffer tmp = fBuf1;
			fBuf1 = fBuf2;
			fBuf2 = tmp;
			if (found && done) {
				break;
			}
		}
		found = false;
		mmbBuf.get(length - 1, bBuf1.array());
		fBuf1.rewind();
		for (int r = (length - 2); r >= 0; r--) {
			boolean done = true;
			mmbBuf.get(r, bBuf2.array());
			fBuf2.rewind();
			for (int c = 0; c < width; ++c) {
				float val1 = fBuf1.get(c);
				float val2 = fBuf2.get(c);
				if (Float.isNaN(val1)) {
					if (!Float.isNaN(val2)) {
						zMin = Math.min(zMin, val2);
						done = false;
						found = true;
					}
				} else {
					found = true;
					if (Float.isNaN(val2)) {
						done = false;
					}
				}
			}
			fBuf2.rewind();
			FloatBuffer tmp = fBuf1;
			fBuf1 = fBuf2;
			fBuf2 = tmp;
			if (found && done) {
				break;
			}
		}
		return (zMin);
	}

	/**
	 * Fill a buffer with a row from the raster
	 * 
	 * @param row
	 * @param bArray
	 */
	public final void get(int row, byte[] bArray) {
		mmbBuf.get(row, bArray);
	}

	/**
	 * Compute the mean of an area in a raster array.
	 * 
	 * @param i
	 *            the start row
	 * @param j
	 *            the start column
	 * @param size
	 *            the size of the area (width and height)
	 * @return the mean value
	 */

	protected final float meanFloat(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += meanBuf.getFloat();
			}
			meanBuf.rewind();
		}
		return ((float) (sum / (size * size)));
	}

	protected final int meanInt(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += meanBuf.getInt();
			}
			meanBuf.rewind();
		}
		return ((int) Math.round(sum / (size * size)));
	}

	protected final long meanUnsignedInt(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += MathUtil.unsignedInt(meanBuf.getInt());
			}
			meanBuf.rewind();
		}
		return (Math.round(sum / (size * size)));
	}

	protected final short meanShort(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += meanBuf.getShort();
			}
			meanBuf.rewind();
		}
		return ((short) Math.round(sum / (size * size)));
	}

	protected final int meanUnsignedShort(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += MathUtil.unsignedShort(meanBuf.getShort());
			}
			meanBuf.rewind();
		}
		return ((int) Math.round(sum / (size * size)));
	}

	protected final byte meanByte(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += meanBuf.get();
			}
			meanBuf.rewind();
		}
		return ((byte) Math.round(sum / (size * size)));
	}

	protected final int meanUnsignedByte(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += MathUtil.unsignedByte(meanBuf.get());
			}
			meanBuf.rewind();
		}
		return ((int) Math.round(sum / (size * size)));
	}

	protected final double meanDouble(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += meanBuf.getDouble();
			}
			meanBuf.rewind();
		}
		return (sum / (size * size));
	}

	protected final long meanLong(int i, int j, int size) {
		double sum = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = j; c < (j + size); ++c) {
				sum += meanBuf.getInt();
			}
			meanBuf.rewind();
		}
		return (Math.round(sum / (size * size)));
	}

	/**
	 * Compute the mean of a section of an rgba pixel raster.
	 * 
	 * @param i
	 *            the start row
	 * @param j
	 *            the start column
	 * @param size
	 *            the section size (width and height)
	 * @return the mean value for each sample packed in an int
	 */
	protected final int meanRGBA(int i, int j, int size) {
		int red = 0;
		int gre = 0;
		int blu = 0;
		int alp = 0;
		for (int r = i; r < (i + size); ++r) {
			mmbBuf.get(r, j * numBytes, size * numBytes, meanArray);
			for (int c = 0; c < size * 4; c += 4) {
				red += MathUtil.unsignedByte(meanArray[c]);
				gre += MathUtil.unsignedByte(meanArray[c + 1]);
				blu += MathUtil.unsignedByte(meanArray[c + 2]);
				alp += MathUtil.unsignedByte(meanArray[c + 3]);
			}
		}
		red = red / (size * size);
		gre = gre / (size * size);
		blu = blu / (size * size);
		alp = alp / (size * size);
		return (MathUtil.bytes2Int((byte) red, (byte) gre, (byte) blu, (byte) alp));
	}

}
