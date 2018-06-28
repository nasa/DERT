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

package gov.nasa.arc.dert.scenegraph.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.glu.gl2.GLUgl2;

/**
 * Create an OpenGL bitmap font from a Java font.
 *
 */

public class BitmapFont {
	
	protected static int IMAGE_WIDTH = 1024, IMAGE_HEIGHT = 1024;
	
	private int letterWidth, letterHeight, xOrigin, yOrigin;
	private int bitmapWidth;
	private BitmapChar[] chars;
	private int size;
	
	public BitmapFont(String name, int type, int size) {
		if (size > IMAGE_HEIGHT/2)
			size = IMAGE_HEIGHT/2;
		this.size = size;
		chars = new BitmapChar[256];
		generateFont(name, type, size);
	}
	
	protected void generateFont(String name, int type, int size) {
		Font font = new Font(name, type, size);	
		BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.createGraphics();
		g.setFont(font);
		FontMetrics fontMetrics = g.getFontMetrics(font);
		letterWidth = fontMetrics.getMaxAdvance();
		letterHeight = fontMetrics.getHeight();
		bitmapWidth = (int)Math.ceil(letterWidth/8.0);
		xOrigin = 0;
		yOrigin = fontMetrics.getMaxDescent();
		Rectangle dataRect = new Rectangle(xOrigin, yOrigin, letterWidth, letterHeight);
		for (int i=32; i<127; ++i) {
			String str = Character.toString((char)i);
			drawLetter(str, g);
			DataBufferInt dbi = (DataBufferInt)image.getData(dataRect).getDataBuffer();
			int[] data = dbi.getData();
			byte[] bitmap = new byte[bitmapWidth*letterHeight];
			getBitmap(data, bitmap);
			chars[i] = new BitmapChar((char)i, letterWidth, letterHeight, xOrigin, yOrigin, fontMetrics.charWidth((char)i), bitmap);
		}
		g.dispose();
	}
	
	private void drawLetter(String letter, Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
		g.setColor(Color.white);
		g.drawString(letter, xOrigin, letterHeight+1);
	}
	
	private void getBitmap(int[] data, byte[] bitmap) {
		for (int i=0; i<bitmap.length; ++i)
			bitmap[i] = 0;
		int col = 0;
		int row = 0;
		for (int i=0; i<data.length; ++i) {
			if (col == letterWidth) {
				row ++;
				col = 0;
			}
			int d = data[i] & 0x00ffffff;
			if (d != 0)
				setBit(col, row, bitmap);
			col ++;
		}
	}
	
	private void setBit(int c, int r, byte[] bitmap) {
		int bit = c%8;
		int index = (letterHeight-1-r)*bitmapWidth+c/8;
//		System.err.println("BitmapFont.setBit "+index+" "+letterHeight+" "+r+" "+bitmapWidth+" "+c);
		switch (bit) {
		case 0:
			bitmap[index] |= 0b10000000;
			break;
		case 1:
			bitmap[index] |= 0b01000000;
			break;
		case 2:
			bitmap[index] |= 0b00100000;
			break;
		case 3:
			bitmap[index] |= 0b00010000;
			break;
		case 4:
			bitmap[index] |= 0b00001000;
			break;
		case 5:
			bitmap[index] |= 0b00000100;
			break;
		case 6:
			bitmap[index] |= 0b00000010;
			break;
		case 7:
			bitmap[index] |= 0b00000001;
			break;
		}
	}
	
	public void drawString(String str) {
	    final GL2 gl = GLUgl2.getCurrentGL2();
	    final int[] swapbytes  = new int[1];
	    final int[] lsbfirst   = new int[1];
	    final int[] rowlength  = new int[1];
	    final int[] skiprows   = new int[1];
	    final int[] skippixels = new int[1];
	    final int[] alignment  = new int[1];
	    
	    // save modes
		gl.glGetIntegerv(GL2GL3.GL_UNPACK_SWAP_BYTES, swapbytes, 0);
		gl.glGetIntegerv(GL2GL3.GL_UNPACK_LSB_FIRST, lsbfirst, 0);
		gl.glGetIntegerv(GL2ES2.GL_UNPACK_ROW_LENGTH, rowlength, 0);
		gl.glGetIntegerv(GL2ES2.GL_UNPACK_SKIP_ROWS, skiprows, 0);
		gl.glGetIntegerv(GL2ES2.GL_UNPACK_SKIP_PIXELS, skippixels, 0);
		gl.glGetIntegerv(GL.GL_UNPACK_ALIGNMENT, alignment, 0);

		// set modes
		gl.glPixelStorei(GL2GL3.GL_UNPACK_SWAP_BYTES, GL.GL_FALSE);
		gl.glPixelStorei(GL2GL3.GL_UNPACK_LSB_FIRST, GL.GL_FALSE);
		gl.glPixelStorei(GL2ES2.GL_UNPACK_ROW_LENGTH, 0);
		gl.glPixelStorei(GL2ES2.GL_UNPACK_SKIP_ROWS, 0);
		gl.glPixelStorei(GL2ES2.GL_UNPACK_SKIP_PIXELS, 0);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		
	    final int len = str.length();
	    for (int i = 0; i < len; i++) {
	    	final int c = str.charAt(i) & 0xFFFF;
			if (c >= chars.length)
				continue;
			if (chars[c] == null)
				continue;
	        final BitmapChar ch = chars[c];
	        if (ch != null) {
	          ch.draw(gl);
	        }
	    }
	    
		/* Restore saved modes. */
		gl.glPixelStorei(GL2GL3.GL_UNPACK_SWAP_BYTES, swapbytes[0]);
		gl.glPixelStorei(GL2GL3.GL_UNPACK_LSB_FIRST, lsbfirst[0]);
		gl.glPixelStorei(GL2ES2.GL_UNPACK_ROW_LENGTH, rowlength[0]);
		gl.glPixelStorei(GL2ES2.GL_UNPACK_SKIP_ROWS, skiprows[0]);
		gl.glPixelStorei(GL2ES2.GL_UNPACK_SKIP_PIXELS, skippixels[0]);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, alignment[0]);
	}
	
	public double stringLength(String str) {
		double l = 0;
		for (int i=0; i<str.length(); ++i) {
			int c = str.charAt(i);
			if (c >= chars.length)
				continue;
			if (chars[c] == null)
				continue;
			l += chars[c].getAdvance();
		}
		return(l);
	}
	
	public double getHeight() {
		return(letterHeight);
	}
	
	public int getSize() {
		return(size);
	}
}
