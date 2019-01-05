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

package gov.nasa.arc.dert.landscape.srs;

import gov.nasa.arc.dert.raster.geotiff.GeoKey;
import gov.nasa.arc.dert.util.StringUtil;

import java.util.Properties;

/**
 * Data structure to hold projection information from a raster file.
 *
 */
public class ProjectionInfo {

	// Globes of the solar system
	public enum GlobeName {
		Earth, Mars, Moon, Mercury, Venus, Jupiter, Saturn, Uranus, Neptune, Pluto, Charon, Phoebe, Titan, Enceladus, Ganymede, Europa, Io, Phobos, Deimos
	}

	// Major and minor axes (radius) of globes
	public static final double SEMI_MAJOR_AXIS[] = { 6378137, 3396200, 1738140, 2439700, 6051800, 71492000, 60268000,
		25559000, 24764000, 1186000, 603000, 106500, 2576000, 252100, 2634100, 1560800, 1821600, 11266.7, 6200 };
	public static final double SEMI_MINOR_AXIS[] = { 6356800, 3376200, 1735970, 2439700, 6051800, 66854000, 54364000,
		24973000, 24341000, 1186000, 603000, 106500, 2576000, 252100, 2634100, 1560800, 1821600, 11266.7, 6200 };

	// Projection names
	public static String[] coordTransformName = { "none", "TransverseMercator", "TransvMercatorModified_Alaska",
		"ObliqueMercator", "ObliqueMercatorLaborde", "ObliqueMercatorRosenmund", "ObliqueMercatorSpherical",
		"Mercator", "LambertConfConic2SP", "LambertConfConicHelmert", "LambertAzimEqualArea", "AlbersEqualArea",
		"AzimuthalEquidistant", "EquidistantConic", "Stereographic", "PolarStereographic", "ObliqueStereographic",
		"Equirectangular", "CassiniSoldner", "Gnomonic", "MillerCylindrical", "Orthographic", "Polyconic", "Robinson",
		"Sinusoidal", "VanDerGrinten", "NewZealandMapGrid", "TransvMercatorSouthOriented", "CylindricalEqualArea"};

	// This raster is projected
	public boolean projected;

	// The globe where the raster is found
	public String globe;

	// The upper left corner of the raster
	public double[] tiePoint;

	// The pixel scale of the raster
	public double[] scale;

	// The raster dimensions
	public int rasterWidth, rasterLength;

	// fields for an unprojected file
	public int gcsCode; // Geographic Coordinate System code, usually EPSG,
						// 32767 = user defined
	public int datumCode; // Datum code, usually EPSG, 32767 = user defined
	public int ellipsoidCode; // Ellipse code, 7000 - 7999 is EPSG, 32767 = user
								// defined
	public int primeMeridianCode; // Prime Meridian code, 8000 - 8999 is EPSG,
									// 32767 = user defined
	public double semiMajorAxis; // Datum semi-major axis
	public double semiMinorAxis; // Datum semi-minor axis
	public double inverseFlattening; // Inverse flattening ratio
	public double gcsPrimeMeridianLon; // Prime meridian longitude
	public String gcsCitation; // documentation

	// fields for a projected file
	// If not set, some of these are maintained as NaN
	public int pcsCode; // Projected Coordinate System code, usually EPSG, 32767
						// = user defined
	public int projCode; // Projection code, usually EPSG, 32767 = user defined
	public int coordTransformCode; // Coordinate transformation code, not EPSG,
									// 32767 = user defined
	public double stdParallel1, stdParallel2; // Standard parallels
	public double naturalOriginLon, naturalOriginLat; // Natural origin
	public double falseEasting, falseNorthing; // False easting and northing
	public double falseOriginLon, falseOriginLat; // False origin
	public double centerEasting, centerNorthing; // Center easting and northing
	public double centerLon, centerLat; // Center longitude and latitude
	public double scaleAtNaturalOrigin, scaleAtCenter; // Scale factor at
														// natural origin and
														// center
	public double azimuth, straightVertPoleLon;
	public String pcsCitation; // documentation
	public String projLinearUnits;
	
