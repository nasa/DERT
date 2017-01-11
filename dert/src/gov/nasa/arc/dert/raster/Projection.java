package gov.nasa.arc.dert.raster;

import gov.nasa.arc.dert.raster.proj.Proj4;
import gov.nasa.arc.dert.view.Console;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;

/**
 * Provides conversion between coordinate systems for raster files. Uses Proj4
 * library
 * 
 * World: The coordinate system of the planetary body. Local: The OpenGL
 * coordinate system. The X,Y center is at 0,0. Spherical: Spherical coordinates
 * (longitude and latitude in degrees).
 *
 */
public class Projection {

	// Information from a file that describes the projection
	private ProjectionInfo projInfo;

	// The control point at the upper left corner of a raster
	private double[] tiePoint = new double[3];

	// The scale of each raster pixel in X, Y, and Z directions
	private double[] scale = { 1, 1, 1 };

	// The actual dimensions in the projected units of the raster
	private double physicalWidth, physicalLength;

	// Proj4 fields
	private Proj4 pjProjected, pjUnprojected;
	private String proj4String;

	// Conversion field
	private double[] coord = new double[3];

	/**
	 * Constructor
	 * 
	 * @param pInfo
	 */
	public Projection(ProjectionInfo pInfo) {
		projInfo = pInfo;

		// kludge to deal with unprojected rasters for now
		if (!projInfo.projected) {

			// get the bounds of the raster
			double[] bounds = projInfo.getBounds();
			System.err.println("Projection "+bounds[0]+" "+bounds[1]+" "+bounds[2]+" "+bounds[3]);
			// get the center lon/lat of the raster
			if (Double.isNaN(projInfo.centerLat)) {
				projInfo.centerLat = (bounds[3] + bounds[1]) / 2;
			}
			if (Double.isNaN(projInfo.centerLon)) {
				projInfo.centerLon = (bounds[2] + bounds[0]) / 2;
			}

			// create a Proj4 conversion command
			String projection = "+proj=longlat +a=" + projInfo.getSemiMajorAxis() + " +b="
				+ projInfo.getSemiMinorAxis() + " +no_defs";
			Proj4 projOld = Proj4.newInstance(projection);
			// Polar Stereographic
			if (bounds[1] < -85) {
				Console.println(
					"Found unprojected data ... projecting tie points with Polar Stereographic.");
				projInfo.coordTransformCode = 15;
				projInfo.poleLat = -90;
				projInfo.scaleAtNaturalOrigin = 1;
				projInfo.falseEasting = 0;
				projInfo.falseNorthing = 0;
				proj4String = projInfo.getProj4String();
			} else if (bounds[3] > 85) {
				Console.println(
					"Found unprojected data ... projecting tie points with Polar Stereographic.");
				projInfo.coordTransformCode = 15;
				projInfo.poleLat = 90;
				projInfo.scaleAtNaturalOrigin = 1;
				projInfo.falseEasting = 0;
				projInfo.falseNorthing = 0;
				proj4String = projInfo.getProj4String();
			}
			// Equirectangular
			else {
				Console.println("Found unprojected data ... projecting tie points with Equirectangular.");
				projInfo.coordTransformCode = 17;
				projInfo.falseEasting = 0;
				projInfo.falseNorthing = 0;
				proj4String = projInfo.getProj4String();
			}
			if (proj4String == null)
				throw new IllegalStateException("Unable to define projection.");

			// Use the projection to create a tie point and scale
			Proj4 projNew = Proj4.newInstance(proj4String);
			double[] xy = new double[3];
			try {
				xy[0] = Math.toRadians(bounds[0]);
				xy[1] = Math.toRadians(bounds[1]);
				projOld.transform(projNew, xy);
				projInfo.tiePoint[0] = xy[0];
				projInfo.tiePoint[1] = xy[1];
				bounds[0] = xy[0];
				bounds[1] = xy[1];
				xy[0] = Math.toRadians(bounds[2]);
				xy[1] = Math.toRadians(bounds[3]);
				projOld.transform(projNew, xy);
				bounds[2] = xy[0];
				bounds[3] = xy[1];
				projInfo.scale[0] = (bounds[2] - bounds[0]) / projInfo.rasterWidth;
				projInfo.scale[1] = (bounds[3] - bounds[1]) / projInfo.rasterLength;
			} catch (Exception e) {
				e.printStackTrace();
			}
			projInfo.projected = true;
		}
		else {
			proj4String = projInfo.getProj4String();
			if (proj4String == null)
				throw new IllegalStateException("Unable to define projection.");
		}

		tiePoint = projInfo.tiePoint;
		scale = projInfo.scale;
		physicalWidth = projInfo.rasterWidth * scale[0];
		physicalLength = projInfo.rasterLength * scale[1];
		Console.println("Projection: "+proj4String);
	}

