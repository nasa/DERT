package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.landscape.FieldCameraLayer;
import gov.nasa.arc.dert.landscape.Layer;
import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.QuadTreeMesh;
import gov.nasa.arc.dert.scene.tool.Grid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.GLSLShaderDataLogic;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.scenegraph.Mesh;

/**
 * Provides a shader program for handling multi-layer effects including shadows,
 * layer blending, and surface grid. Shader program is generated on the fly.
 *
 */
public class LayerEffects extends GLSLShaderObjectsState {

	protected static final String top = "varying vec4 gl_TexCoord[8];\n" + "void main() {\n"
		+ "	vec4 color = vec4(0, 0, 0, 0);\n" + "	vec4 tcolor = vec4(0, 0, 0, 0);\n"
		+ "	vec4 gColor = vec4(gridColor[0], gridColor[1], gridColor[2], 0);\n" + "	float shadeFactor = 1.0;\n"
		+ "	bool hasTexture = false;\n" + "	float x = 0;\n" + "	float y = 0;\n" + "	if (layersEnabled) {\n";

	protected static final String standardUniforms = "uniform float blendFactor[7];\n"
		+ "uniform bool layersEnabled;\n" + "uniform sampler2DShadow shadowUnit;\n" + "uniform bool shadowEnabled;\n"
		+ "uniform float xGridOffset;\n" + "uniform float yGridOffset;\n" + "uniform float xGridCell;\n"
		+ "uniform float yGridCell;\n" + "uniform float gridLineWidth;\n" + "uniform float gridColor[4];\n"
		+ "uniform bool gridEnabled;\n";

	protected static final String bottom = "		if (color.a > 0.0)\n" + "			color.a = 1.0;\n"
		+ "	}\n"
		+ "	if (shadowEnabled) {\n" + "   		shadeFactor = shadow2DProj(shadowUnit, gl_TexCoord[7]).x;\n"
		+ "   		shadeFactor = (shadeFactor < 1.0) ? 0.5 : 1.0;\n" + "	}\n" + "	if (hasTexture)\n"
		+ "		gl_FragColor = vec4(shadeFactor*(color.rgb+(blendFactor[0]*gl_Color.rgb)), gl_Color.a);\n"
		+ "	else if (layersEnabled)\n"
		+ "		gl_FragColor = vec4(shadeFactor*blendFactor[0]*gl_Color.rgb, gl_Color.a);\n"
		+ "	else\n"
		+ "		gl_FragColor = vec4(shadeFactor*gl_Color.rgb, gl_Color.a);\n"
		+ "	if (gridEnabled) {\n"
		+ "		x = gl_TexCoord[0].x-xGridOffset;\n"
		+ "		x = abs(xGridOffset+xGridCell*floor(x/xGridCell)-gl_TexCoord[0].x);\n"
		+ "		y = gl_TexCoord[0].y-yGridOffset;\n"
		+ "		y = abs(yGridOffset+yGridCell*floor(y/yGridCell)-gl_TexCoord[0].y);\n" + "		if (x < gridLineWidth)\n"
		+ "			gl_FragColor = vec4(gridColor[0], gridColor[1], gridColor[2], gl_FragColor.a);\n"
		+ "		else if (y < gridLineWidth)\n"
		+ "			gl_FragColor = vec4(gridColor[0], gridColor[1], gridColor[2], gl_FragColor.a);\n" + "	}\n" + "}\n";

	// show shadows
	public boolean shadowEnabled;

	// show surface grid
	public boolean gridEnabled;

	// show layers
	public boolean layersEnabled = true;

	// layer contribution to overall color
	public float[] blendFactor;

	// size of surface grid cell
	public double gridCell;

	// other surface grid fields
	public float xGridOffset, yGridOffset, xGridCell, yGridCell, lineWidth = 0.01f;

	// RGBA color
	public float[] gridColor;

	// shader uniforms
	protected ArrayList<Object[]> intUniforms;
	protected ArrayList<Object[]> floatArrayUniforms;

	// program text
	protected byte[] fragmentProgram;