	public int poleLat;

	/**
	 * Create a default projection
	 * 
	 * @param rasterWidth
	 * @param rasterLength
	 * @param pixelScale
	 * @return
	 */
	public static ProjectionInfo createDefault(int rasterWidth, int rasterLength, double pixelScale) {
		ProjectionInfo projInfo = new ProjectionInfo();
		projInfo.projCode = GeoKey.Code_CT_Orthographic;
		projInfo.coordTransformCode = GeoKey.Code_CT_Orthographic;
		projInfo.tiePoint = new double[] { -pixelScale * rasterWidth / 2, pixelScale * rasterLength / 2, 0 };
		projInfo.scale = new double[] { pixelScale, pixelScale, 1 };
		projInfo.centerLat = 0;
		projInfo.centerLon = 0;
		projInfo.naturalOriginLon = 0;
		projInfo.naturalOriginLat = 0;
		projInfo.falseEasting = 0;
		projInfo.falseNorthing = 0;
		projInfo.globe = "Earth";
		projInfo.rasterWidth = rasterWidth;
		projInfo.rasterLength = rasterLength;
		projInfo.projected = true;
		projInfo.projLinearUnits = "meter";
		return (projInfo);
	}

	/**
	 * Constructor
	 * 
	 * Fields are initialized to NaN
	 */
	public ProjectionInfo() {
		semiMajorAxis = Double.NaN;
		semiMinorAxis = Double.NaN;
		inverseFlattening = Double.NaN;
		gcsPrimeMeridianLon = Double.NaN;

		stdParallel1 = Double.NaN;
		stdParallel2 = Double.NaN;
		naturalOriginLon = Double.NaN;
		naturalOriginLat = Double.NaN;
		falseOriginLon = Double.NaN;
		falseOriginLat = Double.NaN;
		falseEasting = Double.NaN;
		falseNorthing = Double.NaN;
		centerEasting = Double.NaN;
		centerNorthing = Double.NaN;
		centerLon = Double.NaN;
		centerLat = Double.NaN;
		scaleAtNaturalOrigin = Double.NaN;
		scaleAtCenter = Double.NaN;
		azimuth = Double.NaN;
		straightVertPoleLon = Double.NaN;
		poleLat = 90;
	}

	/**
	 * Copy constructor
	 * 
	 * @param that
	 */
	public ProjectionInfo(ProjectionInfo that) {
		this.projected = that.projected;
		if (that.tiePoint != null) {
			this.tiePoint = new double[that.tiePoint.length];
			System.arraycopy(that.tiePoint, 0, this.tiePoint, 0, that.tiePoint.length);
		}
		if (that.scale != null) {
			this.scale = new double[that.scale.length];
			System.arraycopy(that.scale, 0, this.scale, 0, that.scale.length);
		}
		this.rasterWidth = that.rasterWidth;
		this.rasterLength = that.rasterLength;
		this.globe = that.globe;
		this.gcsCode = that.gcsCode;
		this.datumCode = that.datumCode;
		this.ellipsoidCode = that.ellipsoidCode;
		this.primeMeridianCode = that.primeMeridianCode;
		this.semiMajorAxis = that.semiMajorAxis;
		this.semiMinorAxis = that.semiMinorAxis;
		this.inverseFlattening = that.inverseFlattening;
		this.gcsPrimeMeridianLon = that.gcsPrimeMeridianLon;
		this.gcsCitation = that.gcsCitation;

		this.pcsCode = that.pcsCode;
		this.projCode = that.projCode;
		this.coordTransformCode = that.coordTransformCode;
		this.stdParallel1 = that.stdParallel1;
		this.stdParallel2 = that.stdParallel2;
		this.naturalOriginLon = that.naturalOriginLon;
		this.naturalOriginLat = that.naturalOriginLat;
		this.falseOriginLon = that.falseOriginLon;
		this.falseOriginLat = that.falseOriginLat;
		this.falseEasting = that.falseEasting;
		this.falseNorthing = that.falseNorthing;
		this.centerEasting = that.centerEasting;
		this.centerNorthing = that.centerNorthing;
		this.centerLon = that.centerLon;
		this.centerLat = that.centerLat;
		this.scaleAtNaturalOrigin = that.scaleAtNaturalOrigin;
		this.scaleAtCenter = that.scaleAtCenter;
		this.azimuth = that.azimuth;
		this.straightVertPoleLon = that.straightVertPoleLon;
		this.pcsCitation = that.pcsCitation;
		this.projLinearUnits = that.projLinearUnits;
		this.poleLat = that.poleLat;
	}

