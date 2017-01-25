package gov.nasa.arc.dert.raster;

import gov.nasa.arc.dert.util.MathUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * Provides a MappedByteBuffer for a file larger than Integer.MAX_VALUE. This is
 * done by creating an array of MappedByteBuffers, positioned at regular
 * intervals in the file and covering only a portion of the file.
 *
 */
public class MultiMappedByteBuffer {

	// The maximum size for a sub-buffer
	private static int MAX_BUFFER_SIZE = Integer.MAX_VALUE;

	// The file channel for the sub-buffers
	private FileChannel fileChannel;

	// An array of sub-buffers
	private MappedByteBuffer[] mbBuf;

	// Number of rows in a sub-buffer
	private int numRows;

	// Raster dimensions
	private int width, length;

	// Size of a sub-buffer
	private long bufSize;

	// Number of sub-buffers
	private int numBuffers;

	// The mapped memory file
	private File file;
	private RandomAccessFile raf;

	/**
	 * Constructor Create a MultiMappedByteBuffer with a given file path, raster
	 * width, and raster length.
	 * 
	 * @param path
	 * @param width
	 *            width of a image scan line in bytes
	 * @param length
	 *            number of scan lines
	 * @throws IOException
	 */
	public MultiMappedByteBuffer(String path, int width, int length) throws IOException {

		this.width = width;
		this.length = length;

		// Determine the number of MappedByteBuffers needed

		long size = MathUtil.unsignedInt(width) * MathUtil.unsignedInt(length);

		numRows = Math.min(length, MAX_BUFFER_SIZE / width);
		bufSize = numRows * width;

		numBuffers = (int) Math.ceil((double) size / bufSize);
		mbBuf = new MappedByteBuffer[numBuffers];

		// System.err.println("MultiMappedByteBuffer "+width+" "+height+" "+size+" "+numRows+" "+bufSize+" "+numBuffers);

		// Create the file

		file = new File(path);
		file.mkdirs();
		file = new File(file, "tmp_" + System.currentTimeMillis());
		file.deleteOnExit();
		raf = new RandomAccessFile(file, "rw");
		fileChannel = raf.getChannel();

		// Create the MappedByteBuffers

		long position = 0;

		for (int i = 0; i < numBuffers; ++i) {
			System.err.println("MultiMappedByteBuffer "+i+" "+position+" "+bufSize);
			mbBuf[i] = fileChannel.map(MapMode.READ_WRITE, position, bufSize);
			position += bufSize;
		}
	}

	/**
	 * Write out all changes to all buffers.
	 */
	public void flush() {
		for (int i = 0; i < mbBuf.length; ++i) {
			mbBuf[i].force();
		}
	}

	/**
	 * Fill a byte array from a row in the file. The array must have the length
	 * of a scan line.
	 * 
	 * @param row
	 * @param bArray
	 */
	public void get(int row, byte[] bArray) {
		int index = row / numRows;
		row = row % numRows;
		mbBuf[index].position(row * width);
		mbBuf[index].get(bArray, 0, width);
	}

	/**
	 * Fill a byte array starting at a given file row and column with wid bytes.
	 * 
	 * @param row
	 * @param column
	 * @param wid
	 * @param bArray
	 */
	public void get(int row, int column, int wid, byte[] bArray) {
		int index = row / numRows;
		row = row % numRows;
		mbBuf[index].position(row * width + column);
		mbBuf[index].get(bArray, 0, wid);
	}

	/**
	 * Set all values in this MultiMappedByteBuffer to a single integer value.
	 * 
	 * @param val
	 */
	public void set(int val) {
		byte[] bArray = new byte[width];
		ByteBuffer bBuf = ByteBuffer.wrap(bArray);
		bBuf.rewind();
		IntBuffer iBuf = bBuf.asIntBuffer();
		int w = width / 4;
		for (int c = 0; c < w; ++c) {
			iBuf.put(val);
		}
		iBuf.rewind();
		for (int r = 0; r < length; ++r) {
			set(r, 0, width, 0, bArray);
		}
	}

	/**
	 * Set all values in this MultiMappedByteBuffer to a single float value.
	 * 
	 * @param val
	 */
	public void set(float val) {
		byte[] bArray = new byte[width];
		ByteBuffer bBuf = ByteBuffer.wrap(bArray);
		bBuf.rewind();
		FloatBuffer fBuf = bBuf.asFloatBuffer();
		int w = width / 4;
		for (int c = 0; c < w; ++c) {
			fBuf.put(val);
		}
		fBuf.rewind();
		for (int r = 0; r < length; ++r) {
			set(r, 0, width, 0, bArray);
		}
	}

	/**
	 * Set a row from a byte array. The array must have the length of a scan
	 * line.
	 * 
	 * @param row
	 * @param bArray
	 */
	public void set(int row, byte[] bArray) {
		int index = row / numRows;
		row = row % numRows;
		mbBuf[index].position(row * width);
		mbBuf[index].put(bArray);
	}

	/**
	 * Set a row starting at a given column from the specified position in a
	 * byte array
	 * 
	 * @param row
	 *            row in file
	 * @param column
	 *            column in file
	 * @param wid
	 *            width of data from array
	 * @param pos
	 *            starting position in array
	 * @param bArray
	 */
	public void set(int row, int column, int wid, int pos, byte[] bArray) {
		int index = row / numRows;
		row = row % numRows;
		// System.err.println("MultiMappedByteBuffer.set "+index+" "+row+" "+column+" "+width+" "+pos+" "+wid);
		mbBuf[index].position(row * width + column);
		mbBuf[index].put(bArray, pos, wid);
	}

	/**
	 * Free the buffers so they can be garbage collected.
	 */
	@Override
	public void finalize() {
		dispose();
	}

	/**
	 * Free the buffers so they can be garbage collected.
	 */
	public void dispose() {
		for (int i = 0; i < mbBuf.length; ++i) {
			mbBuf[i] = null;
		}
		try {
			if (raf != null) {
				raf.close();
			}
			raf = null;
			file.delete();
		} catch (Exception e) {
			// do nothing
		}
	}

}
