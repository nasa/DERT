package gov.nasa.arc.dert.viewpoint;

import java.io.Serializable;

/**
 * Data structure that provides fly through parameters and can be persisted.
 *
 */
public class FlyThroughParameters implements Serializable {

	public int numInbetweens;
	public int millisPerFrame;
	public double pathHeight;
	public boolean loop;
	
	public FlyThroughParameters() {
		numInbetweens = 10;
		millisPerFrame = 100;
		pathHeight = 5;
		loop = false;
	}
	
	public FlyThroughParameters(int numInbetweens, int millisPerFrame, double pathHeight, boolean loop) {
		this.numInbetweens = numInbetweens;
		this.millisPerFrame = millisPerFrame;
		this.pathHeight = pathHeight;
		this.loop = loop;
	}
	
	public double[] toArray() {
		double[] array = new double[4];
		array[0] = numInbetweens;
		array[1] = millisPerFrame;
		array[2] = pathHeight;
		array[3] = loop ? 1 : 0;
		return(array);
	}
	
	public static FlyThroughParameters fromArray(double[] array) {
		if (array == null)
			return(new FlyThroughParameters());
		FlyThroughParameters params = new FlyThroughParameters((int)array[0], (int)array[1], array[2], array[3] == 1);
		return(params);
	}
	
	@Override
	public String toString() {
		String str = "FlyThroughParameters["+numInbetweens+","+millisPerFrame+","+pathHeight+","+loop+"]";
		return(str);
	}

}
