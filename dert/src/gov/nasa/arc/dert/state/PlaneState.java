package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.scenegraph.MotionListener;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.contour.ContourScenePanel;
import gov.nasa.arc.dert.view.contour.ContourView;
import gov.nasa.arc.dert.view.mapelement.NotesDialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
	public double minimum = Double.NaN;
	public double maximum = Double.NaN;

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
		p1.multiplyLocal(Landscape.defaultCellSize / 2);
		p1.addLocal(p0);
		p1.setZ(Landscape.getInstance().getZ(p1.getX(), p1.getY()));
		p2.multiplyLocal(Landscape.defaultCellSize / 4);
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
		viewData = new ViewData(550, 400, false);
	}
	
	/**
	 * Constructor for hash map.
	 */
	public PlaneState(Map<String,Object> map) {
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
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
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
//		viewData.createWindow(Dert.getMainWindow(), name + " Elevation Difference Map", X_OFFSET, Y_OFFSET);
		viewData.createWindow(Dert.getMainWindow(), name + " Elevation Difference Map");
	}
	
	@Override
	public String toString() {
		String str = "["+triangleVisible+","+lengthScale+","+widthScale+","+colorMapName+","+gradient+","+minimum+","+maximum+","+p0+","+p1+","+p2+"]"+super.toString();
		return(str);
	}

	/**
	 * Set the MapElement
	 * 
	 * @param me
	 */
	@Override
	public void setMapElement(MapElement me) {
		mapElement = me;
		Plane plane = (Plane)me;
		for (int i=0; i<3; ++i) {
			Movable movable = (Movable) plane.getMarker(i);
			movable.addMotionListener(new MotionListener() {
				@Override
				public void move(Movable mo, ReadOnlyVector3 pos) {
					if (annotationDialog != null) {
						annotationDialog.update();
					}
					if (editDialog != null) {
						editDialog.update();
					}
				}
			});
		}
	}
	
	/**
	 * Open the annotation
	 */
	@Override
	public NotesDialog openAnnotation() {
		if (mapElement == null)
			return(null);
		if (annotationDialog == null)
			annotationDialog = new NotesDialog(Dert.getMainWindow(), name, 400, 200, mapElement) {
				private JLabel p0, p1, p2, l0, l1, l2, strikeNDip;
				@Override
				protected void buildLocation(JPanel thePanel) {
					JPanel panel = new JPanel(new GridLayout(4, 1));
					panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					JPanel topPanel = new JPanel(new BorderLayout());
					topPanel.setBorder(BorderFactory.createEmptyBorder());
					p0 = new JLabel(" ");
					l0 = new JLabel(new ImageIcon());
					topPanel.add(new JLabel("Point 0: ", SwingConstants.RIGHT), BorderLayout.WEST);
					topPanel.add(p0, BorderLayout.CENTER);
					topPanel.add(l0, BorderLayout.EAST);
					panel.add(topPanel);
					topPanel = new JPanel(new BorderLayout());
					topPanel.setBorder(BorderFactory.createEmptyBorder());
					p1 = new JLabel(" ");
					l1 = new JLabel(new ImageIcon());
					topPanel.add(new JLabel("Point 1: ", SwingConstants.RIGHT), BorderLayout.WEST);
					topPanel.add(p1, BorderLayout.CENTER);
					topPanel.add(l1, BorderLayout.EAST);
					panel.add(topPanel);
					topPanel = new JPanel(new BorderLayout());
					topPanel.setBorder(BorderFactory.createEmptyBorder());
					p2 = new JLabel(" ");
					l2 = new JLabel(new ImageIcon());
					topPanel.add(new JLabel("Point 2: ", SwingConstants.RIGHT), BorderLayout.WEST);
					topPanel.add(p2, BorderLayout.CENTER);
					topPanel.add(l2, BorderLayout.EAST);
					panel.add(topPanel);
					strikeNDip = new JLabel(" ");
					panel.add(strikeNDip);
					thePanel.add(panel, BorderLayout.NORTH);
				}
				protected void updateLocation() {
					Plane plane = (Plane)mapElement;
					if (mapElement.isLocked()) {
						l0.setIcon(locked);
						l1.setIcon(locked);
						l2.setIcon(locked);
					}
					else {
						l0.setIcon(null);
						l1.setIcon(null);
						l2.setIcon(null);
					}
					p0.setText(StringUtil.format(plane.getPointInWorld(0)));
					p1.setText(StringUtil.format(plane.getPointInWorld(1)));
					p2.setText(StringUtil.format(plane.getPointInWorld(2)));
					String str = "Strike: ";
					if (Plane.strikeAsCompassBearing) {
						str += StringUtil.azimuthToCompassBearing(plane.getStrike());
					} else {
						str += StringUtil.format(plane.getStrike()) + StringUtil.DEGREE;
					}
					str += "   Dip: " + StringUtil.format(plane.getDip()) + StringUtil.DEGREE;
					strikeNDip.setText(str);
				}
			};
		else
			annotationDialog.update();
		annotationDialog.open();
		return(annotationDialog);
	}

}
