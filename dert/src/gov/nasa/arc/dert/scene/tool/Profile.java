package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.io.CsvWriter;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scenegraph.BillboardMarker;
import gov.nasa.arc.dert.scenegraph.Marker;
import gov.nasa.arc.dert.scenegraph.MotionListener;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.ProfileState;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.world.GroundEdit;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a profile of the terrain.
 *
 */
public class Profile extends Node implements ViewDependent, Tool {

	public static final Icon icon = Icons.getImageIcon("profile_16.png");

	// Defaults
	public static float defaultSize = 1;
	public static Color defaultColor = Color.blue;
	public static boolean defaultLabelVisible = true;
	public static float defaultLineWidth = 2.0f;

	// Endpoint icon texture
	protected static Texture texture, highlightTexture;

	// Scene graph elements
	private BillboardMarker endpointA;
	private BillboardMarker endpointB;
	private Line line;

	// Transect line
	private int xDim, yDim;
	private float[] vertex;

	// Endpoint locations
	private Vector3 pALoc, pBLoc;

	// Graph line
	private float[] graphVertex;
	private Vector3 coord;
	private float graphXMin, graphXMax;
	private float graphYMin, graphYMax;
	private int vertexCount;

	// Attributes
	private double size;
	private ColorRGBA colorRGBA;
	private Color color;
	private boolean labelVisible, locked;
	private float lineWidth;

	// MapElement state
	private ProfileState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Profile(ProfileState pstate) {
		super(pstate.name);
		this.state = pstate;
		this.labelVisible = state.labelVisible;
		this.color = state.color;
		this.size = state.size;
		this.lineWidth = state.lineWidth;
		pALoc = new Vector3(state.p0);
		pBLoc = new Vector3(state.p1);
		colorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
			color.getAlpha() / 255f);
		Landscape landscape = Landscape.getInstance();
		xDim = landscape.getRasterWidth();
		yDim = landscape.getRasterLength();
		vertex = new float[3 * (int) (Math.sqrt((long) xDim * xDim + (long) yDim * yDim) + 1024)];
		graphVertex = new float[vertex.length];
		coord = new Vector3();
		vertex[0] = pALoc.getXf();
		vertex[1] = pALoc.getYf();
		vertex[2] = pALoc.getZf();
		vertex[3] = pBLoc.getXf();
		vertex[4] = pBLoc.getYf();
		vertex[5] = pBLoc.getZf();

		endpointA = createMarker(state.name + "_A", pALoc, state.zOff0, color);
		endpointA.addMotionListener(new MotionListener() {
			@Override
			public void move(Movable mo, ReadOnlyVector3 pos) {
				pALoc.set(pos);
				updateGraph();
				state.move(mo, pos);
				updateGeometricState(0);
			}
		});
		attachChild(endpointA);

		endpointB = createMarker(state.name + "_B", pBLoc, state.zOff1, color);
		endpointB.addMotionListener(new MotionListener() {
			@Override
			public void move(Movable mo, ReadOnlyVector3 pos) {
				pBLoc.set(pos);
				updateGraph();
				state.move(mo, pos);
				updateGeometricState(0);
			}
		});
		attachChild(endpointB);

		createLine();

		// keep the overlays from coloring the profile
		TextureState textureState = new TextureState();
		textureState.setEnabled(false);
		setRenderState(textureState);

