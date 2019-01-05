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
 
Tile Rendering Library - Brian Paul 
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

package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.util.UIUtil;

import java.awt.Color;
import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.util.geom.BufferUtils;

public class HiddenLine
	extends Node {
	
	private Line line;
	private Line dashedLine;
	private float lineWidth = 2;
	
	public HiddenLine(String name, IndexMode indexMode) {
		super(name);
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(3);
		vertexBuffer.limit(0);
		line = new Line("_line");
		line.getMeshData().setIndexMode(indexMode);
		line.getMeshData().setVertexBuffer(vertexBuffer);
		line.getMeshData().updateVertexCount();
		line.getSceneHints().setCastsShadows(false);
		line.getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
		line.setModelBound(new BoundingBox());
		line.updateModelBound();
		line.setLineWidth((float)lineWidth);
		attachChild(line);
		
		vertexBuffer = BufferUtils.createFloatBuffer(3);
		vertexBuffer.limit(0);
		dashedLine = new Line("_dashedline");
		dashedLine.getMeshData().setIndexMode(indexMode);
		dashedLine.getMeshData().setVertexBuffer(vertexBuffer);
		dashedLine.getMeshData().updateVertexCount();
		dashedLine.getSceneHints().setCastsShadows(false);
		dashedLine.getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
		dashedLine.setModelBound(new BoundingBox());
		dashedLine.updateModelBound();
		boolean hiddenDashed = World.getInstance().isHiddenDashed();
		dashedLine.getSceneHints().setCullHint(hiddenDashed ? CullHint.Inherit : CullHint.Always);
		dashedLine.setLineWidth((float)lineWidth*0.5f);
		ZBufferState zbs = new ZBufferState();
		zbs.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		zbs.setEnabled(false);
		dashedLine.setRenderState(zbs);
		dashedLine.setStipplePattern((short) 0xf0f0);
		attachChild(dashedLine);
	}

	/**
	 * Constructor
	 * 
	 * @param p0
	 *            first end point
	 * @param p1
	 *            second end point
	 */
	public HiddenLine(String name, ReadOnlyVector3 p0, ReadOnlyVector3 p1) {
		this(name, IndexMode.Lines);
		float[] vertex = new float[6];
		vertex[0] = (float) p0.getX();
		vertex[1] = (float) p0.getY();
		vertex[2] = (float) p0.getZ();
		vertex[3] = (float) p1.getX();
		vertex[4] = (float) p1.getY();
		vertex[5] = (float) p1.getZ();
		setVertexBuffer(BufferUtils.createFloatBuffer(vertex));
	}

	/**
	 * Set the endpoints of the line segment
	 * 
	 * @param p0
	 * @param p1
	 */
	public void setPoints(ReadOnlyVector3 p0, ReadOnlyVector3 p1) {
		float[] vertex = new float[6];
		vertex[0] = (float) p0.getX();
		vertex[1] = (float) p0.getY();
		vertex[2] = (float) p0.getZ();
		vertex[3] = (float) p1.getX();
		vertex[4] = (float) p1.getY();
		vertex[5] = (float) p1.getZ();
		setVertexBuffer(BufferUtils.createFloatBuffer(vertex));
	}
	
	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
		line.setLineWidth((float)lineWidth);
		dashedLine.setLineWidth((float)lineWidth*0.5f);
	}
	
	public float getLineWidth() {
		return(lineWidth);
	}
	
	public void enableDash(boolean enable) {
		dashedLine.getSceneHints().setCullHint(enable ? CullHint.Inherit : CullHint.Always);
	}
	
	public boolean isHiddenDashed() {
		return(dashedLine.getSceneHints().getCullHint() == CullHint.Inherit);
	}
	
	public FloatBuffer getVertexBuffer() {
		return(line.getMeshData().getVertexBuffer());
	}
	
	public void setVertexBuffer(FloatBuffer buffer) {
		line.getMeshData().setVertexBuffer(buffer);
		line.getMeshData().updateVertexCount();
		dashedLine.getMeshData().setVertexBuffer(buffer);
		dashedLine.getMeshData().updateVertexCount();
		markDirty(DirtyType.Bounding);
	}
	
	public void updateModelBound() {
		line.updateModelBound();
		dashedLine.updateModelBound();
	}
	
	public void setModelBound(BoundingVolume bounds) {
		line.setModelBound(bounds);
		dashedLine.setModelBound(bounds);
	}

	/**
	 * Set the color.
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		ColorRGBA colorRGBA = UIUtil.colorToColorRGBA(color);

		MaterialState lineMS = new MaterialState();
		lineMS.setDiffuse(ColorRGBA.BLACK);
		lineMS.setAmbient(ColorRGBA.BLACK);
		lineMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		line.setRenderState(lineMS);
		dashedLine.setRenderState(lineMS);
	}

	public void highlight(boolean enable, Color color) {
		ColorRGBA colorRGBA = UIUtil.colorToColorRGBA(color);
		
		MaterialState materialState = (MaterialState) line.getLocalRenderState(RenderState.StateType.Material);
		if (enable) {
			materialState.setAmbient(MaterialFace.FrontAndBack, colorRGBA);
			materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
		}
		else {
			materialState.setDiffuse(ColorRGBA.BLACK);
			materialState.setAmbient(ColorRGBA.BLACK);
		}
		line.setRenderState(materialState);
		dashedLine.setRenderState(materialState);
	}

}
