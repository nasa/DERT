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
 
Tile Rendering Library - Brian Paul 
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

package gov.nasa.arc.dert.ephemeris;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scenegraph.Ellipsoid;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.UIUtil;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import spice.basic.Body;
import spice.basic.CSPICE;
import spice.basic.SpiceException;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.ApplyMode;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;

public class CelestialBody {
	
    public final static double[] X_AXIS = new double[] {1, 0, 0};
    public final static double[] Y_AXIS = new double[] {0, 1, 0};
    public final static double[] Z_AXIS = new double[] {0, 0, 1};
    public final static double KM_TO_M = 1000;
    public final static double KMsqd_TO_Msqd = KM_TO_M*KM_TO_M;
    public final static double LUMINOSITY = 3.828e26;
    
    public class XForm {
    	public double[][] rotation;
    	public double[] translation;
    	
    	public XForm(double[][] matrix, double[] pos) {
    		rotation = matrix;
    		translation = pos;
    	}
    	
    	@Override
    	public String toString() {
    		String str = "";
//    		str += "["+rotation[0][0]+","+rotation[0][1]+","+rotation[0][2]+"\n";
//    		str += rotation[1][0]+","+rotation[1][1]+","+rotation[1][2]+"\n";
//    		str += rotation[2][0]+","+rotation[2][1]+","+rotation[2][2]+"]\n";
    		str += translation[0]+","+translation[1]+","+translation[2];
    		return(str);
    	}
    }
    
    public class Facet {
        public String id;
        public double[] centroid;
        public double surfaceArea;
        
        public Facet(String id, double[] a, double[] b, double[] c) {
            this.id = id;
            centroid = new double[3];
            for (int i=0; i<centroid.length; ++i)
                centroid[i] = (a[i]+b[i]+c[i])/3;
            double abx = a[0]-b[0];
            double aby = a[1]-b[1];
            double abz = a[2]-b[2];
            double cbx = c[0]-b[0];
            double cby = c[1]-b[1];
            double cbz = c[2]-b[2];
            double crossX = aby*cbz-abz*cby;
            double crossY = abz*cbx-abx*cbz;
            double crossZ = abx*cby-aby*cbx;            
            double d = Math.sqrt(crossX*crossX+crossY*crossY+crossZ*crossZ);
            surfaceArea = d/2;
        }

    }
	
    public Node iPoints;
	private Mesh mesh;
	public String bodyName;
	public String bodyFrame;
	public String bodyShape;
	private double[] radii;
	private String texturePath;
	private double bodyRe;
	private double bodyF;
	private double[][] bodyToEnuMatrix;
	public double[][] horizonPlane;
	private double[] enuPoint, enuNormal;
	private ArrayList<Facet> facets;
	private double surfaceArea;
	private double areaFactor = 1;
	private double[] tmp0;
	private double albedo;
	private double scale = 1;
	private Color color;
	private boolean isLit;
	protected SimpleDateFormat utcDateFormat;
	
	private static CelestialBody worldBody, sunBody, reflectingBody;
	
	public static boolean initialize(Node contents) {
		try {
			String globeName = Landscape.getInstance().getGlobeName();
			ReadOnlyVector3 center = Landscape.getInstance().getCenterLonLat();
			System.err.println("CelestialBody.initialize lon/lat/alt of center:"+center);
			worldBody = new CelestialBody(globeName, "IAU_"+globeName, null, 0, 0.64, "ELLIPSOID", null, false);
			worldBody.setEnuFrame(center.getX(), center.getY(), center.getZ());
			sunBody = new CelestialBody("SUN", "J2000", null, 0, 1, "POINT", Color.ORANGE, false);
			sunBody.radii[0] *= 10;
			sunBody.radii[1] *= 10;
			sunBody.radii[2] *= 10;
			double scale = Landscape.getInstance().getWorldBound().getRadius()/worldBody.distanceTo(sunBody);
			sunBody.setScale(scale, 10);
			Mesh mesh = sunBody.getMesh();
			sunBody.updateMesh(worldBody);
			contents.attachChild(mesh);
			reflectingBody = new CelestialBody("JUPITER", "IAU_JUPITER", Dert.getPath()+"/texture/jupiter.png", 10, 0.52, "ELLIPSOID", null, true);
			scale = Landscape.getInstance().getWorldBound().getRadius()*0.75/worldBody.distanceTo(reflectingBody);
			reflectingBody.setScale(scale, 10);
			mesh = reflectingBody.getMesh();
			reflectingBody.updateMesh(worldBody);
			contents.attachChild(mesh);
			contents.attachChild(worldBody.iPoints);
			return(true);
		}
		catch (Exception e) {
			e.printStackTrace();
			return(false);
		}
	}
	
