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

package gov.nasa.arc.dert.scenegraph.text;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;

/**
 * A base class for drawing text strings.
 *
 */
public abstract class Text extends Mesh {
	
	public static double FONT_SIZE = 20;

	// Text alignment options
	public static enum AlignType {
		Left, Right, Center
	}

	// The text string
	protected String textString;

	// A scale for size (not used in RasterText)
	protected double scaleFactor;

	// The color
	protected ColorRGBA color = new ColorRGBA(1, 1, 1, 1);

	// The alignment
	protected AlignType alignment;

	// The location where the text begins
	protected Vector3 position;

	// For visibility
	protected CullHint cullHint = CullHint.Inherit;
	
	// Cache string dimensions
	protected double width, height;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param textString
	 * @param alignment
	 */
	public Text(String name, String textString, AlignType alignment) {
		super(name);
		if (textString == null) {
			textString = "";
		}
		this.textString = textString;
		this.alignment = alignment;
		scaleFactor = 1;
	}
	
	/**
	 * Call this method in constructor of extending classes.
	 */
	protected void initialize() {
		width = getTextWidth();
		height = getTextHeight();
		double w = getWidth() / 2.0;
		double h = getHeight() / 2.0;
		switch (alignment) {
		case Left:
			position = new Vector3();
			_modelBound = new BoundingBox(new Vector3(w, h, 0), w, h, 1);
			break;
		case Center:
			position = new Vector3(-w, 0, 0);
			_modelBound = new BoundingBox(Vector3.ZERO, w, h, 1);
			break;
		case Right:
			position = new Vector3(-w * 2, 0, 0);
			_modelBound = new BoundingBox(new Vector3(-w, h, 0), w, h, 1);
			break;
		}
		updateWorldTransform(true);
		updateWorldBound(true);
		getSceneHints().setCastsShadows(false);
		getSceneHints().setCullHint(cullHint);
		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		getSceneHints().setPickingHint(PickingHint.Pickable, false);

		GLSLShaderObjectsState glsl = new GLSLShaderObjectsState();
		glsl.setEnabled(false);
		setRenderState(glsl);
		BlendState bs = new BlendState();
		bs.setBlendEnabled(false);
		setRenderState(bs);
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);

		setDefaultColor(color);
	}	

	protected abstract double getTextWidth();

	protected abstract double getTextHeight();

	/**
	 * Set the color of the text
	 * 
	 * @param col
	 */
	public void setColor(ReadOnlyColorRGBA col) {
		color.set(col);
		setDefaultColor(color);
	}

	/**
	 * Set the scale factor of the text
	 * 
	 * @param scaleFactor
	 */
	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
		double w = getWidth() / 2.0;
		switch (alignment) {
		case Left:
			position.set(Vector3.ZERO);
			break;
		case Center:
			position.set(-w, 0, 0);
			break;
		case Right:
			position.set(-w * 2, 0, 0);
			break;
		}
	}

	/**
	 * Set the text string
	 * 
	 * @param textString
	 */
	public void setText(String textString) {
		if (textString == null) {
			textString = "";
		}
		this.textString = textString;
		width = getTextWidth();
		height = getTextHeight();
		double w = getWidth() / 2.0;
		double h = getHeight() / 2.0;
		switch (alignment) {
		case Left:
			position.set(Vector3.ZERO);
			((BoundingBox)_modelBound).setXExtent(w);
			((BoundingBox)_modelBound).setYExtent(h);
			((BoundingBox)_modelBound).setCenter(w, h, 0);
			break;
		case Center:
			position.set(-w, 0, 0);
			((BoundingBox)_modelBound).setXExtent(w);
			((BoundingBox)_modelBound).setYExtent(h);
			((BoundingBox)_modelBound).setCenter(Vector3.ZERO);
			break;
		case Right:
			position.set(-w * 2, 0, 0);
			((BoundingBox)_modelBound).setXExtent(w);
			((BoundingBox)_modelBound).setYExtent(h);
			((BoundingBox)_modelBound).setCenter(-w, h, 0);
			break;
		}
		markDirty(DirtyType.Bounding);
	}

	/**
	 * Get the text scale factor
	 * 
	 * @return
	 */
	public double getScaleFactor() {
		return (scaleFactor);
	}

	/**
	 * Get the text string
	 * 
	 * @return
	 */
	public String getText() {
		return (textString);
	}

	/**
	 * Get the text string width
	 * 
	 * @return
	 */
	public double getWidth() {
		return (scaleFactor*getTextWidth());
	}

	/**
	 * Get the text string height
	 * 
	 * @return
	 */
	public double getHeight() {
		return (scaleFactor*getTextHeight());
	}

	/**
	 * Set visibility
	 */
	@Override
	public void setVisible(boolean visible) {
		cullHint = visible ? CullHint.Inherit : CullHint.Always;
		getSceneHints().setCullHint(cullHint);
	}

	/**
	 * Get visibility
	 */
	@Override
	public boolean isVisible() {
		return (cullHint == CullHint.Inherit);
	}

}
