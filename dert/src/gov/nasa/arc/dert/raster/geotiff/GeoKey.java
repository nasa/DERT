package gov.nasa.arc.dert.raster.geotiff;

import java.util.HashMap;

/**
 * Defines enumerations for use with GeoTIFF GeoKeys.
 *
 */
public class GeoKey implements Comparable<GeoKey> {

	public static final int Code_GeoKeyDirectoryTag = 34735;
	public static final int Code_GeoDoubleParamsTag = 34736;
	public static final int Code_GeoAsciiParamsTag = 34737;

	public static final int Code_ModelType = 1024;
	public static final int Code_RasterType = 1025;
	public static final int Code_Citation = 1026;
	public static final int Code_GeographicType = 2048;
	public static final int Code_GeogCitation = 2049;
	public static final int Code_GeogGeodeticDatum = 2050;
	public static final int Code_GeogPrimeMeridian = 2051;
	public static final int Code_GeogLinearUnits = 2052;
	public static final int Code_GeogLinearUnitSize = 2053;
	public static final int Code_GeogAngularUnits = 2054;
	public static final int Code_GeogAngularUnitSize = 2055;
	public static final int Code_GeogEllipsoid = 2056;
	public static final int Code_GeogSemiMajorAxis = 2057;
	public static final int Code_GeogSemiMinorAxis = 2058;
	public static final int Code_GeogInvFlattening = 2059;
	public static final int Code_GeogAzimuthUnits = 2060;
	public static final int Code_GeogPrimeMeridianLong = 2061;
	public static final int Code_ProjectedCSType = 3072;
	public static final int Code_PCSCitation = 3073;
	public static final int Code_Projection = 3074;
	public static final int Code_ProjCoordTrans = 3075;
	public static final int Code_ProjLinearUnits = 3076;
	public static final int Code_ProjLinearUnitSize = 3077;
	public static final int Code_ProjStdParallel1 = 3078;
	public static final int Code_ProjStdParallel2 = 3079;
	public static final int Code_ProjNatOriginLong = 3080;
	public static final int Code_ProjNatOriginLat = 3081;
	public static final int Code_ProjFalseEasting = 3082;
	public static final int Code_ProjFalseNorthing = 3083;
	public static final int Code_ProjFalseOriginLong = 3084;
	public static final int Code_ProjFalseOriginLat = 3085;
	public static final int Code_ProjFalseOriginEasting = 3086;
	public static final int Code_ProjFalseOriginNorthing = 3087;
	public static final int Code_ProjCenterLong = 3088;
	public static final int Code_ProjCenterLat = 3089;
	public static final int Code_ProjCenterEasting = 3090;
	public static final int Code_ProjCenterNorthing = 3091;
	public static final int Code_ProjScaleAtNatOrigin = 3092;
	public static final int Code_ProjScaleAtCenter = 3093;
	public static final int Code_ProjAzimuthAngle = 3094;
	public static final int Code_ProjStraightVertPoleLong = 3095;
	public static final int Code_VerticalCSType = 4096;
	public static final int Code_VerticalCitation = 4097;
	public static final int Code_VerticalDatum = 4098;
	public static final int Code_VerticalUnits = 4099;

	public static final int Code_Angular_Radian = 9101;
	public static final int Code_Angular_Degree = 9102;
	public static final int Code_Angular_Arc_Minute = 9103;
	public static final int Code_Angular_Arc_Second = 9104;
	public static final int Code_Angular_Grad = 9105;
	public static final int Code_Angular_Gon = 9106;
	public static final int Code_Angular_DMS = 9107;
	public static final int Code_Angular_DMS_Hemisphere = 9108;

	public static final int Code_ModelTypeUndefined = 0;
	public static final int Code_ModelTypeProjected = 1;
	public static final int Code_ModelTypeGeographic = 2;
	public static final int Code_ModelTypeGeocentric = 3;

	public static final int Code_RasterPixelIsArea = 1;
	public static final int Code_RasterPixelIsPoint = 2;