	/**
	 * Load contents from a properties object
	 * 
	 * @param properties
	 */
	public void loadFromProperties(Properties properties) {
		projected = StringUtil.getBooleanValue(properties, "ProjectionInfo.Projected", false, true);
		tiePoint = StringUtil.getDoubleArray(properties, "ProjectionInfo.TiePoint", null, true);
		scale = StringUtil.getDoubleArray(properties, "ProjectionInfo.Scale", null, true);
		globe = StringUtil.getStringValue(properties, "ProjectionInfo.Globe", null, true);
		rasterWidth = StringUtil.getIntegerValue(properties, "ProjectionInfo.RasterWidth", true, 0, true);
		try {
			rasterLength = StringUtil.getIntegerValue(properties, "ProjectionInfo.RasterLength", true, 0, true);
		}
		catch (Exception e) {
			rasterLength = StringUtil.getIntegerValue(properties, "ProjectionInfo.RasterHeight", true, 0, true);
		}

		datumCode = StringUtil.getIntegerValue(properties, "ProjectionInfo.DatumCode", true, 0, false);
		ellipsoidCode = StringUtil.getIntegerValue(properties, "ProjectionInfo.DatumCode", true, 0, false);
		semiMajorAxis = StringUtil.getDoubleValue(properties, "ProjectionInfo.SemiMajorAxis", true, Double.NaN, false);
		semiMinorAxis = StringUtil.getDoubleValue(properties, "ProjectionInfo.SemiMinorAxis", true, Double.NaN, false);
		inverseFlattening = StringUtil.getDoubleValue(properties, "ProjectionInfo.InverseFlattening", false,
			Double.NaN, false);
		gcsPrimeMeridianLon = StringUtil.getDoubleValue(properties, "ProjectionInfo.GCSPrimeMeridianLon", false,
			Double.NaN, false);

		projCode = StringUtil.getIntegerValue(properties, "ProjectionInfo.ProjectionCode", true, 0, false);
		pcsCode = StringUtil.getIntegerValue(properties, "ProjectionInfo.ProjectionCoordinateSystemCode", true, 0,
			false);
		coordTransformCode = StringUtil
			.getIntegerValue(properties, "ProjectionInfo.CoordTransformCode", true, 0, false);
		stdParallel1 = StringUtil.getDoubleValue(properties, "ProjectionInfo.StdParallel1", false, Double.NaN, false);
		poleLat = (int)(Math.signum(stdParallel1)*90);
		stdParallel2 = StringUtil.getDoubleValue(properties, "ProjectionInfo.StdParallel2", false, Double.NaN, false);
		naturalOriginLon = StringUtil.getDoubleValue(properties, "ProjectionInfo.NaturalOriginLon", false, Double.NaN,
			false);
		naturalOriginLat = StringUtil.getDoubleValue(properties, "ProjectionInfo.NaturalOriginLat", false, Double.NaN,
			false);
		falseOriginLon = StringUtil.getDoubleValue(properties, "ProjectionInfo.FalseOriginLon", false, Double.NaN,
			false);
		falseOriginLat = StringUtil.getDoubleValue(properties, "ProjectionInfo.FalseOriginLat", false, Double.NaN,
			false);
		falseEasting = StringUtil.getDoubleValue(properties, "ProjectionInfo.FalseEasting", false, Double.NaN, false);
		falseNorthing = StringUtil.getDoubleValue(properties, "ProjectionInfo.FalseNorthing", false, Double.NaN, false);
		centerEasting = StringUtil.getDoubleValue(properties, "ProjectionInfo.CenterEasting", false, Double.NaN, false);
		centerNorthing = StringUtil.getDoubleValue(properties, "ProjectionInfo.CenterNorthing", false, Double.NaN,
			false);
		centerLon = StringUtil.getDoubleValue(properties, "ProjectionInfo.CenterLon", false, Double.NaN, false);
		centerLat = StringUtil.getDoubleValue(properties, "ProjectionInfo.CenterLat", false, Double.NaN, false);
		scaleAtNaturalOrigin = StringUtil.getDoubleValue(properties, "ProjectionInfo.ScaleAtNaturalOrigin", false,
			Double.NaN, false);
		scaleAtCenter = StringUtil.getDoubleValue(properties, "ProjectionInfo.ScaleAtCenter", false, Double.NaN, false);
		azimuth = StringUtil.getDoubleValue(properties, "ProjectionInfo.Azimuth", false, Double.NaN, false);
		straightVertPoleLon = StringUtil.getDoubleValue(properties, "ProjectionInfo.StraightVertPoleLon", false,
			Double.NaN, false);

		gcsCitation = StringUtil.getStringValue(properties, "ProjectionInfo.GCSCitation", null, false);
		pcsCitation = StringUtil.getStringValue(properties, "ProjectionInfo.PCSCitation", null, false);

		projLinearUnits = StringUtil.getStringValue(properties, "ProjectionInfo.ProjLinearUnits", "meter", false);
		
		// Hack for Web Mercator confusion
		if (coordTransformCode == GeoKey.Code_Undefined) {
			String citation = (pcsCitation == null ? "" : pcsCitation.toLowerCase());
			if (citation.contains("mercator") && (citation.contains("web") || citation.contains("pseudo") || citation.contains("global"))) {
				coordTransformCode = GeoKey.Code_CT_Mercator;
				if (Double.isNaN(naturalOriginLon))
					naturalOriginLon = 0;
				if (Double.isNaN(falseEasting))
					falseEasting = 0;
				if (Double.isNaN(falseNorthing))
					falseNorthing = 0;
				if (citation.contains("auxiliary") && citation.contains("sphere")) {
					if (!Double.isNaN(semiMajorAxis))
						semiMinorAxis = semiMajorAxis;
					else {
						semiMajorAxis = SEMI_MAJOR_AXIS[0];
						semiMinorAxis = SEMI_MINOR_AXIS[0];
					}
				}					
			}
		}
	}

