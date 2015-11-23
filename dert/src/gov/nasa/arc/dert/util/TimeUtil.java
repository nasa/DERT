package gov.nasa.arc.dert.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Provides methods for time conversion.
 *
 */
public class TimeUtil {

	private static int secondsInADay = 86400;
	private static int secondsInAnHr = 3600;
	private static int secondsInAMin = 60;
	private long epoch;
	private double toEarth;
	private SimpleDateFormat utcDateFormat;

	/**
	 * Constructor
	 * 
	 * @param epoch
	 * @param toEarth
	 */
	public TimeUtil(long epoch, double toEarth) {
		this.epoch = epoch;
		this.toEarth = toEarth;
		utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Convert Unix time to local mean solar time
	 * 
	 * @param time
	 * @return
	 */
	public int[] time2LMST(long time) {
		int[] result = new int[4];
		long marsTime = Math.round((time - epoch) / 1000f / toEarth);
		result[0] = (int) (marsTime / (secondsInADay));
		marsTime = (int) (marsTime % secondsInADay);
		result[1] = Math.abs((int) (marsTime / secondsInAnHr));
		marsTime = (int) (marsTime % secondsInAnHr);
		result[2] = Math.abs((int) (marsTime / secondsInAMin));
		result[3] = Math.abs((int) (marsTime % secondsInAMin));
		return (result);
	}

	/**
	 * Convert local mean solar time to Unix time
	 * 
	 * @param sol
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	public long lmst2Time(int sol, int hour, int minute, int second) {
		long earthTime = sol * secondsInADay + hour * secondsInAnHr + minute * secondsInAMin + second;
		earthTime = (long) (earthTime * toEarth * 1000 + epoch + 0.5f);
		return (earthTime);
	}

}
