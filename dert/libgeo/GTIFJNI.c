#include <jni.h>
#include <string.h>
/* Header for class gov_nasa_arc_dert_geotiff_GTIF */
#include "GTIFJNI.h"
#include "tiffio.h"

// The following is adapted from the "Modifying the TIFF Library" chapter of the
// libtiff documentation.  WARNING: This interface is labeled obsolete in tiffio.h
// so it may be removed in the future.  However, modifying the tiff library itself
// seems a much more invasive approach at this point.

#define TIFFTAG_GEOPIXELSCALE        33550
#define TIFFTAG_GEOTIEPOINTS         33922
#define TIFFTAG_GEOTRANSMATRIX       34264
#define TIFFTAG_GEOKEYDIRECTORY      34735
#define TIFFTAG_GEODOUBLEPARAMS      34736
#define TIFFTAG_GEOASCIIPARAMS       34737
#define TIFFTAG_GDAL_NODATA          42113
#define TIFFTAG_GDAL_METADATA        42112

#define TRUE 1
#define FALSE 0

	static const TIFFFieldInfo geotiffFieldInfo[] = {
		{ TIFFTAG_GEOPIXELSCALE, -1,-1, TIFF_DOUBLE, FIELD_CUSTOM, TRUE, TRUE, "GeoPixelScale" },
		{ TIFFTAG_GEOTRANSMATRIX, -1,-1, TIFF_DOUBLE, FIELD_CUSTOM, TRUE, TRUE,	"GeoTransformationMatrix" },
		{ TIFFTAG_GEOTIEPOINTS,	-1,-1, TIFF_DOUBLE,	FIELD_CUSTOM, TRUE,	TRUE, "GeoTiePoints" },
		{ TIFFTAG_GEOKEYDIRECTORY, -1,-1, TIFF_SHORT, FIELD_CUSTOM, TRUE, TRUE,	"GeoKeyDirectory" },
		{ TIFFTAG_GEODOUBLEPARAMS, -1,-1, TIFF_DOUBLE, FIELD_CUSTOM, TRUE, TRUE, "GeoDoubleParams" },
		{ TIFFTAG_GEOASCIIPARAMS, -1,-1, TIFF_ASCII, FIELD_CUSTOM, TRUE, FALSE,	"GeoASCIIParams" },
		{ TIFFTAG_GDAL_NODATA, -1,-1, TIFF_ASCII, FIELD_CUSTOM, TRUE, FALSE, "GdalNoData" },
		{ TIFFTAG_GDAL_METADATA, -1,-1, TIFF_ASCII, FIELD_CUSTOM, TRUE, FALSE, "GdalMetadata" }
	};

	static char errorMsgStr[65536];
	static int firstTime = 1;

	static void errorHandler(const char* module, const char* fmt, va_list ap) {
		sprintf(errorMsgStr, fmt, ap);
	}

	static void geotiffTagExtender(TIFF *tif) {
	    /* Install the extended Tag field info */
		int n = sizeof(geotiffFieldInfo)/sizeof(geotiffFieldInfo[0]);
	    TIFFMergeFieldInfo(tif, geotiffFieldInfo, n);
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    openTIFF
	 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
	 */
	JNIEXPORT jlong JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_openTIFF
	(JNIEnv *env, jobject theObject, jstring filePath, jstring access) {

		if (firstTime) {
		    TIFFSetTagExtender(geotiffTagExtender);
			TIFFSetErrorHandler((TIFFErrorHandler)errorHandler);
			firstTime = 0;
		}
  
		jboolean isFileCopy, isAccCopy;
		const char* path = (char*)(*env)->GetStringUTFChars(env, filePath, &isFileCopy);
		const char* acc = (char*)(*env)->GetStringUTFChars(env, access, &isAccCopy);
		TIFF* tif = TIFFOpen( path, acc );
		if ( isFileCopy )
			(*env)->ReleaseStringUTFChars(env, filePath, path);
		if ( isAccCopy )
			(*env)->ReleaseStringUTFChars(env, access, acc);
		return( (jlong)tif );
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    closeTIFF
	 * Signature: (J)V
	 */
	JNIEXPORT void JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_closeTIFF
	(JNIEnv *env, jobject theObject, jlong handle) {
  
		TIFF* tif = (TIFF*)handle;
		if ( !tif )
			return;
		TIFFClose( tif );
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getTIFFFieldString
	 * Signature: (JI[Ljava/lang/String;)I
	 */
	JNIEXPORT jint JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getTIFFFieldString
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jobjectArray value) {
		
		void* val;
		uint16 c;
		int count, n;
		TIFF* tiff;
		
		tiff = (TIFF*)handle;
		if ( !tiff )
			return( 0 );
			
		n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, &val );
//		fprintf(stderr, "getTIFFFieldString %d %d\n", tag, val);
		if ( !val )
			return(0);
		count = strlen((char*)val);
		if ( n > 0 ) {
//			fprintf(stderr, "getTIFFFieldString %d %d %d %s\n", tag, n, count, (char*)val);
			jstring str = (*env)->NewStringUTF(env, (char*)val);
			(*env)->SetObjectArrayElement(env, value, 0, str);
			(*env)->DeleteLocalRef(env, str);
			return( (jint)count );
		}
		return( 0 );
	}		
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getTIFFFieldInt
	 * Signature: (JI[I)I
	 */
	JNIEXPORT jint JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getTIFFFieldInt
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jintArray value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( 0 );
		jint count = (*env)->GetArrayLength(env, value);
		int n = 0;
		if (count == 1) {
			int val[1] = {0};
			n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, val );
			if ( n > 0 ) {
				(*env)->SetIntArrayRegion(env, value, 0, 1, val);
				return( (jint)count );
			}
			return( 0 );
		}
		else {
			void* val;
			uint16 c;
			n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, &c, &val );
			count = c;
			if ( n > 0 ) {
				(*env)->SetIntArrayRegion(env, value, 0, count, (int*)val);
				return( (jint)count );
			}
			return( 0 );
		}
	}		
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getTIFFFieldShort
	 * Signature: (JI[S)I
	 */
	JNIEXPORT jint JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getTIFFFieldShort
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jshortArray value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( 0 );
		jint count = (*env)->GetArrayLength(env, value);
		int n = 0;
		if (count == 1) {
			short val[1] = {0};
			n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, val );
			if ( n > 0 ) {
				(*env)->SetShortArrayRegion(env, value, 0, 1, val);
				return( (jint)count );
			}
			return( 0 );
		}
		else {
			void* val;
			uint16 c;
			n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, &c, &val );
			if ( n > 0 ) {
				count = c;
				(*env)->SetShortArrayRegion(env, value, 0, count, (short*)val);
				return( (jint)count );
			}
			return( 0 );
		}
	}		
		
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getTIFFFieldDouble
	 * Signature: (JI[D)I
	 */
	JNIEXPORT jint JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getTIFFFieldDouble
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jdoubleArray value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( 0 );
		jint count = (*env)->GetArrayLength(env, value);
		int n = 0;
		if (count == 1) {
			double val[1] = {0};
			n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, val );
//			fprintf(stderr, "getTIFFFieldDouble %d %d %f\n", tag, n, val[0]);
			if ( n > 0 ) {
				(*env)->SetDoubleArrayRegion(env, value, 0, 1, val);
				return( (jint)count );
			}
			return( 0 );
		}
		else {
			void* val;
			uint16 c;
			n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, &c, &val );
			count = c;
			if ( n > 0 ) {
				(*env)->SetDoubleArrayRegion(env, value, 0, count, (double*)val);
				return( (jint)count );
			}
			return( 0 );
		}
	}		
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getTIFFFieldFloat
	 * Signature: (JI[F)I
	 */
	JNIEXPORT jint JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getTIFFFieldFloat
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jfloatArray value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( 0 );
		jint count = (*env)->GetArrayLength(env, value);
		int n = 0;
		if (count == 1) {
			float val[1] = {0};
			n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, val );
			if ( n > 0 ) {
				(*env)->SetFloatArrayRegion(env, value, 0, 1, val);
				return( (jint)count );
			}
			return( 0 );
		}
		else {
			void* val;
			uint16 c;
			n = TIFFGetFieldDefaulted( tiff, (ttag_t)tag, &c, &val );
			count = c;
			if ( n > 0 ) {
				(*env)->SetFloatArrayRegion(env, value, 0, count, (float*)val);
				return( (jint)count );
			}
			return( 0 );
		}
	}		
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldString
	 * Signature: (JILjava/lang/String;)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldString
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jstring value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		jboolean isCopy;
		const char* str = (char*)(*env)->GetStringUTFChars(env, value, &isCopy);
