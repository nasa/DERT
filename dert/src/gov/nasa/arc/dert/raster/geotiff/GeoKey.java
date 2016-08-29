package gov.nasa.arc.dert.raster.geotiff;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Defines enumerations for use with GeoTIFF GeoKeys.
 *
 */
public class GeoKey implements Comparable<GeoKey> {

	public static final int Code_GeoKeyShortTag = 0;
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

	public static final int Code_ModelTypeProjected = 1;
	public static final int Code_ModelTypeGeographic = 2;
	public static final int Code_ModelTypeGeocentric = 3;

	public static final int Code_RasterPixelIsArea = 1;
	public static final int Code_RasterPixelIsPoint = 2;

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
	public static final int Code_Undefined = 0;

	public static final int[] idCode = {
		
		Code_ModelType, Code_RasterType, Code_Citation,

		Code_GeographicType, Code_GeogCitation, Code_GeogGeodeticDatum, Code_GeogPrimeMeridian, Code_GeogPrimeMeridianLong, Code_GeogLinearUnits,
		Code_GeogLinearUnitSize, Code_GeogAngularUnits, Code_GeogAngularUnitSize, Code_GeogEllipsoid,
		Code_GeogSemiMajorAxis, Code_GeogSemiMinorAxis, Code_GeogInvFlattening, Code_GeogAzimuthUnits,
		

		Code_ProjectedCSType, Code_PCSCitation, Code_Projection, Code_ProjCoordTrans, Code_ProjLinearUnits,
		Code_ProjLinearUnitSize, Code_ProjStdParallel1, Code_ProjStdParallel2, Code_ProjNatOriginLong,
		Code_ProjNatOriginLat, Code_ProjFalseEasting, Code_ProjFalseNorthing, Code_ProjFalseOriginLong,
		Code_ProjFalseOriginLat, Code_ProjFalseOriginEasting, Code_ProjFalseOriginNorthing, Code_ProjCenterLong,
		Code_ProjCenterLat, Code_ProjCenterEasting, Code_ProjCenterNorthing, Code_ProjScaleAtNatOrigin,
		Code_ProjScaleAtCenter, Code_ProjAzimuthAngle, Code_ProjStraightVertPoleLong,

		Code_VerticalCSType, Code_VerticalCitation, Code_VerticalDatum, Code_VerticalUnits
		
	};

	public static final int[] idType = {
		
		Code_GeoKeyShortTag, Code_GeoKeyShortTag, Code_GeoAsciiParamsTag,

		Code_GeoKeyShortTag, Code_GeoAsciiParamsTag, Code_GeoKeyShortTag, Code_GeoKeyShortTag, Code_GeoDoubleParamsTag, Code_GeoKeyShortTag,
		Code_GeoDoubleParamsTag, Code_GeoKeyShortTag, Code_GeoDoubleParamsTag, Code_GeoKeyShortTag,
		Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoKeyShortTag,

		Code_GeoKeyShortTag, Code_GeoAsciiParamsTag, Code_GeoKeyShortTag, Code_GeoKeyShortTag, Code_GeoKeyShortTag,
		Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag,
		Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag,
		Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag,
		Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag,
		Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag, Code_GeoDoubleParamsTag,

		Code_GeoKeyShortTag, Code_GeoAsciiParamsTag, Code_GeoKeyShortTag, Code_GeoKeyShortTag
		
	};

	public static enum KeyID {
		ModelType, RasterType, Citation,

		GeographicType, GeogCitation, GeogGeodeticDatum, GeogPrimeMeridian, GeogPrimeMeridianLong, GeogLinearUnits, GeogLinearUnitSize, GeogAngularUnits, GeogAngularUnitSize, GeogEllipsoid, GeogSemiMajorAxis, GeogSemiMinorAxis, GeogInvFlattening, GeogAzimuthUnits,

