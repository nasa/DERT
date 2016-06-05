package gov.nasa.arc.dert.viewpoint;


/**
 * Data structure that provides fly through parameters and can be persisted.
 *
 */
public class FlyThroughParameters {

	public int numInbetweens;
	public int millisPerFrame;
	public double pathHeight;
	public boolean loop;
	public boolean grab;
	public String imageSequencePath;
	
	public FlyThroughParameters() {
		numInbetweens = 10;
		millisPerFrame = 100;
		pathHeight = 5;
		loop = false;
		grab = false;
	}
	
	public FlyThroughParameters(int numInbetweens, int millisPerFrame, double pathHeight, boolean loop, boolean grab) {
		this.numInbetweens = numInbetweens;
		this.millisPerFrame = millisPerFrame;
		this.pathHeight = pathHeight;
		this.loop = loop;
		this.grab = grab;
	}
	
	public double[] toArray() {
		double[] array = new double[5];
		array[0] = numInbetweens;
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
		String str = "FlyThroughParameters["+numInbetweens+","+millisPerFrame+","+pathHeight+","+loop+","+grab+","+imageSequencePath+"]";
		return(str);
	}

}
