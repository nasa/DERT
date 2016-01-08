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
		utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
	public static double[] getTargetVector(String observer, String target, String time, double lat, double lon) {
		// SPICE is not thread safe. Allow only one thread to use it at a time
		// ...
		synchronized (instance) {
			double[] vector = instance.getTargetVectorAtLatLon(observer, target, time, lat, lon);
			return (vector);
		}
	}

	protected double[] getTargetVectorAtLatLon(String observer, String target, String time, double lat, double lon) {
		// System.err.println("Ephemeris.getTargetVectorAtLatLon "+observer+" "+target+" "+time+" "+lat+" "+lon);
		try {
			// Convert epoch to ephemeris time
			double et = CSPICE.str2et(time);

			// Get the observer body
			BodyName obsBody = new BodyName(observer);

			// Get the observer radii
			double[] obsRadii = CSPICE.bodvcd(obsBody.getIDCode(), "RADII");

			// Compute vector from observer body center to lat, lon (perfect
			// sphere model)
			double[] vectorFromObsCntr = CSPICE.latrec(1.0, Math.toRadians(lon), Math.toRadians(lat));

			// Compute surface point on observer body in rectangular coordinates
			// and with ellipsoid
			double[] obsCenter = new double[3];
			double[] obsSurfacePt = new double[3];
			boolean[] found = new boolean[1];
			CSPICE.surfpt(obsCenter, vectorFromObsCntr, obsRadii[0], obsRadii[1], obsRadii[2], obsSurfacePt, found);

			// Compute surface normal
			double[] obsSurfaceNm = CSPICE.surfnm(obsRadii[0], obsRadii[1], obsRadii[2], obsSurfacePt);

			// Compute transform matrix
			double[][] obsMatrix = CSPICE.twovec(obsSurfaceNm, 3, zAxis, 2); // make
																				// the
																				// Y
																				// axis
																				// Lon
																				// =
																				// 0

			// Compute target state in observer frame with a call to SPKEZR
			double[] state = new double[6];
			double[] lt = new double[1];
			CSPICE.spkezr(target, et, "IAU_" + observer, "LT+S", observer, state, lt);
			// CSPICE.spkezr(target, et, "J2000", "LT+S",
			// observer+" barycenter", state, lt);

			// Compute target's pointing vector in observer frame.
			double[] trgPt = new double[3];
			trgPt[0] = state[0] - obsSurfacePt[0];
			trgPt[1] = state[1] - obsSurfacePt[1];
			trgPt[2] = state[2] - obsSurfacePt[2];

			// Transform observer frame to surface frame and normalize to unit
			// vector
			trgPt = CSPICE.mxv(obsMatrix, trgPt);
			trgPt = CSPICE.vhat(trgPt);

			// Copy the vector to the output.
			state[0] = trgPt[0];
			state[1] = trgPt[1];
			state[2] = trgPt[2];

			// convert to radius, lon, and lat
			trgPt = CSPICE.reclat(trgPt);

			// Copy the coordinates to the output.
			state[3] = trgPt[0];
			state[4] = trgPt[1];
			state[5] = trgPt[2];

			return (state);
		} catch (Exception e) {
			System.out.println("Unable to get target vector, see log.");
			e.printStackTrace();
			return (null);
		}
	}

}