	public static final int Code_Linear_Undefined = 0;
	public static final int Code_Linear_Meter = 9001;
	public static final int Code_Linear_Foot = 9002;
	public static final int Code_Linear_Foot_US_Survey = 9003;
	public static final int Code_Linear_Foot_Modified_American = 9004;
	public static final int Code_Linear_Foot_Clarke = 9005;
	public static final int Code_Linear_Foot_Indian = 9006;
	public static final int Code_Linear_Link = 9007;
	public static final int Code_Linear_Link_Benoit = 9008;
	public static final int Code_Linear_Link_Sears = 9009;
	public static final int Code_Linear_Chain_Benoit = 9010;
	public static final int Code_Linear_Chain_Sears = 9011;
	public static final int Code_Linear_Yard_Sears = 9012;
	public static final int Code_Linear_Yard_Indian = 9013;
	public static final int Code_Linear_Fathom = 9014;
	public static final int Code_Linear_Mile_International_Nautical = 9015;

	public static final int Code_CT_TransverseMercator = 1;
	public static final int Code_CT_TransvMercator_Modified_Alaska = 2;
	public static final int Code_CT_ObliqueMercator = 3;
	public static final int Code_CT_ObliqueMercator_Laborde = 4;
	public static final int Code_CT_ObliqueMercator_Rosenmund = 5;
	public static final int Code_CT_ObliqueMercator_Spherical = 6;
	public static final int Code_CT_Mercator = 7;
	public static final int Code_CT_LambertConfConic_2SP = 8;
	public static final int Code_CT_LambertConfConic_Helmert = 9;
	public static final int Code_CT_LambertAzimEqualArea = 10;
	public static final int Code_CT_AlbersEqualArea = 11;
	public static final int Code_CT_AzimuthalEquidistant = 12;
	public static final int Code_CT_EquidistantConic = 13;
	public static final int Code_CT_Stereographic = 14;
	public static final int Code_CT_PolarStereographic = 15;
	public static final int Code_CT_ObliqueStereographic = 16;
	public static final int Code_CT_Equirectangular = 17;
	public static final int Code_CT_CassiniSoldner = 18;
	public static final int Code_CT_Gnomonic = 19;
	public static final int Code_CT_MillerCylindrical = 20;
	public static final int Code_CT_Orthographic = 21;
	public static final int Code_CT_Polyconic = 22;
	public static final int Code_CT_Robinson = 23;
	public static final int Code_CT_Sinusoidal = 24;
	public static final int Code_CT_VanDerGrinten = 25;
	public static final int Code_CT_NewZealandMapGrid = 26;
	public static final int Code_CT_TransvMercator_SouthOriented = 27;
	public static final int Code_CT_CylindricalEqualArea = 28;

	public static final int Code_GCS_WGS_84 = 4326;
	public static final int Code_GCS_NAD_83 = 4269;
	public static final int Code_Datum_WGS84 = 6326;
	public static final int Code_UserDefined = 32767;

	public static final int[] idCode = { Code_ModelType, Code_RasterType, Code_Citation,

	Code_GeographicType, Code_GeogCitation, Code_GeogGeodeticDatum, Code_GeogPrimeMeridian, Code_GeogLinearUnits,
		Code_GeogLinearUnitSize, Code_GeogAngularUnits, Code_GeogAngularUnitSize, Code_GeogEllipsoid,
		Code_GeogSemiMajorAxis, Code_GeogSemiMinorAxis, Code_GeogInvFlattening, Code_GeogAzimuthUnits,
		Code_GeogPrimeMeridianLong,

		Code_ProjectedCSType, Code_PCSCitation, Code_Projection, Code_ProjCoordTrans, Code_ProjLinearUnits,
		Code_ProjLinearUnitSize, Code_ProjStdParallel1, Code_ProjStdParallel2, Code_ProjNatOriginLong,
		Code_ProjNatOriginLat, Code_ProjFalseEasting, Code_ProjFalseNorthing, Code_ProjFalseOriginLong,
		Code_ProjFalseOriginLat, Code_ProjFalseOriginEasting, Code_ProjFalseOriginNorthing, Code_ProjCenterLong,
		Code_ProjCenterLat, Code_ProjCenterEasting, Code_ProjCenterNorthing, Code_ProjScaleAtNatOrigin,
		Code_ProjScaleAtCenter, Code_ProjAzimuthAngle, Code_ProjStraightVertPoleLong,

		Code_VerticalCSType, Code_VerticalCitation, Code_VerticalDatum, Code_VerticalUnits, };

