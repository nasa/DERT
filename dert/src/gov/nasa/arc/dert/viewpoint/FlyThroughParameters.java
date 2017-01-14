package gov.nasa.arc.dert.viewpoint;


/**
 * Data structure that provides fly through parameters and can be persisted.
 *
 */
public class FlyThroughParameters {

	public int numFrames;
	public int millisPerFrame;
	public double pathHeight;
	public boolean loop;
	public boolean grab;
	public String imageSequencePath;
	
	public FlyThroughParameters() {
		numFrames = 100;
		millisPerFrame = 100;
		pathHeight = 5;
		loop = false;
		grab = false;
	}
	
	public FlyThroughParameters(int numFrames, int millisPerFrame, double pathHeight, boolean loop, boolean grab) {
		this.numFrames = numFrames;
		this.millisPerFrame = millisPerFrame;
		this.pathHeight = pathHeight;
		this.loop = loop;
		this.grab = grab;
	}
	
	public double[] toArray() {
		double[] array = new double[5];
		array[0] = numFrames;
		array[1] = millisPerFrame;
		array[2] = pathHeight;
		array[3] = loop ? 1 : 0;
		array[4] = grab ? 1 : 0;
		return(array);
	}
	
	public static FlyThroughParameters fromArray(double[] array) {
		if (array == null)
			return(new FlyThroughParameters());
		FlyThroughParameters params = new FlyThroughParameters((int)array[0], (int)array[1], array[2], array[3] == 1, array[4] == 1);
		return(params);
	}
	
	@Override
	public String toString() {
		String str = "FlyThroughParameters["+numFrames+","+millisPerFrame+","+pathHeight+","+loop+","+grab+","+imageSequencePath+"]";
		return(str);
	}

}
