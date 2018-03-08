#include <jni.h>
#include <string.h>
#include "proj_config.h"
#include "projects.h"

// Header for class gov_nasa_arc_dert_proj_Proj4
#include "Proj4JNI.h"

	/*
	 * Class:     gov_nasa_arc_dert_proj_Proj4
	 * Method:    createProj4
	 * Signature: (Ljava/lang/String;)J
	 */
	JNIEXPORT jlong JNICALL Java_gov_nasa_arc_dert_proj_Proj4_createProj4
		(JNIEnv* env, jclass class, jstring str) {
    	const char* def = (*env)->GetStringUTFChars(env, str, NULL);
    	if ( !def )
    		return 0;
		PJ* pj = pj_init_plus(def);
		(*env)->ReleaseStringUTFChars(env, str, def);
		return((jlong)pj);
	}

	/*
	 * Class:     gov_nasa_arc_dert_proj_Proj4
	 * Method:    destroyProj4
	 * Signature: (J)V
	 */
	JNIEXPORT void JNICALL Java_gov_nasa_arc_dert_proj_Proj4_destroyProj4
		(JNIEnv* env, jclass class, jlong handle) {
		if ( !handle )
			return;
		PJ* pj = (PJ*)handle;
		pj_free(pj);
	}

	/*
	 * Class:     gov_nasa_arc_dert_proj_Proj4
	 * Method:    transform
	 * Signature: (JJJI[D[D[D)Ljava/lang/String;
	 */
	JNIEXPORT jstring JNICALL Java_gov_nasa_arc_dert_proj_Proj4_transform
		(JNIEnv* env, jobject obj, jlong src, jlong dest, jlong pntCnt, jint offset, jdoubleArray xArray, jdoubleArray yArray, jdoubleArray zArray) {
		jboolean isXCopy, isYCopy, isZCopy;
		jstring errStr = NULL;
		
		
		PJ* pjSrc = (PJ*)src;
		PJ* pjDst = (PJ*)dest;
		
		if ( pjSrc && pjDst ) {
			jdouble* x = (*env)->GetDoubleArrayElements(env, xArray, &isXCopy);
			jdouble* y = (*env)->GetDoubleArrayElements(env, yArray, &isYCopy);
			jdouble* z = NULL;
			if ( zArray )
				z = (*env)->GetDoubleArrayElements(env, zArray, &isZCopy);
			
			int err = pj_transform(pjSrc, pjDst, pntCnt, offset, x, y, z);
			if ( err )
				errStr = (*env)->NewStringUTF(env, pj_strerrno(err));
			if ( isXCopy )
				(*env)->ReleaseDoubleArrayElements(env, xArray, x, 0);
			if ( isYCopy )
				(*env)->ReleaseDoubleArrayElements(env, yArray, y, 0);
			if ( isZCopy )
				(*env)->ReleaseDoubleArrayElements(env, zArray, z, 0);
		}
		return(errStr);
	}
	
	/*
	 * Class:     gov_nasa_arc_dert_proj_Proj4
	 * Method:    setProjPath
	 * Signature: (Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_gov_nasa_arc_dert_proj_Proj4_setProjPath
		(JNIEnv *env, jclass class, jstring str) {
    	const char* s = (*env)->GetStringUTFChars(env, str, NULL);
    	if ( !s )
    		return;
		setenv("PROJ_LIB", s, 1);
		(*env)->ReleaseStringUTFChars(env, str, s);	
	}