	public static enum KeyID {
		ModelType, RasterType, Citation,

		GeographicType, GeogCitation, GeogGeodeticDatum, GeogPrimeMeridian, GeogLinearUnits, GeogLinearUnitSize, GeogAngularUnits, GeogAngularUnitSize, GeogEllipsoid, GeogSemiMajorAxis, GeogSemiMinorAxis, GeogInvFlattening, GeogAzimuthUnits, GeogPrimeMeridianLong,

		ProjectedCSType, PCSCitation, Projection, ProjCoordTrans, ProjLinearUnits, ProjLinearUnitSize, ProjStdParallel1, ProjStdParallel2, ProjNatOriginLong, ProjNatOriginLat, ProjFalseEasting, ProjFalseNorthing, ProjFalseOriginLong, ProjFalseOriginLat, ProjFalseOriginEasting, ProjFalseOriginNorthing, ProjCenterLong, ProjCenterLat, ProjCenterEasting, ProjCenterNorthing, ProjScaleAtNatOrigin, ProjScaleAtCenter, ProjAzimuthAngle, ProjStraightVertPoleLong,

		VerticalCSType, VerticalCitation, VerticalDatum, VerticalUnits,
	}

	public static final int angularUnitsCode[] = { Code_Angular_Radian, Code_Angular_Degree, Code_Angular_Arc_Minute,
		Code_Angular_Arc_Second, Code_Angular_Grad, Code_Angular_Gon, Code_Angular_DMS, Code_Angular_DMS_Hemisphere };

	public static final int modelTypeCode[] = { Code_ModelTypeUndefined, Code_ModelTypeProjected,
		Code_ModelTypeGeographic, Code_ModelTypeGeocentric };

	public static final int rasterTypeCode[] = { Code_RasterPixelIsArea, Code_RasterPixelIsPoint };

	public static final int linearUnitsCode[] = { Code_Linear_Undefined, Code_Linear_Meter, Code_Linear_Foot,
		Code_Linear_Foot_US_Survey, Code_Linear_Foot_Modified_American, Code_Linear_Foot_Clarke,
		Code_Linear_Foot_Indian, Code_Linear_Link, Code_Linear_Link_Benoit, Code_Linear_Link_Sears,
		Code_Linear_Chain_Benoit, Code_Linear_Chain_Sears, Code_Linear_Yard_Sears, Code_Linear_Yard_Indian,
		Code_Linear_Fathom, Code_Linear_Mile_International_Nautical };

	public static final int coordinateTransformCode[] = { Code_CT_TransverseMercator,
		Code_CT_TransvMercator_Modified_Alaska, Code_CT_ObliqueMercator, Code_CT_ObliqueMercator_Laborde,
		Code_CT_ObliqueMercator_Rosenmund, Code_CT_ObliqueMercator_Spherical, Code_CT_Mercator,
		Code_CT_LambertConfConic_2SP, Code_CT_LambertConfConic_Helmert, Code_CT_LambertAzimEqualArea,
		Code_CT_AlbersEqualArea, Code_CT_AzimuthalEquidistant, Code_CT_EquidistantConic, Code_CT_Stereographic,
		Code_CT_PolarStereographic, Code_CT_ObliqueStereographic, Code_CT_Equirectangular, Code_CT_CassiniSoldner,
		Code_CT_Gnomonic, Code_CT_MillerCylindrical, Code_CT_Orthographic, Code_CT_Polyconic, Code_CT_Robinson,
		Code_CT_Sinusoidal, Code_CT_VanDerGrinten, Code_CT_NewZealandMapGrid, Code_CT_TransvMercator_SouthOriented,
		Code_CT_CylindricalEqualArea };

