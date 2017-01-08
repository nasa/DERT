package gov.nasa.arc.dert.scenegraph.text;

import com.jogamp.opengl.GL2;

public class BitmapChar {

	private char c;	
	private int width;
	private int height;
	private float xOrigin;
	private float yOrigin;
	private float advance;
	private byte[] bitmap;
	
	public BitmapChar(char c, int width, int height, float xOrigin, float yOrigin, float advance, byte[] bitmap) {
		this.c = c;
		this.width = width;
		this.height = height;
		this.xOrigin = xOrigin;
		this.yOrigin = yOrigin;
		this.advance = advance;
		this.bitmap = bitmap;
	}
	
	public void draw(GL2 gl2) {
		gl2.glBitmap(width, height, xOrigin, yOrigin, advance, 0, bitmap, 0);
	}
	
	public double getAdvance() {
		return(advance);
	}
	
	@Override
	public String toString() {
		return(Character.toString(c));
	}
	
}