	public static void update(long time) {
		try {
			// Get current time
			String str = sunBody.time2UtcStr(time);
			System.out.println("CelestialBody time: "+str);
			// Convert epoch to ephemeris time
			double et = CSPICE.str2et(str);
			sunBody.updateMesh(worldBody, et);
			reflectingBody.updateMesh(worldBody, et);
			worldBody.getTotalIndirectLightFromBody(reflectingBody, et);
			System.out.println("Sun occulted by Jupiter: "+worldBody.isOcculted(sunBody, reflectingBody, et, 10));
		}
		catch (SpiceException e) {
			e.printStackTrace();
		}
		
	}
	
	public CelestialBody(String bodyName, String bodyFrame, String texturePath, double theta, double albedo, String bodyShape, Color color, boolean isLit)
		throws SpiceException {
		utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		iPoints = new Node("iPoints");
		tmp0 = new double[3];
		this.bodyName = bodyName;
		this.bodyFrame = bodyFrame;
		this.bodyShape = bodyShape;
		this.texturePath = texturePath;
		this.albedo = albedo;
		this.color = color;
		this.isLit = isLit;
		Body body = new Body(bodyName);
		radii = CSPICE.bodvcd(body.getIDCode(), "RADII");
        bodyRe = radii[0];
        bodyF = (radii[0]-radii[2])/radii[0];
        surfaceArea = Math.pow((Math.pow(radii[0]*radii[0], 1.6)+Math.pow(radii[0]*radii[0], 1.6)+Math.pow(radii[0]*radii[0], 1.6))/3, 1/1.6);
        surfaceArea = surfaceArea*4*Math.PI;
        if (theta > 0) {
        	double tArea = tessellate(theta);
            areaFactor = surfaceArea/tArea;
        	System.out.println(bodyName+" radius a:"+radii[0]+", radius b:"+radii[1]+", radius c:"+radii[2]+", surface area:"+surfaceArea+", est surface area:"+tArea+", area factor:"+areaFactor);
        }
        else
        	System.out.println(bodyName+" radius a:"+radii[0]+", radius b:"+radii[1]+", radius c:"+radii[2]+", surface area:"+surfaceArea);
	}
	
	public Mesh getMesh() {
		return(mesh);
	}
	
	public void setScale(double scale, double angle) {
		this.scale = scale;
		mesh = new Ellipsoid(bodyName+"_ellipsoid", angle, radii[0]*KM_TO_M*scale, radii[1]*KM_TO_M*scale, radii[2]*KM_TO_M*scale);
		if (texturePath != null) {
			TextureState ts = new TextureState();
			Texture texture = ImageUtil.createTexture(texturePath, true);
			texture.setApply(ApplyMode.Modulate);
			ts.setEnabled(true);
			ts.setTexture(texture, 0);
			mesh.setRenderState(ts);
			MaterialState ms = new MaterialState();
			ms.setDiffuse(ColorRGBA.WHITE);
			ms.setAmbient(ColorRGBA.WHITE);
			ms.setEmissive(ColorRGBA.BLACK_NO_ALPHA);
			mesh.setRenderState(ms);
		}
		if (color != null) {
			ColorRGBA rgba = UIUtil.colorToColorRGBA(color);
			MaterialState ms = new MaterialState();
			ms.setDiffuse(rgba);
			ms.setAmbient(rgba);
			ms.setEmissive(ColorRGBA.BLACK_NO_ALPHA);
			mesh.setRenderState(ms);
		}
		mesh.setModelBound(new BoundingBox());
		mesh.updateModelBound();
		if (!isLit)
			mesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		mesh.getSceneHints().setCastsShadows(false);
		mesh.getSceneHints().setAllPickingHints(false);
	}
	
	public void updateMesh(CelestialBody observer, double et) {
		XForm trans = observer.getTransformInEnu(this, et);
		System.out.println(bodyName+" in "+observer.bodyName+" frame:\n"+trans);
		trans.translation[0] *= scale*KM_TO_M;
		trans.translation[1] *= scale*KM_TO_M;
		trans.translation[2] *= scale*KM_TO_M;
		Matrix3 matrix = new Matrix3();
		matrix.setRow(0, new Vector3(trans.rotation[0][0], trans.rotation[0][1], trans.rotation[0][2]));
		matrix.setRow(1, new Vector3(trans.rotation[1][0], trans.rotation[1][1], trans.rotation[1][2]));
		matrix.setRow(2, new Vector3(trans.rotation[2][0], trans.rotation[2][1], trans.rotation[2][2]));
		mesh.setRotation(matrix);
		mesh.setTranslation(trans.translation[0], trans.translation[1], trans.translation[2]);
		mesh.updateGeometricState(0);
	}
	