	public static final int geographicTypeCode[] = { Code_GCS_WGS_84, Code_GCS_NAD_83, Code_UserDefined };

	public static enum AngularUnits {
		Angular_Radian, Angular_Degree, Angular_Arc_Minute, Angular_Arc_Second, Angular_Grad, Angular_Gon, Angular_DMS, Angular_DMS_Hemisphere
	}

	public static enum LinearUnits {
		Linear_Undefined, Linear_Meter, Linear_Foot, Linear_Foot_US_Survey, Linear_Foot_Modified_American, Linear_Foot_Clarke, Linear_Foot_Indian, Linear_Link, Linear_Link_Benoit, Linear_Link_Sears, Linear_Chain_Benoit, Linear_Chain_Sears, Linear_Yard_Sears, Linear_Yard_Indian, Linear_Fathom, Linear_Mile_International_Nautical
	}

	static public enum CoordinateTransform {
		TransverseMercator, TransvMercator_Modified_Alaska, ObliqueMercator, ObliqueMercator_Laborde, ObliqueMercator_Rosenmund, ObliqueMercator_Spherical, Mercator, LambertConfConic_2SP, LambertConfConic_Helmert, LambertAzimEqualArea, AlbersEqualArea, AzimuthalEquidistant, EquidistantConic, Stereographic, PolarStereographic, ObliqueStereographic, Equirectangular, CassiniSoldner, Gnomonic, MillerCylindrical, Orthographic, Polyconic, Robinson, Sinusoidal, VanDerGrinten, NewZealandMapGrid, TransvMercator_SouthOriented, CylindricalEqualArea
	}

	public static enum ModelType {
		ModelTypeUndefined, ModelTypeProjected, ModelTypeGeographic, ModelTypeGeocentric
	}

	public static enum RasterType {
		RasterPixelIsArea, RasterPixelIsPoint
	}

	public static enum GeographicCSType {
		GCS_WGS_84, GCS_NAD_83, GCS_UserDefined
	}

	public int keyCode;
	public int count;
	public int offset;
	public int tagLocation;
	public int[] intValue;
	public double[] doubleValue;
	public String[] stringValue;

	/**
	 * Compare this GeoKey to another.
	 * 
	 * @param gke
	 *            the key
	 * @return
	 */
	@Override
	public int compareTo(GeoKey gke) {
		if (keyCode < gke.keyCode) {
			return (-1);
		}
		if (keyCode > gke.keyCode) {
			return (1);
		}
		return (0);
	}

	/**
	 * Given a key code, find a label for it.
	 * 
	 * @param k
	 *            the key code
	 * @return the label
	 */
	public static String findKeyLabel(int k) {
		KeyID ki = findKeyID(k);
		if (ki == null) {
			return (null);
		} else {
			return (ki.toString());
		}
	}

	/**
	 * Find the key enumeration given its code.
	 * 
	 * @param keyCode
	 *            the key code
	 * @return the enumeration value
	 */
	public static KeyID findKeyID(int keyCode) {
		for (int i = 0; i < idCode.length; ++i) {
			if (keyCode == idCode[i]) {
				return (KeyID.values()[i]);
			}
		}
		return (null);
	}

	/**
	 * Given a key code and value, find the enumeration for the value.
	 * 
	 * @param k
	 *            key code
	 * @param v
	 *            key value
	 * @return the enumeration
	 */
	public static Object findValue(int k, int v) {
		KeyID ki = findKeyID(k);
		if (ki == null) {
			return (null);
		}
		Object val = findValue(ki, v);
		return (val);
	}