	/**
	 * Save contents to a properties object.
	 * 
	 * @param properties
	 */
	public void saveToProperties(Properties properties, String defaultGlobe) {
		properties.setProperty("ProjectionInfo.Projected", Boolean.toString(projected));
		properties.setProperty("ProjectionInfo.TiePoint", StringUtil.doubleArrayToString(tiePoint));
		properties.setProperty("ProjectionInfo.Scale", StringUtil.doubleArrayToString(scale));
		if (globe == null) {
			globe = defaultGlobe;
		}
		properties.setProperty("ProjectionInfo.Globe", globe);
		properties.setProperty("ProjectionInfo.RasterWidth", Integer.toString(rasterWidth));
		properties.setProperty("ProjectionInfo.RasterLength", Integer.toString(rasterLength));
		properties.setProperty("ProjectionInfo.ProjLinearUnits", projLinearUnits);

		if (datumCode > 0) {
			properties.setProperty("ProjectionInfo.DatumCode", Integer.toString(datumCode));
		}
		if (ellipsoidCode > 0) {
			properties.setProperty("ProjectionInfo.EllipsoidCode", Integer.toString(ellipsoidCode));
		}
		if (!Double.isNaN(semiMajorAxis)) {
			properties.setProperty("ProjectionInfo.SemiMajorAxis", Double.toString(semiMajorAxis));
		}
		if (!Double.isNaN(semiMinorAxis)) {
			properties.setProperty("ProjectionInfo.SemiMinorAxis", Double.toString(semiMinorAxis));
		}
		if (!Double.isNaN(inverseFlattening)) {
			properties.setProperty("ProjectionInfo.InverseFlattening", Double.toString(inverseFlattening));
		}
		if (!Double.isNaN(gcsPrimeMeridianLon)) {
			properties.setProperty("ProjectionInfo.GCSPrimeMeridianLon", Double.toString(gcsPrimeMeridianLon));
		}

		if (projCode > 0) {
			properties.setProperty("ProjectionInfo.ProjectionCode", Integer.toString(projCode));
		}
		if (pcsCode > 0) {
			properties.setProperty("ProjectionInfo.ProjectionCoordinateSystemCode", Integer.toString(pcsCode));
		}
		if (coordTransformCode > 0) {
			properties.setProperty("ProjectionInfo.CoordTransformCode", Integer.toString(coordTransformCode));
		}
		if (!Double.isNaN(stdParallel1)) {
			properties.setProperty("ProjectionInfo.StdParallel1", Double.toString(stdParallel1));
		}
		if (!Double.isNaN(stdParallel2)) {
			properties.setProperty("ProjectionInfo.StdParallel2", Double.toString(stdParallel2));
		}
		if (!Double.isNaN(naturalOriginLon)) {
			properties.setProperty("ProjectionInfo.NaturalOriginLon", Double.toString(naturalOriginLon));
		}
		if (!Double.isNaN(naturalOriginLat)) {
			properties.setProperty("ProjectionInfo.NaturalOriginLat", Double.toString(naturalOriginLat));
		}
		if (!Double.isNaN(falseOriginLon)) {
			properties.setProperty("ProjectionInfo.FalseOriginLon", Double.toString(falseOriginLon));
		}
		if (!Double.isNaN(falseOriginLat)) {
			properties.setProperty("ProjectionInfo.FalseOriginLat", Double.toString(falseOriginLat));
		}
		if (!Double.isNaN(falseEasting)) {
			properties.setProperty("ProjectionInfo.FalseEasting", Double.toString(falseEasting));
		}
		if (!Double.isNaN(falseNorthing)) {
			properties.setProperty("ProjectionInfo.FalseNorthing", Double.toString(falseNorthing));
		}
		if (!Double.isNaN(centerEasting)) {
			properties.setProperty("ProjectionInfo.CenterEasting", Double.toString(centerEasting));
		}
		if (!Double.isNaN(centerNorthing)) {
			properties.setProperty("ProjectionInfo.CenterNorthing", Double.toString(centerNorthing));
		}
		if (!Double.isNaN(centerLon)) {
			properties.setProperty("ProjectionInfo.CenterLon", Double.toString(centerLon));
		}
		if (!Double.isNaN(centerLat)) {
			properties.setProperty("ProjectionInfo.CenterLat", Double.toString(centerLat));
		}
		if (!Double.isNaN(scaleAtNaturalOrigin)) {
			properties.setProperty("ProjectionInfo.ScaleAtNaturalOrigin", Double.toString(scaleAtNaturalOrigin));
		}
		if (!Double.isNaN(scaleAtCenter)) {
			properties.setProperty("ProjectionInfo.ScaleAtCenter", Double.toString(scaleAtCenter));
		}
		if (!Double.isNaN(azimuth)) {
			properties.setProperty("ProjectionInfo.Azimuth", Double.toString(azimuth));
		}
		if (!Double.isNaN(straightVertPoleLon)) {
			properties.setProperty("ProjectionInfo.StraightVertPoleLon", Double.toString(straightVertPoleLon));
		}

		if (gcsCitation != null) {
			properties.setProperty("ProjectionInfo.GCSCitation", gcsCitation);
		}
		if (pcsCitation != null) {
			properties.setProperty("ProjectionInfo.PCSCitation", pcsCitation);
		}
	}