	/**
	 * Constructor
	 * 
	 * @param layers
	 * @param oldEffects
	 */
	public LayerEffects(Layer[] layers, LayerEffects oldEffects) {
		blendFactor = new float[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gridColor = new float[] { 1, 1, 1, 1 };
		gridCell = Grid.defaultCellSize;
		if (oldEffects != null) {
			layersEnabled = oldEffects.layersEnabled;
			shadowEnabled = oldEffects.shadowEnabled;
			gridEnabled = oldEffects.gridEnabled;
			gridCell = oldEffects.gridCell;
			System.arraycopy(oldEffects.gridColor, 0, gridColor, 0, gridColor.length);
		}
		setLayers(layers);
	}

	protected void setUniforms() {
		setUniform("layersEnabled", layersEnabled);
		setUniform("gridEnabled", gridEnabled);
		setUniform("shadowEnabled", shadowEnabled);
		setUniform("shadowUnit", ShadowMap.SHADOW_MAP_UNIT);
		setUniform("blendFactor", blendFactor);

		setUniform("xGridOffset", xGridOffset);
		setUniform("yGridOffset", yGridOffset);
		setUniform("xGridCell", xGridCell);
		setUniform("yGridCell", yGridCell);
		setUniform("gridLineWidth", lineWidth);
		setUniform("gridColor", gridColor);

		for (int i = 0; i < intUniforms.size(); ++i) {
			setUniform((String) intUniforms.get(i)[0], (Integer) intUniforms.get(i)[1]);
		}
		for (int i = 0; i < floatArrayUniforms.size(); ++i) {
			setUniform((String) floatArrayUniforms.get(i)[0], (float[]) floatArrayUniforms.get(i)[1]);
		}
	}

	protected void setupGrid(Mesh mesh) {
		QuadTreeMesh qtm = (QuadTreeMesh) mesh;
		ReadOnlyVector3 trans = qtm.getWorldTranslation();

		double xSize = qtm.getTileWidth() * qtm.getPixelWidth();
		double x0 = trans.getX() - xSize / 2;
		xGridOffset = (float) ((Math.ceil(x0 / gridCell) * gridCell - x0));
		xGridOffset /= (float) xSize;
		xGridCell = (float) (gridCell / xSize);

		double ySize = qtm.getTileLength() * qtm.getPixelLength();
		double y0 = trans.getY() + ySize / 2;
		yGridOffset = (float) (y0 - Math.floor(y0 / gridCell) * gridCell);
		yGridOffset /= (float) ySize;
		yGridCell = (float) (gridCell / ySize);

		lineWidth = (float) Math.min(0.5 / qtm.getTileWidth(), 0.5 / qtm.getTileLength());
	}

	/**
	 * Set the layers to be rendered by this layer effects
	 * 
	 * @param layers
	 */
	public void setLayers(Layer[] layers) {
		intUniforms = new ArrayList<Object[]>();
		floatArrayUniforms = new ArrayList<Object[]>();

		String imageUniforms = "";
		String imageFunction = "";
		String overlayFunction = "";
		String colorMapUniforms = "";
		String colorMapFunction = "";
		String footprintUniforms = "";
		String footprintFunction = "";
		String viewshedUniforms = "";
		String viewshedFunction = "";

		for (int i = 0; i < blendFactor.length; ++i) {
			blendFactor[i] = 0;
		}

		// generate shader code for each layer
		boolean addHasTexture = false;
		blendFactor[0] = (float)layers[0].getBlendFactor();
		for (int i = 1; i < layers.length; ++i) {
			if (layers[i] == null) {
				continue;
			}
			if (layers[i].isOverlay()) {
				imageUniforms += "uniform sampler2D photo" + i + "Unit;\n";
				intUniforms.add(new Object[] { "photo" + i + "Unit", new Integer(i-1) });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				overlayFunction += "		tcolor = texture2D(photo" + i + "Unit, gl_TexCoord[0].st);\n";
				overlayFunction += "		if (tcolor.a > 0.5)\n";
				overlayFunction += "			color = blendFactor[" + i + "]*tcolor+color*(1-blendFactor[" + i + "]);\n";
				addHasTexture = true;
			} else if (layers[i].isImage()) {
				imageUniforms += "uniform sampler2D photo" + i + "Unit;\n";
				intUniforms.add(new Object[] { "photo" + i + "Unit", new Integer(i-1) });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				imageFunction += "		color += blendFactor[" + i + "]*texture2D(photo" + i
					+ "Unit, gl_TexCoord[0].st);\n";
				addHasTexture = true;
			} else if (layers[i].getLayerType() == LayerType.floatfield) {
				colorMapUniforms += "uniform sampler2D colorMap" + i + "Unit;\n";
				intUniforms.add(new Object[] { "colorMap" + i + "Unit", new Integer(i-1) });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				colorMapFunction += "		color += blendFactor[" + i + "]*texture2D(colorMap" + i + "Unit, gl_TexCoord["+ (i-1) + "].st);\n";
				addHasTexture = true;
			} else if (layers[i].getLayerType() == LayerType.footprint) {
				footprintUniforms += "uniform sampler2D footprint" + i + "Unit;\n";
				intUniforms.add(new Object[] { "footprint" + i + "Unit", new Integer(i-1) });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				footprintFunction += getFootprintFunction(i);
			} else if (layers[i].getLayerType() == LayerType.viewshed) {
				FieldCameraLayer iLayer = (FieldCameraLayer) layers[i];
				viewshedUniforms += "uniform sampler2DShadow viewshed" + i + "Unit;\n";
				intUniforms.add(new Object[] { "viewshed" + i + "Unit", new Integer(i-1) });
				viewshedUniforms += "uniform float viewshed" + i + "Color[4];\n";
				floatArrayUniforms.add(new Object[] { "viewshed" + i + "Color", iLayer.getColor() });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				viewshedFunction += getViewshedFunction(i);
			}
		}

		// build shader program
		String progStr = "#version 120\n";
		progStr += standardUniforms;
		progStr += imageUniforms;
		progStr += colorMapUniforms;
		progStr += footprintUniforms;
		progStr += viewshedUniforms;
		progStr += top;
		if (addHasTexture) {
			progStr += "		hasTexture = true;\n";
		}
		progStr += imageFunction;
		progStr += colorMapFunction;
		progStr += footprintFunction;
		progStr += viewshedFunction;
		progStr += overlayFunction;
		progStr += bottom;
//		System.err.println("LayerEffects.setLayers");
//		System.err.println(progStr);

		fragmentProgram = progStr.getBytes();

		// load shader program
		InputStream iStream = null;
		try {
			iStream = new ByteArrayInputStream(fragmentProgram);
			setFragmentShader(iStream, "frag");
			iStream.close();
			setUniforms();
			setShaderDataLogic(new GLSLShaderDataLogic() {
				@Override
				public void applyData(GLSLShaderObjectsState shader, Mesh mesh, Renderer renderer) {
					if (!(mesh instanceof QuadTreeMesh)) {
						throw new IllegalStateException();
					}
					setupGrid(mesh);
					setUniforms();
				}
			});
		} catch (final IOException ex) {
			ex.printStackTrace();
		} finally {
			if (iStream != null) {
				try {
					iStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String getViewshedFunction(int i) {
		String str = "		vec4 vscol" + i + " = vec4(viewshed" + i + "Color[0], viewshed" + i + "Color[1], viewshed" + i
			+ "Color[2], viewshed" + i + "Color[3]);\n" + "		if (gl_TexCoord[" + (i-1) + "].q > 0.0) {\n"
			+ "			float d = shadow2DProj(viewshed" + i + "Unit, gl_TexCoord[" + (i-1) + "]).x;\n"
			+ "			d = d < 1.0 ? 0.0 : 1.0;\n" + "			color += blendFactor[" + i + "]*d*vscol" + i + ";\n"
			+ "			hasTexture = true;\n" + "		}\n";
		return (str);
	}

	private String getFootprintFunction(int i) {
		String str = "		if (gl_TexCoord[" + (i-1) + "].q > 0.0) {\n" + "			color += blendFactor[" + i
			+ "]*texture2DProj(footprint" + i + "Unit, gl_TexCoord[" + (i-1) + "]);\n" + "			hasTexture = true;\n"
			+ "		}\n";
		return (str);
	}

}