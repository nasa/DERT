package gov.nasa.arc.dert.landscape;

import gov.nasa.arc.dert.io.TileSource;
import gov.nasa.arc.dert.util.StringUtil;

import java.io.IOException;
import java.util.Properties;

import com.ardor3d.math.Vector3;

public class RangeLayer
	extends RasterLayer {
	
	protected double hScale, vScale, hCenter, vCenter;
	protected Vector3 cameraOrigin;
	protected Vector3 tmpVec;
	
	public RangeLayer(LayerInfo layerInfo, TileSource source) throws IOException {
		super(layerInfo, source);
		tmpVec = new Vector3();
	}

	@Override
	protected void initialize(TileSource dataSource, String layerName) throws IOException {
		super.initialize(dataSource, layerName);
	}
	
	protected void loadCameraModel() {
		Properties prop = getProperties();
		double[] c = StringUtil.getDoubleArray(prop, "RangeMap.Source.CameraModel.C", null, true);
		double[] a = StringUtil.getDoubleArray(prop, "RangeMap.Source.CameraModel.A", null, true);
		double[] h = StringUtil.getDoubleArray(prop, "RangeMap.Source.CameraModel.H", null, true);
		double[] v = StringUtil.getDoubleArray(prop, "RangeMap.Source.CameraModel.V", null, true);
		cameraOrigin = new Vector3(c[0], c[1], c[2]);
		Vector3 aVec = new Vector3(a[0], a[1], a[2]);
		Vector3 hVec = new Vector3(h[0], h[1], h[2]);
		Vector3 vVec = new Vector3(v[0], v[1], v[2]);
		aVec.cross(vVec, tmpVec);
		hScale = tmpVec.length();
		hCenter = aVec.dot(hVec);
		aVec.cross(vVec, tmpVec);
		vScale = tmpVec.length();
		vCenter = aVec.dot(vVec);		
	}
	
	public void imageToWorld(float col, float row, float val, float[] store) {
		double x = col-hCenter;
		double y = row*hScale/vScale-vCenter;
		double z = hScale;
		tmpVec.set(x, y, z);
		tmpVec.normalizeLocal();
		tmpVec.scaleAddLocal(val, cameraOrigin);
		tmpVec.toFloatArray(store);
	}

}