		ProjectedCSType, PCSCitation, Projection, ProjCoordTrans, ProjLinearUnits, ProjLinearUnitSize, ProjStdParallel1, ProjStdParallel2, ProjNatOriginLong, ProjNatOriginLat, ProjFalseEasting, ProjFalseNorthing, ProjFalseOriginLong, ProjFalseOriginLat, ProjFalseOriginEasting, ProjFalseOriginNorthing, ProjCenterLong, ProjCenterLat, ProjCenterEasting, ProjCenterNorthing, ProjScaleAtNatOrigin, ProjScaleAtCenter, ProjAzimuthAngle, ProjStraightVertPoleLong,

		VerticalCSType, VerticalCitation, VerticalDatum, VerticalUnits,
	}

	public static final int angularUnitsCode[] = { Code_Angular_Radian, Code_Angular_Degree, Code_Angular_Arc_Minute,
		Code_Angular_Arc_Second, Code_Angular_Grad, Code_Angular_Gon, Code_Angular_DMS, Code_Angular_DMS_Hemisphere };

	public static final int modelTypeCode[] = { Code_Undefined, Code_ModelTypeProjected,
		Code_ModelTypeGeographic, Code_ModelTypeGeocentric, Code_UserDefined };

	public static final int rasterTypeCode[] = { Code_Undefined, Code_RasterPixelIsArea, Code_RasterPixelIsPoint, Code_UserDefined };

	public static final int linearUnitsCode[] = { Code_Undefined, Code_Linear_Meter, Code_Linear_Foot,
		Code_Linear_Foot_US_Survey, Code_Linear_Foot_Modified_American, Code_Linear_Foot_Clarke,
		Code_Linear_Foot_Indian, Code_Linear_Link, Code_Linear_Link_Benoit, Code_Linear_Link_Sears,
		Code_Linear_Chain_Benoit, Code_Linear_Chain_Sears, Code_Linear_Yard_Sears, Code_Linear_Yard_Indian,
		Code_Linear_Fathom, Code_Linear_Mile_International_Nautical, Code_UserDefined };

	public static final int coordinateTransformCode[] = { Code_Undefined, Code_CT_TransverseMercator,
		Code_CT_TransvMercator_Modified_Alaska, Code_CT_ObliqueMercator, Code_CT_ObliqueMercator_Laborde,
		Code_CT_ObliqueMercator_Rosenmund, Code_CT_ObliqueMercator_Spherical, Code_CT_Mercator,
		Code_CT_LambertConfConic_2SP, Code_CT_LambertConfConic_Helmert, Code_CT_LambertAzimEqualArea,
		Code_CT_AlbersEqualArea, Code_CT_AzimuthalEquidistant, Code_CT_EquidistantConic, Code_CT_Stereographic,
		Code_CT_PolarStereographic, Code_CT_ObliqueStereographic, Code_CT_Equirectangular, Code_CT_CassiniSoldner,
		Code_CT_Gnomonic, Code_CT_MillerCylindrical, Code_CT_Orthographic, Code_CT_Polyconic, Code_CT_Robinson,
		Code_CT_Sinusoidal, Code_CT_VanDerGrinten, Code_CT_NewZealandMapGrid, Code_CT_TransvMercator_SouthOriented,
		Code_CT_CylindricalEqualArea, Code_UserDefined };

	public static final int geographicTypeCode[] = { Code_Undefined, Code_GCS_WGS_84, Code_GCS_NAD_83, Code_UserDefined };

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
	 * Find the key code given its enumeration.
	 * 
	 * @param keyID the key enumeration enumeration
	 *           
	 * @return the key code
	 */
	public static int findKeyCode(KeyID keyID) {
		return(idCode[keyID.ordinal()]);
	}

	/**
	 * Find the key type given its enumeration.
	 * 
	 * @param keyID
	 *            the key enumeration
	 * @return the key type
	 */
	public static int findKeyType(KeyID keyID) {
		return(idType[keyID.ordinal()]);
	}

