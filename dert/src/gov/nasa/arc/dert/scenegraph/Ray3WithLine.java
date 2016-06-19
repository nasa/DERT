package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.Dert;

import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;

public class Ray3WithLine
	extends Ray3 {
	
	public Ray3WithLine() {
		super();
	}

    /**
     * @param polygonVertices
     * @param locationStore
     * @return true if this ray intersects a polygon described by the given vertices.
     */
    @Override
    public boolean intersects(final Vector3[] polygonVertices, final Vector3 locationStore) {
    	if (polygonVertices.length == 2) {
    		// LINE
    		return(isCloseEnough(polygonVertices[0], polygonVertices[1], locationStore));
    	}
    	else
    		return(super.intersects(polygonVertices, locationStore));
    }
    
    protected boolean isCloseEnough(Vector3 p0, Vector3 p1, Vector3 store) {
    	Vector3[] bounds = getBounds(p0, p1);
    	Vector3 u = new Vector3(p1);
    	u.subtractLocal(p0);
    	Vector3 v = new Vector3(_direction);
    	Vector3 w0 = new Vector3(p0);
    	w0.subtractLocal(_origin);
    	double a = u.dot(u);
    	double b = u.dot(v);
    	double c = v.dot(v);
    	double d = u.dot(w0);
    	double e = v.dot(w0);
    	
    	Vector3 pl = new Vector3(p0);
    	Vector3 pr = new Vector3(_origin);
    	double dist = 0;
    	
    	double den = a*c-b*b;
    	if (den == 0) {
    		dist = w0.cross(v, w0).length()/v.length();
    		if (p1.distance(_origin) < p0.distance(_origin))
    			pl.set(p1);
    	}
    	else {
        	double tl = (b*e-c*d)/den;
        	double tr = (a*e-b*d)/den;
        	u.scaleAdd(tl, p0, pl);
        	if (!inBounds(pl, bounds))
        		return(false);
        	v.scaleAdd(tr, _origin, pr);
        	dist = pl.distance(pr);        	    		
    	}
    	double pixelSize = Dert.getWorldView().getViewpointNode().getCamera().getPixelSizeAt(pl, true);
    	if (dist <= (pixelSize*4)) {
    		if (store != null)
    			store.set(pl);
    		return(true);
    	}
    	return(false);
    }
    
    private Vector3[] getBounds(Vector3 p0, Vector3 p1) {
    	Vector3[] bounds = new Vector3[2];
    	bounds[0] = new Vector3(p0);
    	bounds[1] = new Vector3(p1);
    	if (p1.getX() < p0.getX()) {
    		bounds[0].setX(p1.getX());
    		bounds[1].setX(p0.getX());
    	}
    	if (p1.getY() < p0.getY()) {
    		bounds[0].setY(p1.getY());
    		bounds[1].setY(p0.getY());
    	}
    	if (p1.getZ() < p0.getZ()) {
    		bounds[0].setZ(p1.getZ());
    		bounds[1].setZ(p0.getZ());
    	}
    	return(bounds);
    }
    
    private boolean inBounds(Vector3 p, Vector3[] bounds) {
    	if (p.getX() < bounds[0].getX())
    		return(false);
    	if (p.getX() > bounds[1].getX())
    		return(false);
    	if (p.getY() < bounds[0].getY())
    		return(false);
    	if (p.getY() > bounds[1].getY())
    		return(false);
    	if (p.getZ() < bounds[0].getZ())
    		return(false);
    	if (p.getZ() > bounds[1].getZ())
    		return(false);
    	return(true);
    }

}
