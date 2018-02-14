package gov.nasa.arc.dert.terrain;

import java.util.ArrayList;


public class QuadKey {
	
	protected byte[] path;
	
	// Quadrants
	//
	//	+---+---+
	//	| 1 | 2 |
	//	+---+---+
	//	| 3 | 4 |
	//	+---+---+
	//
	
	public QuadKey() {
		path = new byte[0];
	}
	
	public QuadKey(byte[] path) {
		this.path = path;
	}
	
	public QuadKey(String str) {
		String[] token = str.split("/");
		if (token.length <= 1) {
			path = new byte[0];
		}
		else
			path = new byte[token.length-1];
		for (int i=1; i<token.length; ++i)
			path[i-1] = (byte)Integer.parseInt(token[i]);
	}
	
	public QuadKey(ArrayList<Byte> qList) {
		path = new byte[qList.size()];
		for (int i=0; i<path.length; ++i)
			path[i] = qList.get(i);
	}
	
	public QuadKey createChild(int quadrant) {
		byte[] childPath = new byte[path.length+1];
		System.arraycopy(path, 0, childPath, 0, path.length);
		childPath[path.length] = (byte)quadrant;
		return(new QuadKey(childPath));
	}
	
	@Override
	public String toString() {
		String str = "";
		for (int i=0; i<path.length; ++i)
			str += "/"+path[i];
		return(str);
	}

	public final int getLevel() {
		return(path.length);
	}
	
	public final byte[] getPath() {
		return(path);
	}
	
	public final byte getPath(int i) {
		return(path[i]);
	}
	
	public final int getQuadrant() {
		if (path.length == 0)
			return(0);
		return(path[path.length-1]);
	}
	
	public boolean equals(QuadKey that) {
		if (this.path.length != that.path.length)
			return(false);
		int n = Math.min(path.length, that.path.length);
		for (int i=0; i<n; ++i)
			if (this.path[i] != that.path[i])
				return(false);
		return(true);
	}
	
	public boolean startsWith(QuadKey that) {
		for (int i=0; i<that.path.length; ++i)
			if (this.path[i] != that.path[i])
				return(false);
		return(true);
	}
	
	public int findXAtLevel(int x, int level, int tileWidth) {
		int l = getLevel();
		if (level == l)
			return(x);
		if (level > l)
			return(-1);
		for (int i=l-1; i>=level; i--) {
			x /= 2;
			if ((path[i] == 2) || (path[i] == 4))
				x += tileWidth/2;
		}
		return(x);
	}
	
	public int findYAtLevel(int y, int level, int tileLength) {
		int l = getLevel();
		if (level == l)
			return(y);
		if (level > l)
			return(-1);
		for (int i=l-1; i>=level; i--) {
			y /= 2;
			if ((path[i] == 3) || (path[i] == 4))
				y += tileLength/2;
		}
		return(y);
	}

}