	/**
	 * Given a KeyID enumeration and a value code, check the code and return it as an Integer object.
	 * 
	 * @param key
	 *            the key id
	 * @param keyCode
	 *            the key value code
	 * @return the key value code as an Integer object
	 */
	public static Integer checkCode(KeyID key, int valueCode) {
		if (key == KeyID.GeogAngularUnits) {
			for (int i = 0; i < angularUnitsCode.length; ++i) {
				if (valueCode == angularUnitsCode[i]) {
					return (new Integer(valueCode));
				}
			}
			System.out.println("Angular units code " + valueCode + " not supported.");
			return (null);
		} else if (key == KeyID.GeogLinearUnits) {
			for (int i = 0; i < linearUnitsCode.length; ++i) {
				if (valueCode == linearUnitsCode[i]) {
					return (new Integer(valueCode));
				}
			}
			System.out.println("Linear units code " + valueCode + " not supported.");
			return (null);
		} else if (key == KeyID.ModelType) {
			for (int i = 0; i < modelTypeCode.length; ++i) {
				if (valueCode == modelTypeCode[i]) {
					return (new Integer(valueCode));
				}
			}
			System.out.println("Mode type code " + valueCode + " not supported.");
			return (null);
		} else if (key == KeyID.RasterType) {
			for (int i = 0; i < rasterTypeCode.length; ++i) {
				if (valueCode == rasterTypeCode[i]) {
					return (new Integer(valueCode));
				}
			}
			System.out.println("Raster type code " + valueCode + " not supported.");
			return (null);
		} else if (key == KeyID.ProjLinearUnits) {
			for (int i = 0; i < linearUnitsCode.length; ++i) {
				if (valueCode == linearUnitsCode[i]) {
					return (new Integer(valueCode));
				}
			}
			System.out.println("Projected linear units code " + valueCode + " not supported.");
			return (null);
		} else if (key == KeyID.ProjCoordTrans) {
			for (int i = 0; i < coordinateTransformCode.length; ++i) {
				if (valueCode == coordinateTransformCode[i]) {
					if (valueCode == Code_UserDefined) {
						
					}
					return (new Integer(valueCode));
				}
			}
			System.out.println("Projected coordinate transform code " + valueCode + " not supported.");
			return (null);
		} else if (key == KeyID.GeographicType) {
			if (valueCode == Code_Datum_WGS84) {
				valueCode = Code_GCS_WGS_84;
			}
			for (int i = 0; i < geographicTypeCode.length; ++i) {
				if (valueCode == geographicTypeCode[i]) {
					return (new Integer(valueCode));
				}
			}
			System.out.println("Geographic CS type code " + valueCode + " not supported.");
			return (null);
		}
		else if ((valueCode > -1) && (valueCode < 65536))		
			return (new Integer(valueCode));
		return(null);
	}

	/**
	 * Given a KeyID enumeration and a value object, find the number of key values.
	 * 
	 * @param key
	 *            the key id
	 * @param index
	 *            the enumeration index
	 * @return the key value
	 */
	public static int findValueCount(KeyID key, Object obj) {
		if (obj instanceof Integer[])
			return(((Integer[])obj).length);
		if (obj instanceof Short[])
			return(((Short[])obj).length);
		if (obj instanceof Float[])
			return(((Float[])obj).length);
		if (obj instanceof Double[])
			return(((Double[])obj).length);
		if (obj instanceof String)
			return(((String)obj).length()+1);
		return(1);
	}

