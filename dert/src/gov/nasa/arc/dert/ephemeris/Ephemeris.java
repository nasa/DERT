package gov.nasa.arc.dert.ephemeris;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.view.Console;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import spice.basic.BodyName;
import spice.basic.CSPICE;
import spice.basic.KernelDatabase;

/**
 * Provides methods for locating the position of the Sun relative to the
 * landscape using the JNISpice library.
 *
 */
public class Ephemeris {

	// Singleton instance
	protected static Ephemeris instance;

	protected final double[] zAxis = { 0, 0, 1 };
	protected SimpleDateFormat utcDateFormat;

	// Load the native libraries for JNISpice.
	static {
		if (Dert.isMac) {
			loadNativeLibrary("/libJNISpice.jnilib");
		} else if (Dert.isLinux) {
			loadNativeLibrary("/libJNISpice.so");
		}
	}

	protected static void loadNativeLibrary(String libName) {
		try {

			// copy the library from the jar file to /tmp
			final InputStream in = Ephemeris.class.getResource(libName).openStream();
			int p0 = libName.lastIndexOf('/');
			int p1 = libName.lastIndexOf('.');
			String tempName = libName.substring(p0, p1) + '_' + System.currentTimeMillis();
			final File libFile = File.createTempFile(tempName, ".jni");
			libFile.deleteOnExit();
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(libFile));
			int len = 0;
			byte[] buffer = new byte[32768];
			while ((len = in.read(buffer)) > -1) {
				out.write(buffer, 0, len);
			}
			out.close();
			in.close();

			// load the library into memory
			System.load(libFile.getAbsolutePath());
			libFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the singleton
	 * 
	 * @param path
	 * @param properties
	 */
	public static void createInstance(String path, Properties properties) {
		instance = new Ephemeris(path, properties);
	}

	/**
	 * Get the singleton
	 * 
	 * @return
	 */
	public static Ephemeris getInstance() {
		return (instance);
	}

	/**
	 * Constructor
	 * 
	 * @param path
	 * @param properties
	 */
	protected Ephemeris(String path, Properties properties) {
		initialize(path, properties);
	}

	protected void initialize(String path, Properties properties) {
		utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalStateException("Unable to initialize ephemeris: " + path + " not found.");
		} else {
			try {
				// load SPICE kernels
				String[] fileName = getFileList(properties);
				for (int i = 0; i < fileName.length; ++i) {
					Console.getInstance().println("Loading SPICE kernel "+fileName[i]);
					KernelDatabase.load(path + "kernels/" + fileName[i]);
				}
			} catch (Exception e) {
				System.out.println("Unable to load SPICE kernels, see log.");
				e.printStackTrace();
				return;
			}
		}
	}

	private String[] getFileList(Properties properties) {
		Object[] key = properties.keySet().toArray();
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < key.length; ++i) {
			String name = (String)key[i];
			if (name.startsWith("SpiceKernel.")) {
				String str = properties.getProperty(name);
				if ((str != null) && !str.isEmpty())
					list.add(name);
			}
		}
		Collections.sort(list);
		String[] files = new String[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			String name = list.get(i);
			files[i] = properties.getProperty(name);
		}
		return (files);
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

	/**
	 * Get the vector to a target body from a point on an observer body.
	 * 
	 * @param observer
	 *            the name for the observer body (Mars, for example)
	 * @param target
	 *            the name for the target (Sun, for example)
	 * @param time
	 *            the UTC epoch at which the vector is to be computed
	 * @param lat
	 *            the latitude on the observer surface
	 * @param lon
	 *            the longitude on the observer surface
	 * @return the vector
	 */
	public static double[] getTargetVector(String observer, String target, String time, double lon, double lat, double alt) {
		// SPICE is not thread safe. Allow only one thread to use it at a time
		synchronized (instance) {
			double[] vector = instance.getTargetVectorAtLonLatAlt(observer, target, time, lon, lat, alt);
			return (vector);
		}
	}

	protected double[] getTargetVectorAtLonLatAlt(String observer, String target, String time, double lon, double lat, double alt) {
		observer = observer.toUpperCase();
		target = target.toUpperCase();		
		alt /= 1000;
		
//		System.err.println("Ephemeris.getTargetVectorAtLonLatAlt "+observer+" "+target+" "+time+" "+lon+" "+lat+" "+alt);
		
		try {
			// Convert epoch to ephemeris time
			double et = CSPICE.str2et(time);

			// Compute target state in observer body-fixed frame
			double[] pos = new double[6];
			double[] lt = new double[1];
			String obsName = observer;
//			if (obsName.equals("MARS"))
//				obsName += " BARYCENTER";
			CSPICE.spkpos(target, et, "IAU_"+observer, "LT+S", obsName, pos, lt);
			
			// Get the observer body
			BodyName obsBody = new BodyName(observer);

			// Get the observer radii
			double[] obsRadii = CSPICE.bodvcd(obsBody.getIDCode(), "RADII");

			// Compute surface point on observer body in body-fixed frame			
			double[] obsSurfacePt = CSPICE.georec(Math.toRadians(lon), Math.toRadians(lat), alt, obsRadii[0], (obsRadii[0]-obsRadii[2])/obsRadii[0]);
			obsSurfacePt[2] += alt;

			// Compute surface normal at surface point
			double[] obsSurfaceNm = CSPICE.surfnm(obsRadii[0], obsRadii[1], obsRadii[2], obsSurfacePt);
			obsSurfaceNm = CSPICE.vhat(obsSurfaceNm);
			
			pos[0] -= obsSurfacePt[0];
			pos[1] -= obsSurfacePt[1];
			pos[2] -= obsSurfacePt[2];

			// Compute the matrix to tranform the body-fixed frame to the surface frame
			// make the Y axis Lon = 0
			double[][] obsMatrix = CSPICE.twovec(obsSurfaceNm, 3, zAxis, 2);
			double[] trgPt = new double[] {pos[0], pos[1], pos[2]};
			trgPt = CSPICE.mxv(obsMatrix, trgPt);
			trgPt = CSPICE.vhat(trgPt);
			pos[0] = trgPt[0];
			pos[1] = trgPt[1];
			pos[2] = trgPt[2];
					
			// convert to radius, lon, and lat
			trgPt = CSPICE.reclat(trgPt);
			pos[3] = trgPt[0];
			pos[4] = trgPt[1];
			pos[5] = trgPt[2];

			return (pos);
		} catch (Exception e) {
			System.out.println("Unable to get target vector, see log.");
			e.printStackTrace();
			return (null);
		}
	}

}
