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
    	double pixelSize = Dert.getWorldView().getViewpoint().getCamera().getPixelSizeAt(pl, true);
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