	public static HashMap<KeyID, Object> mapKeys(short[] geoKeyDirectory, double[] geoKeyDouble, String geoKeyAscii) {
		short[] keys = geoKeyDirectory;
		double[] doubleParams = geoKeyDouble;
		String asciiParams = geoKeyAscii;
		
//		System.err.println();
//		System.err.println("GeoKey.mapKeys geoKeyDirectory");
//		for (int i=0; i<geoKeyDirectory.length; ++i)
//			System.err.println(i+" "+(int)(geoKeyDirectory[i] & 0xffff));
//		System.err.println("GeoKey.mapKeys geoKeyDouble");
//		for (int i=0; i<geoKeyDouble.length; ++i)
//			System.err.println(i+" "+geoKeyDouble[i]);
//		System.err.println("GeoKey.mapKeys geoKeyAscii");
//		System.err.println(geoKeyAscii);
//		System.err.println();
		
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
			case GeoKey.Code_GeoKeyShortTag:
				Integer value = checkCode(label, valParam);
				if (value != null)
					map.put(label, value);
				break;
			case GeoKey.Code_GeoKeyDirectoryTag:
				int[] ival = new int[count];
				for (int i = 0; i < count; ++i) {
					ival[i] = keys[numKeys + valParam + i];
				}
				map.put(label, ival);
				break;
			case GeoKey.Code_GeoDoubleParamsTag:
				if (doubleParams == null) {
					break;
				}
				if (count > 1) {
					double[] dval = new double[count];
					for (int i = 0; i < count; ++i) {
						dval[i] = doubleParams[valParam + i];
					}
					map.put(label, dval);
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

	public static Object[] unmapKeys(HashMap<KeyID, Object> map) {
		KeyID[] mapKey = new KeyID[map.size()];
		map.keySet().toArray(mapKey);
		ArrayList<Short> keys = new ArrayList<Short>();
		ArrayList<Short> shortData = new ArrayList<Short>();
		keys.add(new Short((short)1)); // key directory version
		keys.add(new Short((short)1)); // key revision
		keys.add(new Short((short)2)); // minor revision
		keys.add(new Short((short)mapKey.length)); // number of keys
		ArrayList<Double> doubleParams = new ArrayList<Double>();
		String asciiParams = "";
		for (int k = 0; k < mapKey.length; ++k) {
			int keyCode = findKeyCode(mapKey[k]);
			int type = findKeyType(mapKey[k]);
			int count = findValueCount(mapKey[k], map.get(mapKey[k]));
			switch (type) {
			case GeoKey.Code_GeoKeyShortTag:
				int code = (Integer)map.get(mapKey[k]);
				keys.add((short)keyCode);
				keys.add((short)type);
				keys.add((short)count);
				keys.add((short)code);
				break;
			case GeoKey.Code_GeoKeyDirectoryTag:
				keys.add((short)keyCode);
				keys.add((short)type);
				keys.add((short)count);
				keys.add((short)(mapKey.length*4+shortData.size()));
				Object sobj = map.get(mapKey[k]);
				if (sobj instanceof Integer[]) {
					Integer[] iarray = (Integer[])sobj;
					for (int i=0; i<iarray.length; ++i)
						shortData.add(iarray[i].shortValue());
				}
				else
					shortData.add(((Integer)sobj).shortValue());
				break;
			case GeoKey.Code_GeoDoubleParamsTag:
				keys.add((short)keyCode);
				keys.add((short)type);
				keys.add((short)count);
				keys.add((short)doubleParams.size());
				Object dobj = map.get(mapKey[k]);
				if (dobj instanceof Double[]) {
					Double[] dArray = (Double[])dobj;
					for (int i=0; i<dArray.length; ++i)
						doubleParams.add(dArray[i]);	
				}
				else {
					doubleParams.add((Double)dobj);
				}
				break;
			case GeoKey.Code_GeoAsciiParamsTag:
				keys.add((short)keyCode);
				keys.add((short)type);
				keys.add((short)count);
				keys.add((short)asciiParams.length());
				String str = (String)map.get(mapKey[k]);
				asciiParams += str+"|";					
				break;
			}

		}
		for (int i=0; i<shortData.size(); ++i)
			keys.add(shortData.get(i));
		short[] keysArray = new short[keys.size()];
		for (int i=0; i<keysArray.length; ++i)
			keysArray[i] = keys.get(i);
		double[] doublesArray = new double[doubleParams.size()];
		for (int i=0; i<doublesArray.length; ++i)
			doublesArray[i] = doubleParams.get(i);
		Object[] result = new Object[3];
		result[0] = keysArray;
		result[1] = doublesArray;
		result[2] = asciiParams;
		return (result);
	}

}
