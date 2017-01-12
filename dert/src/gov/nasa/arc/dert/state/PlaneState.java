package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.contour.ContourScenePanel;
import gov.nasa.arc.dert.view.contour.ContourView;

import java.util.HashMap;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the Plane tool.
 *
 */
public class PlaneState extends ToolState {

	// Show the triangle
	public boolean triangleVisible;

	// Three points of the triangle
	public Vector3 p0, p1, p2;
	public double zOff0, zOff1, zOff2;

	// Dimensional scales
	public double lengthScale, widthScale;

	// Color map
	public String colorMapName = Plane.defaultColorMap;
	public boolean gradient;
	public double minimum = 0;
	public double maximum = 100;

	// The display panel for the elevation difference map
	protected transient ContourScenePanel panel;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public PlaneState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Plane), MapElementState.Type.Plane, "Plane",
			Plane.defaultSize, Plane.defaultColor, Plane.defaultLabelVisible);
		p0 = new Vector3(position);
		p1 = new Vector3(Landscape.getInstance().getCenter());
		p1.subtractLocal(p0);
		p1.normalizeLocal();
		p2 = new Vector3(p1);
		p1.multiplyLocal(Grid.defaultCellSize / 2);
		p1.addLocal(p0);
		p1.setZ(Landscape.getInstance().getZ(p1.getX(), p1.getY()));
		p2.multiplyLocal(Grid.defaultCellSize / 4);
		p2.addLocal(p0);
		p2.setZ(Landscape.getInstance().getZ(p2.getX(), p2.getY()));

		triangleVisible = Plane.defaultTriangleVisible;
		lengthScale = Plane.defaultSize;
		widthScale = Plane.defaultSize;
		double s = Landscape.getInstance().getPixelWidth();
		if (s < 1) {
			lengthScale *= s;
			widthScale *= s;
		}
		viewData = new ViewData(-1, -1, 550, 400, false);
	}
	
	/**
	 * Constructor for hash map.
	 */
	public PlaneState(HashMap<String,Object> map) {
		super(map);
		p0 = StateUtil.getVector3(map, "P0", Vector3.ZERO);
		p1 = StateUtil.getVector3(map, "P1", Vector3.ZERO);
		p2 = StateUtil.getVector3(map, "P2", Vector3.ZERO);
		triangleVisible = StateUtil.getBoolean(map, "TriangleVisible", Plane.defaultTriangleVisible);
		lengthScale = StateUtil.getDouble(map, "LengthScale", Plane.defaultSize);
		widthScale = StateUtil.getDouble(map, "WidthScale", Plane.defaultSize);
		colorMapName = StateUtil.getString(map, "ColorMapName", Plane.defaultColorMap);
		minimum = StateUtil.getDouble(map, "ColorMapMinimum", minimum);
		maximum = StateUtil.getDouble(map, "ColorMapMaximum", maximum);
		zOff0 = StateUtil.getDouble(map, "ZOffset0", 0);
		zOff1 = StateUtil.getDouble(map, "ZOffset1", 0);
		zOff2 = StateUtil.getDouble(map, "ZOffset2", 0);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof PlaneState)) 
			return(false);
		PlaneState that = (PlaneState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (this.triangleVisible != that.triangleVisible)
			return(false);
		if (this.lengthScale != that.lengthScale)
			return(false);
		if (this.widthScale != that.widthScale)
			return(false);
		if (this.minimum != that.minimum)
			return(false);
		if (this.maximum != that.maximum)
			return(false);
		if (this.zOff0 != that.zOff0)
			return(false);
		if (this.zOff1 != that.zOff1)
			return(false);
		if (this.zOff2 != that.zOff2)
			return(false);
		if (!this.p0.equals(that.p0)) 
			return(false);
		if (!this.p1.equals(that.p1)) 
			return(false);
		if (!this.p2.equals(that.p2)) 
			return(false);
		if (!this.colorMapName.equals(that.colorMapName)) 
			return(false);
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			Plane plane = (Plane) mapElement;
			p0 = new Vector3(plane.getPoint(0));
			p1 = new Vector3(plane.getPoint(1));
			p2 = new Vector3(plane.getPoint(2));
			triangleVisible = plane.isTriangleVisible();
			lengthScale = plane.getLengthScale();
			widthScale = plane.getWidthScale();
			zOff0 = plane.getMarker(0).getZOffset();
			zOff1 = plane.getMarker(1).getZOffset();
			zOff2 = plane.getMarker(2).getZOffset();
		}
		if (panel != null) {
			ColorMap colorMap = panel.getColorMap();
			colorMapName = colorMap.getName();
			gradient = colorMap.isGradient();
			minimum = colorMap.getMinimum();
			maximum = colorMap.getMaximum();
		}
		
		StateUtil.putVector3(map, "P0", p0);
		StateUtil.putVector3(map, "P1", p1);
		StateUtil.putVector3(map, "P2", p2);
		map.put("TriangleVisible", new Boolean(triangleVisible));
		map.put("LengthScale", new Double(lengthScale));
		map.put("WidthScale", new Double(widthScale));
		map.put("ColorMapName", colorMapName);
		map.put("ColorMapMinimum", new Double(minimum));
		map.put("ColorMapMaximum", new Double(maximum));
		map.put("ZOffset0", new Double(zOff0));
		map.put("ZOffset1", new Double(zOff1));
		map.put("ZOffset2", new Double(zOff2));
		
		return(map);
	}

	@Override
	public void setView(View view) {
		super.setView(view);
		panel = ((ContourView) view).getContourScenePanel();
		panel.setDraw(true);
	}
	
	@Override
	public void createView() {
		setView(new ContourView(this));
		viewData.createWindow(Dert.getMainWindow(), name + " Elevation Difference Map", X_OFFSET, Y_OFFSET);
	}
	
	@Override
	public String toString() {
		String str = "["+triangleVisible+","+lengthScale+","+widthScale+","+colorMapName+","+gradient+","+minimum+","+maximum+","+p0+","+p1+","+p2+"]"+super.toString();
		return(str);
	}
}
