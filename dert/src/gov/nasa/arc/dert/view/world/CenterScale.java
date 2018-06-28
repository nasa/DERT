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

package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scenegraph.text.BitmapText;
import gov.nasa.arc.dert.scenegraph.text.Text;
import gov.nasa.arc.dert.scenegraph.text.Text.AlignType;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a simple scale at the center of the world view.
 *
 */
public class CenterScale extends Node {

	private final static float[] scaleVertex = { -50f,0,0, 50f,0,0, -50,-5,0, -50f,5,0, 50,-5,0, 50,5,0, 0,-5,0, 0,0,0 };
	private final static int[] scaleIndex = { 0,1, 2,3, 4,5, 6,7};
	private ReadOnlyColorRGBA[] scaleColor;
	
	private Line line;
	private BitmapText sizeText, distText;

	/**
	 * Constructor
	 * 
	 * @param color
	 */
	public CenterScale(ReadOnlyColorRGBA color) {
		super("Center Scale");
		line = new Line("_line");
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(scaleVertex);
		line.getMeshData().setVertexBuffer(vertexBuffer);
		scaleColor = new ReadOnlyColorRGBA[scaleVertex.length/3];
		for (int i=0; i<scaleColor.length; ++i)
			scaleColor[i] = color;
		line.getMeshData().setIndexMode(IndexMode.Lines);
		FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(scaleColor);
		colorBuffer.rewind();
		line.getMeshData().setColorBuffer(colorBuffer);
		line.getMeshData().setIndexBuffer(BufferUtils.createIntBuffer(scaleIndex));
		line.getMeshData().getIndexBuffer().limit(scaleIndex.length);
		line.getMeshData().getIndexBuffer().rewind();
		line.getSceneHints().setAllPickingHints(false);
		line.setModelBound(new BoundingBox());
		line.updateModelBound();
		MaterialState crosshairMaterialState = new MaterialState();
		crosshairMaterialState.setColorMaterial(MaterialState.ColorMaterial.Emissive);
		crosshairMaterialState.setEnabled(true);
		line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		line.setRenderState(crosshairMaterialState);
		line.updateGeometricState(0, true);
		
		attachChild(line);
		
		sizeText = new BitmapText("_ctr", BitmapText.DEFAULT_FONT, "", AlignType.Center, false);
		sizeText.setColor(ColorRGBA.WHITE);
		sizeText.setVisible(true);
		sizeText.setTranslation(0, 4, 0);
		attachChild(sizeText);
		
		distText = new BitmapText("_ctr", BitmapText.DEFAULT_FONT, "", AlignType.Center, false);
		distText.setColor(ColorRGBA.WHITE);
		distText.setVisible(true);
		distText.setTranslation(0, -Text.FONT_SIZE, 0);
		attachChild(distText);

		ZBufferState zBuf = new ZBufferState();
		zBuf.setFunction(ZBufferState.TestFunction.Always);
		zBuf.setEnabled(true);
		setRenderState(zBuf);
	}
	
	public void setText(double size, double dist) {
		sizeText.setText(String.format(Landscape.stringFormat, size));
		distText.setText(String.format(Landscape.stringFormat, dist));
	}
	
	public void showText(boolean show) {
		if (show) {
			sizeText.getSceneHints().setCullHint(CullHint.Inherit);
			distText.getSceneHints().setCullHint(CullHint.Inherit);
		}
		else {
			sizeText.getSceneHints().setCullHint(CullHint.Always);
			distText.getSceneHints().setCullHint(CullHint.Always);
		}
	}

}