	/**
	 * Get the coord transform code
	 * 
	 * @return
	 */
	public String getTransformName() {
		return (projInfo.findCoordTransformName(projInfo.coordTransformCode));
	}

	/**
	 * Get the globe
	 * 
	 * @return
	 */
	public String getGlobeName() {
		return (projInfo.globe);
	}

	/**
	 * Get the scale
	 * 
	 * @return
	 */
	public double[] getScale() {
		return (scale);
	}

	/**
	 * Get the tie point
	 * 
	 * @return
	 */
	public double[] getTiePoint() {
		return (tiePoint);
	}

	/**
	 * Convert in place from local OpenGL coordinates to that of the virtual
	 * world.
	 * 
	 * @param coord
	 */
	public void localToWorld(Vector3 coord) {
		// shift the coordinate back to the original position and add the tie
		// point.
		coord.setX(coord.getX() + physicalWidth / 2 + tiePoint[0]);
		coord.setY(coord.getY() - (physicalLength / 2) + tiePoint[1]);
		coord.setZ(coord.getZ() + tiePoint[2]);		
	}

	/**
	 * Convert in place from local OpenGL coordinates to that of the virtual
	 * world.
	 * 
	 * @param coord
	 */
	public void localToWorld(Vector2 coord) {
		// shift the coordinate back to the original position and add the tie
		// point.
		coord.setX(coord.getX() + physicalWidth / 2 + tiePoint[0]);
		coord.setY(coord.getY() - (physicalLength / 2) + tiePoint[1]);
	}

	/**
	 * Convert in place from virtual world coordinates to OpenGL coordinates.
	 * 
	 * @param coord
	 */
	public void worldToLocal(Vector3 coord) {
		// remove the tie point value and shift center to 0,0
		coord.setX(coord.getX() - tiePoint[0] - (physicalWidth / 2));
		coord.setY(coord.getY() - tiePoint[1] + (physicalLength / 2));
		coord.setZ(coord.getZ() - tiePoint[2]);
	}

	/**
	 * Convert virtual world coordinates to lon/lat. Results are in degrees.
	 * 
	 * @param vec
	 */
	public void worldToSpherical(Vector3 vec) {
		if (pjUnprojected == null) {
			String projStr = "+proj=longlat +a=" + projInfo.getSemiMajorAxis() + " +b=" + projInfo.getSemiMinorAxis()
				+ " +no_defs";
			pjUnprojected = Proj4.newInstance(projStr);
		}
		if (pjProjected == null) {
			pjProjected = Proj4.newInstance(proj4String);
		}
		try {
			vec.toArray(coord);
			pjProjected.transform(pjUnprojected, coord);
			vec.setX(Math.toDegrees(coord[0]));
			vec.setY(Math.toDegrees(coord[1]));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Convert lon/lat (degree) coordinate to virtual world coordinates.
	 * 
	 * @param vec
	 */
	public void sphericalToWorld(Vector3 vec) {
		if (pjUnprojected == null) {
			String projStr = "+proj=longlat +a=" + projInfo.getSemiMajorAxis() + " +b=" + projInfo.getSemiMinorAxis()
				+ " +no_defs";
			pjUnprojected = Proj4.newInstance(projStr);
		}
		if (pjProjected == null) {
			pjProjected = Proj4.newInstance(proj4String);
		}
		vec.toArray(coord);
		coord[0] = Math.toRadians(coord[0]);
		coord[1] = Math.toRadians(coord[1]);
		pjUnprojected.transform(pjProjected, coord);
		vec.setX(coord[0]);
		vec.setY(coord[1]);
	}

	/**
	 * Get the projection information
	 * 
	 * @return
	 */
	public ProjectionInfo getProjectionInfo() {
		return (projInfo);
	}

	/*
	 * GeogCS to WKT public String toWKT() { String str =
	 * "GEOGCS[\""+label+"\", "
	 * +datum.toWKT()+", PRIMEM["+primaryMeridianLabel+","
	 * +primaryMeridian+"], UNIT[\""+unitLabel+"\","+unit+"], "+
	 * "AXIS[\"Lat\","+
	 * latAxis+"],"+"AXIS[\"Lon\","+lonAxis+"], AUTHORITY["+authority+"]]";
	 * return(str); }
	 */

	/*
	 * ProjCS to WKT public String toWKT() { String str =
	 * "PROJCS[\""+label+"\", "
	 * +geogCS.toWKT()+", PROJECTION["+projectionLabel+"], "+
	 * "PARAMETER[\"False_Easting\","+easting+"], "+
	 * "PARAMETER[\"False_Northing\","+northing+"], "+
	 * "PARAMETER[\"Central_Meridian\","+centralMeridian+"], "+
	 * "PARAMETER[\"Standard_Parallel_1\","+standardParallel+"], "+
	 * "UNIT[\""+unitLabel+"\","+unit+"], AUTHORITY["+authority+"]]";
	 * return(str); }
	 */

}
