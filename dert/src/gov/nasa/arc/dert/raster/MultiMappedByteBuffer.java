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