	@Override
	public String toString() {
		String str = "Projection Info:\n";
		str += "    Projected = " + projected + "\n" + "    TiePoint = " + StringUtil.doubleArrayToString(tiePoint)
			+ "\n" + "    Scale = " + StringUtil.doubleArrayToString(scale) + "\n" + "    Globe = " + globe + "\n"
			+ "    Raster Width = " + rasterWidth + "\n" + "    Raster Length = " + rasterLength + "\n";
		if (projected) {
			str += "    Units = " + projLinearUnits + "\n";
		}
		if (datumCode > 0) {
			str += "    DatumCode = " + datumCode + "\n";
		}
		if (ellipsoidCode > 0) {
			str += "    EllipsoidCode = " + ellipsoidCode + "\n";
		}
		if (!Double.isNaN(semiMajorAxis)) {
			str += "    SemiMajorAxis = " + semiMajorAxis + "\n";
		}
		if (!Double.isNaN(semiMinorAxis)) {
			str += "    SemiMinorAxis = " + semiMinorAxis + "\n";
		}
		if (!Double.isNaN(inverseFlattening)) {
			str += "    InverseFlattening = " + inverseFlattening + "\n";
		}
		if (!Double.isNaN(gcsPrimeMeridianLon)) {
			str += "    GCSPrimeMeridianLon = " + gcsPrimeMeridianLon + "\n";
		}

		if (projCode > 0) {
			str += "    ProjectionCode = " + projCode + "\n";
		}
		if (pcsCode > 0) {
			str += "    ProjectionCoordinateSystemCode = " + pcsCode + "\n";
		}
		str += "    CoordTransformCode = " + findCoordTransformName(coordTransformCode) + "\n";
		if (!Double.isNaN(stdParallel1)) {
			str += "    StdParallel1 = " + stdParallel1 + "\n";
		}
		if (!Double.isNaN(stdParallel2)) {
			str += "    StdParallel2 = " + stdParallel2 + "\n";
		}
		if (!Double.isNaN(naturalOriginLon)) {
			str += "    NaturalOriginLon = " + naturalOriginLon + "\n";
		}
		if (!Double.isNaN(naturalOriginLat)) {
			str += "    NaturalOriginLat = " + naturalOriginLat + "\n";
		}
		if (!Double.isNaN(falseOriginLon)) {
			str += "    FalseOriginLon = " + falseOriginLon + "\n";
		}
		if (!Double.isNaN(falseOriginLat)) {
			str += "    FalseOriginLat = " + falseOriginLat + "\n";
		}
		if (!Double.isNaN(falseEasting)) {
			str += "    FalseEasting = " + falseEasting + "\n";
		}
		if (!Double.isNaN(falseNorthing)) {
			str += "    FalseNorthing = " + falseNorthing + "\n";
		}
		if (!Double.isNaN(centerEasting)) {
			str += "    CenterEasting = " + centerEasting + "\n";
		}
		if (!Double.isNaN(centerNorthing)) {
			str += "    CenterNorthing = " + centerNorthing + "\n";
		}
		if (!Double.isNaN(centerLon)) {
			str += "    CenterLon = " + centerLon + "\n";
		}
		if (!Double.isNaN(centerLat)) {
			str += "    CenterLat = " + centerLat + "\n";
		}
		if (!Double.isNaN(scaleAtNaturalOrigin)) {
			str += "    ScaleAtNaturalOrigin = " + scaleAtNaturalOrigin + "\n";
		}
		if (!Double.isNaN(scaleAtCenter)) {
			str += "    ScaleAtCenter = " + scaleAtCenter + "\n";
		}
		if (!Double.isNaN(azimuth)) {
			str += "    Azimuth = " + azimuth + "\n";
		}
		if (!Double.isNaN(straightVertPoleLon)) {
			str += "    StraightVertPoleLon = " + straightVertPoleLon + "\n";
		}

		if (gcsCitation != null) {
			str += "    GCSCitation = " + gcsCitation + "\n";
		}
		if (pcsCitation != null) {
			str += "    PCSCitation = " + pcsCitation + "\n";
		}
		return (str);
	}

