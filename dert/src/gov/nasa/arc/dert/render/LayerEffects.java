/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.layer.FieldCameraLayer;
import gov.nasa.arc.dert.landscape.layer.Layer;
import gov.nasa.arc.dert.landscape.layer.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;

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

	protected static final String top =
		  "varying vec4 gl_TexCoord[8];\n"
		+ "void main() {\n"
		+ "	vec4 color = vec4(gl_Color);\n"
		+ "	vec4 tcolor = vec4(0, 0, 0, 0);\n"
		+ "	vec4 gColor = vec4(gridColor[0], gridColor[1], gridColor[2], 0);\n"
		+ "	float shadeFactor = 1.0;\n"
		+ "	bool hasTexture = false;\n"
		+ "	float x = 0;\n"
		+ "	float y = 0;\n"
		+ "	float f = 0;\n"
		+ "	if (layersEnabled) {\n";

	protected static final String standardUniforms = 
		  "uniform float blendFactor[7];\n"
		+ "uniform bool layersEnabled;\n"
		+ "uniform sampler2DShadow shadowUnit;\n"
		+ "uniform bool shadowEnabled;\n"
		+ "uniform bool allDark;\n"
		+ "uniform float xGridOffset;\n"
		+ "uniform float yGridOffset;\n"
		+ "uniform float xGridCell;\n"
		+ "uniform float yGridCell;\n"
		+ "uniform float gridLineWidth;\n"
		+ "uniform float gridColor[4];\n"
		+ "uniform bool gridEnabled;\n";

	protected static final String bottom =
		  "		if (color.a > 0.0)\n"
		+ "			color.a = 1.0;\n"
		+ "	}\n"
		+ "	if (shadowEnabled) {\n"
		+ "   		shadeFactor = shadow2DProj(shadowUnit, gl_TexCoord[7]).x;\n"
		+ "   		shadeFactor = ((shadeFactor < 1.0) || allDark) ? 0.5 : 1.0;\n"
		+ "	}\n"
		+ "	if (hasTexture)\n"
		+ "		gl_FragColor = vec4(shadeFactor*color.rgb, color.a);\n"
		+ "	else if (layersEnabled)\n"
		+ "		gl_FragColor = vec4(shadeFactor*blendFactor[0]*gl_Color.rgb, gl_Color.a);\n"
		+ "	else\n"
		+ "		gl_FragColor = vec4(shadeFactor*gl_Color.rgb, gl_Color.a);\n"
		+ "	if (gridEnabled) {\n"
		+ "		x = gl_TexCoord[0].x-xGridOffset;\n"
		+ "		x = abs(xGridOffset+xGridCell*floor(x/xGridCell)-gl_TexCoord[0].x);\n"
		+ "		y = gl_TexCoord[0].y-yGridOffset;\n"
		+ "		y = abs(yGridOffset+yGridCell*floor(y/yGridCell)-gl_TexCoord[0].y);\n"
		+ "		if (x < gridLineWidth)\n"
		+ "			gl_FragColor = vec4(gridColor[0], gridColor[1], gridColor[2], gl_FragColor.a);\n"
		+ "		else if (y < gridLineWidth)\n"
		+ "			gl_FragColor = vec4(gridColor[0], gridColor[1], gridColor[2], gl_FragColor.a);\n" + "	}\n" + "}\n";

	// show shadows
	public boolean shadowEnabled;
	public boolean allDark;

	// show surface grid
	public boolean gridEnabled;

	// show layers
	public boolean layersEnabled = false;

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
		blendFactor = new float[] { 0, 0, 0, 0, 0, 0, 0 };
		gridColor = new float[] { 1, 1, 1, 1 };
		gridCell = Landscape.defaultCellSize;
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
		setUniform("allDark", allDark);
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
	protected void setLayers(Layer[] layers) {
		intUniforms = new ArrayList<Object[]>();
		floatArrayUniforms = new ArrayList<Object[]>();

		String imageUniforms = "";
		String colorMapUniforms = "";
		String footprintUniforms = "";
		String viewshedUniforms = "";
		String textureLayers = "";

		for (int i = 0; i < blendFactor.length; ++i) {
			blendFactor[i] = 0;
		}

		// generate shader code for each layer
		boolean addHasTexture = false;
		for (int i = 0; i < layers.length; ++i) {
			if (layers[i] == null) {
				continue;
			}
			if (layers[i].isImage()) {
				imageUniforms += "uniform sampler2D photo" + i + "Unit;\n";
				intUniforms.add(new Object[] { "photo" + i + "Unit", new Integer(i) });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				textureLayers += "		tcolor = texture2D(photo"+i+"Unit, gl_TexCoord[0].st);\n";
				textureLayers += "		f = blendFactor["+i+"]*tcolor.a;\n";
				textureLayers += "		color.rgb = color.rgb*(1-f)+tcolor.rgb*f;\n";
				addHasTexture = true;
			} else if (layers[i].hasColorMap()) {
				colorMapUniforms += "uniform sampler2D colorMap" + i + "Unit;\n";
				intUniforms.add(new Object[] { "colorMap" + i + "Unit", new Integer(i) });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				textureLayers += "		tcolor = texture2D(colorMap" + i + "Unit, gl_TexCoord["+ i + "].st);\n";
				textureLayers += "		f = blendFactor["+i+"]*tcolor.a;\n";
				textureLayers += "		color.rgb = color.rgb*(1-f)+tcolor.rgb*f;\n";
				addHasTexture = true;
			} else if (layers[i].getLayerType() == LayerType.footprint) {
				footprintUniforms += "uniform sampler2D footprint" + i + "Unit;\n";
				intUniforms.add(new Object[] { "footprint" + i + "Unit", new Integer(i) });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				textureLayers += getFootprintFunction(i);
			} else if (layers[i].getLayerType() == LayerType.viewshed) {
				FieldCameraLayer iLayer = (FieldCameraLayer) layers[i];
				viewshedUniforms += "uniform sampler2DShadow viewshed" + i + "Unit;\n";
				intUniforms.add(new Object[] { "viewshed" + i + "Unit", new Integer(i) });
				viewshedUniforms += "uniform float viewshed" + i + "Color[4];\n";
				floatArrayUniforms.add(new Object[] { "viewshed" + i + "Color", iLayer.getColor() });
				blendFactor[i] = (float) layers[i].getBlendFactor();
				textureLayers += getViewshedFunction(i);
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
		progStr += textureLayers;
		progStr += bottom;
		System.err.println("LayerEffects.setLayers");
		System.err.println(progStr);

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
		String str =
				  "		vec4 vscol" + i + " = vec4(viewshed" + i + "Color[0], viewshed" + i + "Color[1], viewshed"+i+"Color[2], viewshed"+i+"Color[3]);\n"
				+ "		if (gl_TexCoord[" + i + "].q > 0.0) {\n"
				+ "			float d = shadow2DProj(viewshed" + i + "Unit, gl_TexCoord[" + i + "]).x;\n"
				+ "			d = d < 1.0 ? 0.0 : 1.0;\n"
				+ "			f = blendFactor["+i+"]*vscol"+i+".a*d;\n"
				+ "			color.rgb = color.rgb*(1-f)+f*vscol"+i+".rgb;\n"
				+ "			hasTexture = true;\n" + "		}\n";
			return (str);
	}

	private String getFootprintFunction(int i) {
		String str = 
				  "		if (gl_TexCoord[" + i + "].q > 0.0) {\n"
				+ "			tcolor = texture2DProj(footprint"+i+"Unit, gl_TexCoord["+i+"]);\n"
				+ "			f = blendFactor["+i+"]*tcolor.a;\n"
				+ "			color.rgb = color.rgb*(1-f)+tcolor.rgb*f;\n"
				+ "			hasTexture = true;\n"
				+ "		}\n";
			return (str);
	}

}