	/**
	 * Given a KeyID enumeration and a key value, find the value enumeration.
	 * 
	 * @param key
	 *            the key id
	 * @param valueCode
	 *            the value code
	 * @return the value enumeration
	 */
	public static Object findValue(KeyID key, int valueCode) {
		if (key == KeyID.GeogAngularUnits) {
			for (int i = 0; i < angularUnitsCode.length; ++i) {
				if (valueCode == angularUnitsCode[i]) {
					return (AngularUnits.values()[i]);
				}
			}
			return (null);
		}
		if (key == KeyID.GeogLinearUnits) {
			for (int i = 0; i < linearUnitsCode.length; ++i) {
				if (valueCode == linearUnitsCode[i]) {
					return (LinearUnits.values()[i]);
				}
			}
			return (null);
		} else if (key == KeyID.ModelType) {
			for (int i = 0; i < modelTypeCode.length; ++i) {
				if (valueCode == modelTypeCode[i]) {
					return (ModelType.values()[i]);
				}
			}
			return (null);
		} else if (key == KeyID.RasterType) {
			for (int i = 0; i < rasterTypeCode.length; ++i) {
				if (valueCode == rasterTypeCode[i]) {
					return (RasterType.values()[i]);
				}
			}
			return (null);
		} else if (key == KeyID.ProjLinearUnits) {
			for (int i = 0; i < linearUnitsCode.length; ++i) {
				if (valueCode == linearUnitsCode[i]) {
					return (LinearUnits.values()[i]);
				}
			}
			return (null);
		} else if (key == KeyID.ProjCoordTrans) {
			for (int i = 0; i < coordinateTransformCode.length; ++i) {
				if (valueCode == coordinateTransformCode[i]) {
					// return(CoordinateTransform.values()[i]);
					return (new Integer(valueCode));
				}
			}
			return (null);
		} else if (key == KeyID.GeographicType) {
			if (valueCode == Code_Datum_WGS84) {
				valueCode = Code_GCS_WGS_84;
			}
			for (int i = 0; i < geographicTypeCode.length; ++i) {
				if (valueCode == geographicTypeCode[i]) {
					// return(GeographicCSType.values()[i]);
					return (new Integer(valueCode));
				}
			}
			System.out.println("Geographic CS type of " + valueCode + " not supported.");
			return (null);
		}
		return (null);
	}

	public static HashMap<KeyID, Object> mapKeys(short[] geoKeyDirectory, double[] geoKeyDouble, String geoKeyAscii) {
		short[] keys = geoKeyDirectory;
		double[] doubleParams = geoKeyDouble;
		String asciiParams = geoKeyAscii;
		HashMap<KeyID, Object> map = new HashMap<KeyID, Object>();
		if (keys == null) {
			return (map);
		}
		int numKeys = keys[3];
		for (int k = 1; k <= numKeys; ++k) {
			int kk = k * 4;
			int keyId = keys[kk];
			KeyID label = GeoKey.findKeyID(keyId);
			if (label == null) {
				System.out.println("Unknown key: " + keyId);
				continue;
			}
			int count = keys[kk + 2];
			int type = keys[kk + 1] & 0xffff;
			int valParam = keys[kk + 3];
			switch (type) {
			case 0:
				Object value = findValue(keyId, valParam);
				if (value != null) {
					map.put(label, value);
				} else {
					map.put(label, new Integer(valParam));
				}
				break;
			case GeoKey.Code_GeoKeyDirectoryTag:
				if (count > 1) {
					int[] val = new int[count];
					for (int i = 0; i < count; ++i) {
						val[i] = keys[numKeys + valParam + i];
					}
					map.put(label, val);
				} else {
					map.put(label, new Integer(numKeys + valParam));
				}
				break;
			case GeoKey.Code_GeoDoubleParamsTag:
				if (doubleParams == null) {
					break;
				}
				if (count > 1) {
					double[] val = new double[count];
					for (int i = 0; i < count; ++i) {
						val[i] = doubleParams[valParam + i];
					}
					map.put(label, val);
				} else {
					map.put(label, new Double(doubleParams[valParam]));
				}
				break;
			case GeoKey.Code_GeoAsciiParamsTag:
				if (asciiParams == null) {
					break;
				}
				String val = asciiParams.substring(valParam, valParam + count - 1);
				map.put(label, val);
				break;
			}

		}
		return (map);
	}

}
