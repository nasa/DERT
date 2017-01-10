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
	protected static BitmapFont INSTANCE;
	
	private int letterWidth, letterHeight, xOrigin, yOrigin;
	private int bitmapWidth;
	private BitmapChar[] chars;
	private int size;
	
	public static void createInstance(String name, int type, int size) {
		if (size > IMAGE_HEIGHT/2)
			size = IMAGE_HEIGHT/2;
		INSTANCE = new BitmapFont(name, type, size);
	}
	
	public static BitmapFont getInstance() {
		return(INSTANCE);
	}
	
	protected BitmapFont(String name, int type, int size) {
		this.size = size;
		chars = new BitmapChar[256];
		generateFont(name, type, size);
	}
	
	public void generateFont(String name, int type, int size) {
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
		/* Little endian machines (DEC Alpha for example) could
		   benefit from setting GL_UNPACK_LSB_FIRST to GL_TRUE
		   instead of GL_FALSE, but this would require changing the
		   generated bitmaps too. */
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