	/**
	 * Get the bounds of this raster
	 * 
	 * @return
	 */
	public double[] getBounds() {
		double[] bounds = new double[4];
		bounds[0] = tiePoint[0];
		bounds[1] = tiePoint[1] - rasterLength * scale[1];
		bounds[2] = tiePoint[0] + rasterWidth * scale[0];
		bounds[3] = tiePoint[1];

		// TODO: check for crossing 360/0 and 180/-180 boundaries
		return (bounds);
	}

	/**
	 * Get the map resolution
	 * 
	 * @return
	 */
	public double[] getMapResolution() {
		double[] resolution = new double[scale.length];
		double circumference = 2 * Math.PI * semiMajorAxis;
		resolution[0] = (360 * scale[0]) / circumference;
		resolution[1] = (360 * scale[1]) / circumference;
		resolution[2] = 1;
		return (resolution);
	}

	/**
	 * Get the Proj4 command string
	 * 
	 * @return
	 */
	public String getProj4String() {
		String proj = null;
		double falseE = falseEasting;
		if (Double.isNaN(falseE))
			falseE = 0;
		double falseN = falseNorthing;
		if (Double.isNaN(falseN))
			falseN = 0;
		if (coordTransformCode != 0) {
			switch (coordTransformCode) {
			case GeoKey.Code_CT_AlbersEqualArea:
				proj = "+proj=aea" + " +lat_1=" + stdParallel1 + " +lat_2=" + stdParallel2 + " +lat_0="
					+ naturalOriginLat + " +lon_0=" + naturalOriginLon + " +x_0=" + falseE + " +y_0="
					+ falseN;
				break;
			case GeoKey.Code_CT_AzimuthalEquidistant:
				proj = "+proj=aeqd" + " +lat_0=" + centerLat + " +lon_0=" + centerLon + " +x_0=" + falseE
					+ " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_CassiniSoldner:
				proj = "+proj=cass" + " +lat_0=" + naturalOriginLat + " +lon_0=" + naturalOriginLon + " +x_0="
					+ falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_CylindricalEqualArea:
				proj = "+proj=cea" + " +lon_0=" + naturalOriginLon + " +lat_ts=" + stdParallel1 + " +x_0="
					+ falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_EquidistantConic:
				proj = "+proj=eqdc" + " +lat_1=" + stdParallel1 + " +lat_2=" + stdParallel2 + " +lat_0="
					+ naturalOriginLat + " +lon_0=" + naturalOriginLon + " +x_0=" + falseE + " +y_0="
					+ falseN;
				break;
			case GeoKey.Code_CT_Equirectangular:
				proj = "+proj=eqc" + " +lat_0=0 +lat_ts=" + centerLat + " +lon_0=" + centerLon + " +x_0=" + falseE
					+ " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_TransverseMercator:
				proj = "+proj=tmerc" + " +lat_0=" + naturalOriginLat + " +lon_0=" + naturalOriginLon + " +k="
					+ scaleAtNaturalOrigin + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_Gnomonic:
				proj = "+proj=gnom" + " +lat_0=" + centerLat + " +lon_0=" + centerLon + " +x_0=" + falseE
					+ " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_ObliqueMercator:
				proj = "+proj=omerc" + " +lat_0=" + centerLat + " +lonc=" + centerLon + " +alpha=" + azimuth + " +k_0="
					+ scaleAtCenter + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_LambertAzimEqualArea:
				proj = "+proj=laea" + " +lat_0=" + centerLat + " +lon_0=" + centerLon + " +x_0=" + falseE
					+ " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_LambertConfConic_Helmert:
				proj = "+proj=lcc" + " +lat_0=" + naturalOriginLat + " +lon_0=" + naturalOriginLon + " +k="
					+ scaleAtNaturalOrigin + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_LambertConfConic_2SP:
				double lon0 = naturalOriginLon;
				if (Double.isNaN(lon0))
					lon0 = falseOriginLon;
				if (Double.isNaN(lon0))
					lon0 = 0;
				proj = "+proj=lcc" + " +lat_1=" + stdParallel1 + " +lat_2=" + stdParallel2 + " +lat_0="
					+ falseOriginLat + " +lon_0=" + lon0 + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_Mercator:
				proj = "+proj=merc" + " +lon_0=" + naturalOriginLon + " +x_0=" + falseE + " +y_0=" + falseN;
				if (!Double.isNaN(stdParallel1))
					proj += " +lat_ts="+stdParallel1;
				else if (!Double.isNaN(scaleAtNaturalOrigin))
					proj += " +k_0=" + scaleAtNaturalOrigin;
				else
					proj += " +lat_ts=0.0";					
				break;
			case GeoKey.Code_CT_MillerCylindrical:
				proj = "+proj=mill" + " +lat_0=" + centerLat + " +lon_0=" + centerLon + " +x_0=" + falseE
					+ " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_NewZealandMapGrid:
				proj = "+proj=nzmg" + " +lat_0=" + centerLat + " +lon_0=" + centerLon + " +x_0=" + falseE
					+ " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_ObliqueStereographic:
				proj = "+proj=sterea" + " +lat_0=" + naturalOriginLat + " +lon_0=" + naturalOriginLon + " +k="
					+ scaleAtNaturalOrigin + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_Orthographic:
				proj = "+proj=ortho" + " +lat_0=" + centerLat + " +lon_0=" + centerLon + " +x_0=" + falseE
					+ " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_PolarStereographic:
				proj = "+proj=stere" + " +lat_ts=" + naturalOriginLat + " +lat_0="+poleLat + " +lon_0=" + naturalOriginLon
					+ " +k_0=" + scaleAtNaturalOrigin + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_Polyconic:
				proj = "+proj=stere" + " +lat_0=" + naturalOriginLat + " +lon_0=" + naturalOriginLon + " +x_0="
					+ falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_Robinson:
				proj = "+proj=stere" + " +lon_0=" + centerLon + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_Sinusoidal:
				proj = "+proj=sinu" + " +lon_0=" + centerLon + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			case GeoKey.Code_CT_VanDerGrinten:
				proj = "+proj=vandg" + " +lon_0=" + centerLon + " +x_0=" + falseE + " +y_0=" + falseN;
				break;
			}
			if (proj != null)
				proj += " +a=" + getSemiMajorAxis()+" +b=" + getSemiMinorAxis()+" +no_defs";
		}
		if ((proj == null) && (pcsCode != GeoKey.Code_Undefined) && (pcsCode != GeoKey.Code_UserDefined)) {
			proj = "+init=epsg:" + pcsCode;
		}
		return (proj);
	}