	public void updateMesh(CelestialBody observer) {
		try {
			// Get current time
			String str = time2UtcStr(System.currentTimeMillis());
			// Convert epoch to ephemeris time
			double et = CSPICE.str2et(str);
			updateMesh(observer, et);
		}
		catch (SpiceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convert Unix time to the UTC format recognized by SPICE.
	 * 
	 * @param time
	 * @return
	 */
	public String time2UtcStr(long time) {
		Date date = new Date(time);
		return (utcDateFormat.format(date));
	}
	
	public void setEnuFrame(double longitude, double latitude, double altitude) {
        try {
        	altitude /= 1000;
        	enuPoint = CSPICE.georec(Math.toRadians(longitude), Math.toRadians(latitude), altitude, bodyRe, bodyF);
            enuNormal = CSPICE.surfnm(radii[0], radii[1], radii[2], enuPoint);
            enuNormal = CSPICE.vhat(enuNormal);

            // Compute the matrix to transform the body-fixed frame to the surface frame
            // make the Y axis Lon = 0
            bodyToEnuMatrix = CSPICE.twovec(enuNormal, 3, Z_AXIS, 2); 
            
            // Get the plane for the limb of the horizon
            horizonPlane = getLimbPlane(enuPoint);
        }
        catch (SpiceException e) {
            e.printStackTrace();
        }
		
	}

    private double[][] getLimbPlane(double[] point) {

        try {            
            double[] nrml = new double[3];
            nrml[0] = point[0]/(radii[0]*radii[0]);
            nrml[1] = point[1]/(radii[1]*radii[1]);
            nrml[2] = point[2]/(radii[2]*radii[2]);
            double[] pln = CSPICE.nvc2pl(nrml, 1);
            double[] pos = new double[3];
            CSPICE.pl2nvp(pln, nrml, pos);
            return (new double[][] {nrml, pos});
        } catch (SpiceException e) {
            e.printStackTrace();
            return (null);
        }
    }
	
    /**
     * Get this body's transform in the observer's body fixed frame at the given epoch.
     * @param observer
     * @param et
     * @return
     */
	public XForm getTransform(CelestialBody observer, double et) {
        try {

            // Get location of observer
            double[] pos = new double[3];
            double[] lt = new double[1];
            CSPICE.spkpos(bodyName, et, observer.bodyFrame, "LT+S", observer.bodyName, pos, lt);
            
            // Get rotation matrix for target
            double[][] rot = CSPICE.pxform(bodyFrame, observer.bodyFrame, et);

            return (new XForm(rot, pos));
        }
        catch (SpiceException e) {
            e.printStackTrace();
            return (null);
        }
		
	}
	
	/**
	 * Get the transform of a target body in this body's ENU frame at the given epoch.
	 * @param target
	 * @param et
	 * @return
	 */
	public XForm getTransformInEnu(CelestialBody target, double et) {
        try {
        	
        	// Get the target transform in our body fixed frame.
        	XForm trans = target.getTransform(this, et);
        	
        	// Move the translation to the ENU frame.
            double[] pos = new double[3];
            System.arraycopy(trans.translation, 0, pos, 0, 3);
            pos[0] -= enuPoint[0];
            pos[1] -= enuPoint[1];
            pos[2] -= enuPoint[2];
            pos = CSPICE.mxv(bodyToEnuMatrix, pos);
            
            // Move the rotation to the ENU frame.
            double[][] matrix = CSPICE.mxm(bodyToEnuMatrix, trans.rotation);
            return(new XForm(matrix, pos));
        }
        catch (SpiceException e) {
            e.printStackTrace();
            return (null);
        }
		
	}
	
	/**
	 * Distance to the target body in meters at the given epoch.
	 * @param target
	 * @param et
	 * @return
	 */
	public double distanceTo(CelestialBody target) {
		try {
			// Get current time
			String str = time2UtcStr(System.currentTimeMillis());
			// Convert epoch to ephemeris time
			double et = CSPICE.str2et(str);
	        // Get location of observer
	        double[] pos = new double[3];
	        double[] lt = new double[1];
	        CSPICE.spkpos(bodyName, et, target.bodyFrame, "LT+S", target.bodyName, pos, lt);
	        return(Math.sqrt(distanceSqd(pos)));
		}
		catch (SpiceException e) {
			e.printStackTrace();
			return(Double.NaN);
		}
		
	}
	
	public boolean isOcculted(CelestialBody back, CelestialBody front, double et, int seconds) {
        try {
        	double[] cnfine = new double[2];
    		CSPICE.wninsd(et, et+seconds, cnfine);
            double[] occ = CSPICE.gfoclt("ANY", front.bodyName, front.bodyShape, front.bodyFrame, back.bodyName, back.bodyShape, back.bodyFrame, "LT", bodyName, seconds/4.0, 10, cnfine);
            return(occ.length > 0);
        }
        catch (SpiceException e) {
            e.printStackTrace();
            return(false);
        }
	}
	
	public boolean isOcculted(String backName, String backFrame, String backShape, String frontName, String frontFrame, double et, int seconds) {
        try {
        	double[] cnfine = new double[2];
    		CSPICE.wninsd(et, et+seconds, cnfine);
            double[] occ = CSPICE.gfoclt("ANY", frontName, "ELLIPSOID", frontFrame, backName, backShape, backFrame, "LT", bodyName, seconds/4.0, 10, cnfine);
            return(occ.length > 0);
        }
        catch (SpiceException e) {
            e.printStackTrace();
            return(false);
        }
	}

    private double tessellate(double theta) {
        if (theta == 0)
            return(0);
        double[][][] points = getSurfacePoints(theta);
        int nLat = points.length-1;
        int nLon = points[0].length-1;
        
        double totalArea = 0;
        facets = new ArrayList<Facet>();
        
        // Bottom triangles
        for (int j=0; j<nLon; ++j) {
            Facet f = new Facet(0+"_"+j+"_A", points[1][j], points[0][j], points[1][j+1]);
            totalArea += f.surfaceArea;
            facets.add(f);
        }                
        
        // Top triangles
        for (int j=0; j<nLon; ++j) {
            Facet f = new Facet((nLat-1)+"_"+j+"_A", points[nLat][j], points[nLat-1][j], points[nLat-1][j+1]);
            totalArea += f.surfaceArea;
            facets.add(f);
        }                

        for (int i=1; i<nLat-1; ++i) {
            for (int j=0; j<nLon; ++j) {
                Facet f = new Facet(i+"_"+j+"_A", points[i+1][j], points[i][j], points[i][j+1]);
                totalArea += f.surfaceArea;
                facets.add(f);
            }                
        }
        for (int i=1; i<nLat-1; ++i) {
            for (int j=0; j<nLon; ++j) {
                Facet f = new Facet(i+"_"+j+"_C", points[i][j+1], points[i+1][j+1], points[i+1][j]);
                totalArea += f.surfaceArea;
                facets.add(f);
            }                
        }
        System.out.println(bodyName+" tessallation: num latitude:"+nLat+", num longitude:"+nLon+", theta:"+theta+", facets:"+facets.size());
        return(totalArea);
    }
    
    private double[][][] getSurfacePoints(double theta) {
        int nLat = (int)(180/theta+0.5)+1;
        int nLon = (int)(360/theta+0.5)+1;
        double[][][] point = new double[nLat][nLon][];
        theta = Math.toRadians(theta);
        double lat = -Math.PI/2;
        for (int r=0; r<nLat; ++r) {
            double lon = -Math.PI;
            for (int c=0; c<nLon; ++c) {
            	double x = radii[0]*Math.cos(lat)*Math.cos(lon);
            	double y = radii[1]*Math.cos(lat)*Math.sin(lon);
            	double z = radii[2]*Math.sin(lat);
                point[r][c] = new double[] {x, y, z};
                lon += theta;
            }
            lat += theta;
        }
        return(point);
    }
    
    /**
     * Get list of positions and values of facets contributing indirect light to an observer at the
     * given time and observer surface point. Results are in observer body fixed frame.
     * @param observer
     * @param et
     * @param observerSurfacePt
     * @return
     */
    public List<double[]> getIndirectLight(CelestialBody observer, double et, double[] observerSurfacePt) {
//		System.err.println("CelestialBody.getIndirectLight observer: "+observer.bodyName+", target: "+bodyName+", et: "+et+", point: "+observerSurfacePt[0]+","+observerSurfacePt[1]+","+observerSurfacePt[2]);
        try {
            double[] lt = new double[1];
            double[] observerPosition = new double[3];
            CSPICE.spkpos(observer.bodyName, et, bodyFrame, "LT+S", bodyName, observerPosition, lt);
            double distToObserverSqd = distanceSqd(observerPosition);
//            System.err.println("Obs pos: "+observerPosition[0]+","+observerPosition[1]+","+observerPosition[2]+" distSqd: "+distToObserverSqd);
            double[] sunPosition = new double[3];
            CSPICE.spkpos("SUN", et, bodyFrame, "LT+S", bodyName, sunPosition, lt);
            double distToSunSqd = distanceSqd(sunPosition);
//            System.err.println("Sun pos: "+sunPosition[0]+","+sunPosition[1]+","+sunPosition[2]+" distSqd: "+distToSunSqd);
            double[] position = new double[3];
            CSPICE.spkpos(bodyName, et, observer.bodyFrame, "LT+S", observer.bodyName, position, lt);
//            System.err.println("Tgt pos: "+position[0]+","+position[1]+","+position[2]);
            
            double[][] fromObserverMatrix = CSPICE.pxform(observer.bodyFrame, bodyFrame, et);
            double[][] toObserverMatrix = CSPICE.pxform(bodyFrame, observer.bodyFrame, et);
            double[][] obsHorizonPlane = rotatePlane(observer.horizonPlane, fromObserverMatrix);
//            System.err.println("Obshorpln: "+obsHorizonPlane[0][0]+","+obsHorizonPlane[0][1]+","+obsHorizonPlane[0][2]+" "+obsHorizonPlane[1][0]+","+obsHorizonPlane[1][1]+","+obsHorizonPlane[1][2]);
            obsHorizonPlane[1][0] += observerPosition[0];
            obsHorizonPlane[1][1] += observerPosition[1];
            obsHorizonPlane[1][2] += observerPosition[2];
            
            double[] surfacePoint = transformPoint(observerSurfacePt, fromObserverMatrix, observerPosition);
//            System.err.println("SrfPt: "+surfacePoint[0]+","+surfacePoint[1]+","+surfacePoint[2]);
            
            double[][] limbPlane = getLimbPlane(surfacePoint);
//            System.err.println("LimbPln: "+limbPlane[0][0]+","+limbPlane[0][1]+","+limbPlane[0][2]+" "+limbPlane[1][0]+","+limbPlane[1][1]+","+limbPlane[1][2]);
            
//            double totalArea = 0;
                        
            // Find illuminated facets visible from observer surface point.
            ArrayList<double[]> result = new ArrayList<double[]>();
            for (int i=0; i<facets.size(); ++i) {
            	Facet f = facets.get(i);
            	if (!isValid(f.centroid, obsHorizonPlane, limbPlane))
            		continue;
                double lambertian = getLambertian(observer.bodyName, et, f.centroid);
                if (Double.isNaN(lambertian))
            		continue;
                double irradiance = LUMINOSITY/(4*Math.PI*distToSunSqd)*lambertian;
                irradiance *= (f.surfaceArea*areaFactor)*KMsqd_TO_Msqd*albedo/(2*Math.PI*distToObserverSqd);
                double[] pos = transformPoint(f.centroid, toObserverMatrix, position);
                double[] r = new double[4];
                System.arraycopy(pos, 0, r, 0, pos.length);
                r[3] = irradiance;
                result.add(r);
//                totalArea += (f.surfaceArea*areaFactor);
            }
//            System.out.println("CelestialBody.getIndirectLight "+result.size()+" "+(totalArea/(surfaceArea/2)));
            return(result);
        }
        catch (SpiceException e) {
            e.printStackTrace();
            return(null);
        }
    }
    
    public List<double[]> getIndirectLightFromBody(CelestialBody cBody, double et) {
    	try {
	    	List<double[]> ilList = cBody.getIndirectLight(this, et, enuPoint);
	    	for (int i=0; i<ilList.size(); ++i) {
	    		double[] val = ilList.get(i);
	    		System.arraycopy(val, 0, tmp0, 0, 3);
	    		tmp0[0] -= enuPoint[0];
	    		tmp0[1] -= enuPoint[1];
	    		tmp0[2] -= enuPoint[2];
	            double[] pos = CSPICE.mxv(bodyToEnuMatrix, tmp0);
	    		System.arraycopy(pos, 0, val, 0, 3);
	    	}
	    	return(ilList);
    	}
    	catch (SpiceException e) {
    		e.printStackTrace();
    		return(null);
    	}
    }
    
    public double getTotalIndirectLightFromBody(CelestialBody cBody, double et) {
	    List<double[]> ilList = getIndirectLightFromBody(cBody, et);
	    if (ilList == null)
	    	return(Double.NaN);
    	double total = 0;
    	iPoints.detachAllChildren();
    	for (int i=0; i<ilList.size(); ++i) {
    		double[] val = ilList.get(i);
    		total += val[3];
    		iPoints.attachChild(createPoint(""+i, val[0]*cBody.scale*KM_TO_M, val[1]*cBody.scale*KM_TO_M, val[2]*cBody.scale*KM_TO_M));
//    		System.out.println("Point "+i+" "+val[0]+","+val[1]+","+val[2]);
    	}
    	iPoints.updateGeometricState(0);
    	System.out.println("Indirect light from "+cBody.bodyName+" to "+bodyName+" count:"+ilList.size()+" total:"+total);
    	return(total);
    }
    
    private Spatial createPoint(String name, double x, double y, double z) {
//    	System.err.println("CelestialBody.createPoint "+name+" "+x+","+y+","+z);
		FigureMarker fm = new FigureMarker(name, new Vector3(x, y, z), 5, 0, Color.GREEN, false, false, true);
		fm.setShape(ShapeType.crystal, false);
		fm.setAutoShowLabel(false);
		return(fm);
    }
    
    private double distanceSqd(double[] pos) {
    	return((pos[0]*pos[0]+pos[1]*pos[1]+pos[2]*pos[2])*KMsqd_TO_Msqd);
    }
    
    private double[][] rotatePlane(double[][] plane, double[][] matrix) {
        try {
            System.arraycopy(plane[0], 0, tmp0, 0, 3);
            tmp0[0] += plane[1][0];
            tmp0[1] += plane[1][1];
            tmp0[2] += plane[1][2];
            double[][] result = new double[2][];
            result[0] = CSPICE.mxv(matrix, tmp0);
            result[1] = CSPICE.mxv(matrix, plane[1]);
            result[0] = CSPICE.vhat(result[0]);
            return(result);
        }
        catch (Exception e) {
            e.printStackTrace();
            return(null);
        }
    }
    
    public double getLambertian(String observerName, double et, double[] point) {
        try {
            double[] resultEpoch = new double[1];
            double[] resultVector = new double[3];
            double[] resultAngles = new double[3];
            boolean[] visible = new boolean[1];
            boolean[] illuminated = new boolean[1];
            CSPICE.illumf("ELLIPSOID", bodyName, "SUN", et, bodyFrame, "LT+S", observerName, point,
                resultEpoch, resultVector, resultAngles, visible, illuminated);
//            System.err.println("CelestialBody.getLambertian observer:"+observerName+" target:"+bodyName+" "+resultAngles[1]+" "+illuminated[0]+" "+et);
            if (!illuminated[0])
            	return(Double.NaN);
            return(Math.cos(resultAngles[1]));
        }
        catch (Exception e) {
            e.printStackTrace();
            return(Double.NaN);
        }
    }
    
    private double[] transformPoint(double[] point, double[][] matrix, double[] offset)
        throws SpiceException {
        point = CSPICE.mxv(matrix, point);
        point[0] += offset[0];
        point[1] += offset[1];
        point[2] += offset[2];
        return(point);
    }
    
    private boolean isValid(double[] pt, double[][] horizonPlane, double[][] limbPlane) {
        if (sideOfPlane(pt, limbPlane) < 0)
            return(false);
        if (sideOfPlane(pt, horizonPlane) < 0)
            return(false);
        return(true);
    }

    private double sideOfPlane(double[] point, double[][] plane) {
        System.arraycopy(point, 0, tmp0, 0, point.length);
        tmp0[0] -= plane[1][0];
        tmp0[1] -= plane[1][1];
        tmp0[2] -= plane[1][2];
        double length = Math.sqrt(tmp0[0]*tmp0[0]+tmp0[1]*tmp0[1]+tmp0[2]*tmp0[2]);
        tmp0[0] /= length;
        tmp0[1] /= length;
        tmp0[2] /= length;       
        double dot = dot(plane[0], tmp0);
        return(Math.signum(dot));
    }
    
    private double dot(double[] v1, double[] v2) {
        double result = 0;
        for (int i=0; i<v1.length; ++i)
            result += v1[i]*v2[i];
        return(result);
    }

}