//		fprintf(stderr, "setTIFFFieldString %s %d\n", str, strlen(str));
		int n = TIFFSetField( tiff, (ttag_t)tag, str );
		if ( isCopy )
			(*env)->ReleaseStringUTFChars(env, value, str);
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldInt
	 * Signature: (JII)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldInt
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jint value) {

		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		int n = TIFFSetField( tiff, (ttag_t)tag, (int)value );
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldShort
	 * Signature: (JIS)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldShort
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jshort value) {

		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		int n = TIFFSetField( tiff, (ttag_t)tag, (short)value );
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldDouble
	 * Signature: (JID)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldDouble
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jdouble value) {

		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		int n = TIFFSetField( tiff, (ttag_t)tag, (double)value );
//		fprintf(stderr, "setTIFFFieldDouble %d %d %f\n", tag, n, (double)value);
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldFloat
	 * Signature: (JIF)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldFloat
	(JNIEnv *env, jobject theObject, jlong handle, jint tag, jfloat value) {

		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		int n = TIFFSetField( tiff, (ttag_t)tag, (float)value );
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldIntArray
	 * Signature: (JI[I)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldIntArray
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jintArray value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		jboolean isCopy;
		jint* val = (*env)->GetIntArrayElements(env, value, &isCopy);
		jint count = (*env)->GetArrayLength(env, value);
		int n = TIFFSetField( tiff, (ttag_t)tag, count, val );
		if ( isCopy )
			(*env)->ReleaseIntArrayElements(env, value, val, JNI_ABORT);
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldShortArray
	 * Signature: (JI[S)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldShortArray
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jshortArray value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		jboolean isCopy;
		jshort* val = (*env)->GetShortArrayElements(env, value, &isCopy);
		jint count = (*env)->GetArrayLength(env, value);
		int n = TIFFSetField( tiff, (ttag_t)tag, count, val );
		if ( isCopy )
			(*env)->ReleaseShortArrayElements(env, value, val, JNI_ABORT);
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldDoubleArray
	 * Signature: (JI[D)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldDoubleArray
		(JNIEnv *env, jobject theObject, jlong handle, jint tag, jdoubleArray value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		jboolean isCopy;
		jdouble* val = (*env)->GetDoubleArrayElements(env, value, &isCopy);
		jint count = (*env)->GetArrayLength(env, value);
		int n = TIFFSetField( tiff, (ttag_t)tag, count, val );
		if ( isCopy )
			(*env)->ReleaseDoubleArrayElements(env, value, val, JNI_ABORT);
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    setTIFFFieldFloatArray
	 * Signature: (JI[F)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_setTIFFFieldFloatArray
	(JNIEnv *env, jobject theObject, jlong handle, jint tag, jfloatArray value) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		jboolean isCopy;
		jfloat* val = (*env)->GetFloatArrayElements(env, value, &isCopy);
		jint count = (*env)->GetArrayLength(env, value);
		int n = TIFFSetField( tiff, (ttag_t)tag, count, val );
		if ( isCopy )
			(*env)->ReleaseFloatArrayElements(env, value, val, JNI_ABORT);
		if ( n > 0 )
			return( JNI_TRUE );
		return( JNI_FALSE );
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    isTiled
	 * Signature: (J)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_isTiled
	(JNIEnv *env, jobject the, jlong handle) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		return( (jboolean)TIFFIsTiled( tiff ) );
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getNumberOfStrips
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getNumberOfStrips
	(JNIEnv *env, jobject theObject, jlong handle) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		int val = TIFFNumberOfStrips( tiff );
		return( (jint)val );
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getStripSize
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getStripSize
	(JNIEnv *env, jobject theObject, jlong handle) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		long val = TIFFStripSize( tiff );
		return( (jlong)val );
	}	
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    readStrip
	 * Signature: (JILjava/nio/Buffer;J)J
	 */
	JNIEXPORT jlong JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_readStrip
	(JNIEnv *env, jobject theObject, jlong handle, jint stripNumber, jobject buffer, jlong size) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		void* buf = (*env)->GetDirectBufferAddress(env, buffer);
//		fprintf(stderr, "readStrip %d %d\n", stripNumber, size);
		long val = TIFFReadEncodedStrip( tiff, (uint32)stripNumber, (tdata_t)buf, (long)size );
		return( (jlong)val );
	}	
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    readRGBAStrip
	 * Signature: (JILjava/nio/Buffer;)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_readRGBAStrip
	(JNIEnv *env, jobject theObject, jlong handle, jint row, jobject buffer) {

		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		uint32* buf = (uint32*)(*env)->GetDirectBufferAddress(env, buffer);
		int val = TIFFReadRGBAStrip( tiff, (uint32)row, buf );
		if (val == 1)
			return( JNI_TRUE );
		return( JNI_FALSE );
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    writeStrip
	 * Signature: (JILjava/nio/Buffer;J)J
	 */
	JNIEXPORT jlong JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_writeStrip
	(JNIEnv *env, jobject theObject, jlong handle, jint stripNumber, jobject buffer, jlong size) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		void* buf = (*env)->GetDirectBufferAddress(env, buffer);
		long val = TIFFWriteEncodedStrip( tiff, (uint32)stripNumber, (tdata_t)buf, (long)size );
		return( (jlong)val );
	}	
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getNumberOfTiles
	 * Signature: (J)I
	 */
	JNIEXPORT jint JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getNumberOfTiles
	(JNIEnv *env, jobject theObject, jlong handle) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		int val = TIFFNumberOfTiles( tiff );
		return( (jint)val );
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getTileSize
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getTileSize
	(JNIEnv *env, jobject theObject, jlong handle) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		long val = TIFFTileSize( tiff );
		return( (jlong)val );
	}	
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    readTile
	 * Signature: (JILjava/nio/Buffer;J)J
	 */
	JNIEXPORT jlong JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_readTile
	(JNIEnv *env, jobject theObject, jlong handle, jint tileNumber, jobject buffer, jlong size) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		void* buf = (*env)->GetDirectBufferAddress(env, buffer);
		long val = TIFFReadEncodedTile( tiff, (uint32)tileNumber, (tdata_t)buf, (long)size );
		return( (jlong)val );
	}	
	
	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    readRGBATile
	 * Signature: (JIILjava/nio/Buffer;)Z
	 */
	JNIEXPORT jboolean JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_readRGBATile
	(JNIEnv *env, jobject theObject, jlong handle, jint x, jint y, jobject buffer) {

		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		uint32* buf = (uint32*)(*env)->GetDirectBufferAddress(env, buffer);
		int val = TIFFReadRGBATile( tiff, (uint32)x, (uint32)y, buf );
		if (val == 1)
			return( JNI_TRUE );
		return( JNI_FALSE );
	}

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    writeTile
	 * Signature: (JILjava/nio/Buffer;J)J
	 */
	JNIEXPORT jlong JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_writeTile
	(JNIEnv *env, jobject theObject, jlong handle, jint tileNumber, jobject buffer, jlong size) {
		
		TIFF* tiff = (TIFF*)handle;
		if ( !tiff )
			return( JNI_FALSE );
		void* buf = (*env)->GetDirectBufferAddress(env, buffer);
		long val = TIFFWriteEncodedTile( tiff, (uint32)tileNumber, (tdata_t)buf, (long)size );
		return( (jlong)val );
	}	

	/*
	 * Class:     gov_nasa_arc_dert_raster_geotiff_GTIF
	 * Method:    getTIFFError
	 * Signature: ()Ljava/lang/String;
	 */
	JNIEXPORT jstring JNICALL Java_gov_nasa_arc_dert_raster_geotiff_GTIF_getTIFFError
	  (JNIEnv *env, jobject theObject) {
		jstring str = (*env)->NewStringUTF(env, errorMsgStr);
		return(str);
	}