	/**
	 * Given the name, find the coordinate transform code
	 * 
	 * @param name
	 * @return
	 */
	public int findCoordTransformCode(String name) {
		name = name.toLowerCase();
		name = name.replace(" ", "");
		for (int i = 0; i < coordTransformName.length; ++i) {
			if (name.equals(coordTransformName[i].toLowerCase())) {
				return (i);
			}
		}
		return (0);
	}

	/**
	 * Given the code, find the coordinate transform name
	 * 
	 * @param code
	 * @return
	 */
	public String findCoordTransformName(int code) {
		if ((code < 0) || (code >= coordTransformName.length)) {
			return (null);
		}
		return (coordTransformName[code]);
	}

	/**
	 * Get the semi-major axis
	 * 
	 * @return
	 */
	public double getSemiMajorAxis() {
		if (Double.isNaN(semiMajorAxis)) {
			if (!Double.isNaN(semiMinorAxis) && !Double.isNaN(inverseFlattening)) {
				semiMajorAxis = semiMinorAxis / (1 - 1 / inverseFlattening);
			} else {
				GlobeName gName = GlobeName.valueOf(globe);
				semiMajorAxis = SEMI_MAJOR_AXIS[gName.ordinal()];
			}
		}
		return (semiMajorAxis);
	}

	/**
	 * Get the semi-minor axis
	 * 
	 * @return
	 */
	public double getSemiMinorAxis() {
		if (Double.isNaN(semiMinorAxis)) {
			if (!Double.isNaN(semiMajorAxis) && !Double.isNaN(inverseFlattening)) {
				semiMinorAxis = semiMajorAxis * (1 - 1 / inverseFlattening);
			} else {
				GlobeName gName = GlobeName.valueOf(globe);
				semiMinorAxis = SEMI_MINOR_AXIS[gName.ordinal()];
			}
		}
		return (semiMinorAxis);
	}

}
