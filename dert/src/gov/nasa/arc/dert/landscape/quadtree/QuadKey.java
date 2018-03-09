package gov.nasa.arc.dert.landscape.quadtree;

import java.util.ArrayList;

import com.ardor3d.math.Vector3;


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

	/**
	 * Given a key, get the tile center relative to the center of the landscape
	 * 
	 * @param key
	 * @return
	 */
	public Vector3 getTileCenter(double terrainWidth, double terrainLength) {
		Vector3 p = new Vector3();
		if (path.length < 1)
			return (p);
		double wid = terrainWidth/2;
		double len = terrainLength/2;
		for (int i=0; i<path.length; ++i) {
			wid /= 2;
			len /= 2;
			switch (path[i]) {
			case 1:
				p.set(p.getX() - wid, p.getY() + len, 0);
				break;
			case 2:
				p.set(p.getX() + wid, p.getY() + len, 0);
				break;
			case 3:
				p.set(p.getX() - wid, p.getY() - len, 0);
				break;
			case 4:
				p.set(p.getX() + wid, p.getY() - len, 0);
				break;
			}
		}
		return (p);
	}

}