		setVisible(state.visible);
		setEndpointsVisible(state.endpointsVisible);
		state.setMapElement(this);
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		// do nothing
	}

	/**
	 * Get the map element state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Update the graph
	 */
	public void updateGraph() {
		buildLine();
		if (vertexCount > -1) {
			state.setData(graphVertex, vertexCount, graphXMin, graphXMax, graphYMin, graphYMax, vertex);
		}
	}

	protected BillboardMarker createMarker(String name, ReadOnlyVector3 point, double zOff, Color color) {
		if (texture == null) {
			texture = ImageUtil.createTexture(Icons.getIconURL("paddle.png"), true);
			highlightTexture = ImageUtil.createTexture(Icons.getIconURL("paddle-highlight.png"), true);
		}
		BillboardMarker bm = new BillboardMarker(name, point, size, zOff, color, labelVisible, false) {
			@Override
			public boolean isLocked() {
				return(Profile.this.locked);
			}
		};
		bm.setTexture(texture, highlightTexture);
		return (bm);
	}

	protected void createLine() {
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertex.length);
		vertexBuffer.limit(0);
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(vertex.length / 3);
		indexBuffer.limit(0);
		line = new Line("_polyline", vertexBuffer, null, null, null);
		line.getSceneHints().setCastsShadows(false);
		line.getMeshData().setIndexBuffer(indexBuffer);
		line.getMeshData().setIndexMode(IndexMode.LineStrip);
		line.setLineWidth(lineWidth);
		line.getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
		line.setModelBound(new BoundingBox());
		line.updateModelBound();
		MaterialState ms = new MaterialState();
		ms.setDiffuse(new ColorRGBA(0, 0, 0, 1));
		ms.setAmbient(new ColorRGBA(0, 0, 0, 1));
		ms.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		ms.setEnabled(true);
		line.setRenderState(ms);
		line.getSceneHints().setAllPickingHints(false);

		buildLine();
		attachChild(line);
	}

	protected void buildLine() {
		// Get vertices along the trancept from the Landscape
		Landscape landscape = Landscape.getInstance();
		int n = landscape.getVertices(vertex, pALoc, pBLoc, false, true);
		FloatBuffer vertexBuffer = line.getMeshData().getVertexBuffer();
		vertexBuffer.clear();
		vertexBuffer.put(vertex, 0, n);
		vertexBuffer.limit(n);
		vertexBuffer.rewind();

		// convert to world coordinates
		for (int i = 0; i < n; i += 3) {
			coord.set(vertex[i], vertex[i + 1], vertex[i + 2]);
			landscape.localToWorldCoordinate(coord);
			vertex[i] = coord.getXf();
			vertex[i + 1] = coord.getYf();
			vertex[i + 2] = coord.getZf();
		}
		n /= 3;
		IntBuffer indexBuffer = (IntBuffer) line.getMeshData().getIndexBuffer();
		indexBuffer.clear();
		for (int i = 0; i < n; ++i) {
			indexBuffer.put(i, i);
		}
		indexBuffer.limit(n);
		indexBuffer.rewind();
		line.updateModelBound();
		computeGraphVertices(n, vertex);
		vertexCount = n;
	}

	protected void computeGraphVertices(int numVertex, float[] vertex) {
		graphXMin = Float.MAX_VALUE;
		graphXMax = -Float.MAX_VALUE;
		graphYMin = Float.MAX_VALUE;
		graphYMax = -Float.MAX_VALUE;
		int iii = 0;
		int ii = 0;
		float lastX = vertex[0];
		float lastY = vertex[1];
		float lastGraphX = 0;
		for (int i = 0; i < numVertex; ++i) {
			iii = i * 3;
			ii = i*2;
			float dx = vertex[iii] - lastX;
			float dy = vertex[iii + 1] - lastY;
			graphVertex[ii] = (float) Math.sqrt(dx * dx + dy * dy) + lastGraphX;
			graphVertex[ii + 1] = vertex[iii + 2];
			if (graphVertex[ii] > graphXMax) {
				graphXMax = graphVertex[ii];
			}
			if (graphVertex[ii] < graphXMin) {
				graphXMin = graphVertex[ii];
			}
			if (graphVertex[ii + 1] > graphYMax) {
				graphYMax = graphVertex[ii + 1];
			}
			if (graphVertex[ii + 1] < graphYMin) {
				graphYMin = graphVertex[ii + 1];
			}
			lastX = vertex[iii];
			lastY = vertex[iii + 1];
			lastGraphX = graphVertex[ii];
		}
	}

	/**
	 * Update view dependent parts
	 */
	@Override
	public void update(BasicCamera camera) {
		endpointA.update(camera);
		endpointB.update(camera);
	}

	/**
	 * Get the color
	 */
	@Override
	public Color getColor() {
		return (color);
	}

	/**
	 * Get the size
	 */
	@Override
	public double getSize() {
		return (size);
	}

	/**
	 * Determine label visibility
	 */
	@Override
	public boolean isLabelVisible() {
		return (endpointA.isLabelVisible());
	}

	/**
	 * Set label visibility
	 */
	@Override
	public void setLabelVisible(boolean visible) {
		endpointA.setLabelVisible(visible);
		endpointB.setLabelVisible(visible);
	}

	/**
	 * Get point and distance to seek to
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		point.set(pALoc);
		return (endpointA.getRadius() * 1.5);
	}

	/**
	 * Get position of endpoint A.
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getEndpointA() {
		return (endpointA.getLocation());
	}

	/**
	 * Set position of endpoint A
	 * 
	 * @param pALoc
	 */
	public void setEndpointA(double x, double y, double z) {
		endpointA.setLocation(x, y, z, true);
		this.pALoc.set(x, y, z);
		updateGraph();
		updateGeometricState(0);
	}

	/**
	 * Get position of endpoint B
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getEndpointB() {
		return (endpointB.getLocation());
	}

	/**
	 * Set position of endpoint B
	 * 
	 * @param pBLoc
	 */
	public void setEndpointB(double x, double y, double z) {
		endpointB.setLocation(x, y, z, true);
		this.pBLoc.set(x, y, z);
		updateGraph();
		updateGeometricState(0);
	}
	
	public Marker getMarkerA() {
		return(endpointA);
	}
	
	public Marker getMarkerB() {
		return(endpointB);
	}
	
	public void setEndpointsVisible(boolean endpointsVisible) {
		endpointA.getSceneHints().setCullHint(endpointsVisible ? CullHint.Inherit : CullHint.Always);
		endpointB.getSceneHints().setCullHint(endpointsVisible ? CullHint.Inherit : CullHint.Always);
	}
	
	public boolean isEndpointsVisible() {
		return(SpatialUtil.isDisplayed(endpointA));
	}

	/**
	 * Determine visibility
	 */
	@Override
	public boolean isVisible() {
		return (SpatialUtil.isDisplayed(this));
	}

	/**
	 * Set visibility
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			getSceneHints().setCullHint(CullHint.Dynamic);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Determine mobility
	 */
	@Override
	public boolean isLocked() {
		return (locked);
	}

	/**
	 * Set mobility
	 */
	@Override
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * Update Z coordinates when landscape changes
	 */
	@Override
	public boolean updateElevation(QuadTree quadTree) {
		endpointA.updateElevation(quadTree);
		endpointB.updateElevation(quadTree);
		return (false);
	}

	/**
	 * Get the MapElement type
	 */
	@Override
	public Type getType() {
		return (Type.Profile);
	}

	/**
	 * Set color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
		endpointA.setColor(color);
		endpointB.setColor(color);
		colorRGBA = UIUtil.colorToColorRGBA(color);
		MaterialState ms = (MaterialState) line.getLocalRenderState(RenderState.StateType.Material);
		ms.setDiffuse(ColorRGBA.BLACK);
		ms.setAmbient(ColorRGBA.BLACK);
		ms.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		state.setColor(color);
		line.markDirty(DirtyType.RenderState);
	}
	
	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
		line.setLineWidth(lineWidth);
		line.markDirty(DirtyType.RenderState);
	}
	
	public float getLineWidth() {
		return(lineWidth);
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Set vertical exaggeration
	 */
	@Override
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		endpointA.setVerticalExaggeration(vertExag, oldVertExag, minZ);
		endpointB.setVerticalExaggeration(vertExag, oldVertExag, minZ);
		line.setScale(1, 1, vertExag);
	}
	
	public GroundEdit ground() {
		GroundEdit[] ge = new GroundEdit[2];
		ge[0] = endpointA.ground();
		ge[1] = endpointB.ground();
		return(new GroundEdit(this, ge));
	}
	
	public void setZOffset(double zOff, boolean doTrans) {
		// do nothing
	}
	
	public double getZOffset() {
		return(0);
	}

	/**
	 * Set defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.Profile.defaultColor", defaultColor, false);
		defaultSize = (float) StringUtil.getDoubleValue(properties, "MapElement.Profile.defaultSize", true,
			defaultSize, false);
		defaultLineWidth = (float) StringUtil.getDoubleValue(properties, "MapElement.Profile.defaultLineWidth", true,
				defaultLineWidth, false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.Profile.defaultLabelVisible",
			defaultLabelVisible, false);
	}

	/**
	 * Get defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.Profile.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.Profile.defaultSize", Float.toString(defaultSize));
		properties.setProperty("MapElement.Profile.defaultLineWidth", Float.toString(defaultLineWidth));
		properties.setProperty("MapElement.Profile.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
	}

	/**
	 * Save vertices to comma separated value formatted file
	 * 
	 * @param filename
	 */
	public void saveAsCsv(String filename) {
		CsvWriter csvWriter = null;
		String[] column = { "Index", "X", "Y", "Z", "Dist" };
		try {
			csvWriter = new CsvWriter(filename, column);
			csvWriter.open();
			String[] value = new String[column.length];
			for (int i = 0; i < vertexCount; ++i) {
				value[0] = Integer.toString(i);
				value[1] = Double.toString(vertex[i*3]);
				value[2] = Double.toString(vertex[i*3 + 1]);
				value[3] = Double.toString(graphVertex[i*2 + 1]);
				value[4] = Double.toString(graphVertex[i*2]);
				csvWriter.writeLine(value);
			}
			csvWriter.close();
			Console.println(vertexCount + " records saved to " + filename);
		} catch (Exception e) {
			e.printStackTrace();
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (Exception e2) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String toString() {
		return (getName());
	}

	/**
	 * Get the location
	 */
	@Override
	public ReadOnlyVector3 getLocationInWorld() {
		return (endpointA.getLocationInWorld());
	}
}
