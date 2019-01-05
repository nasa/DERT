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

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.scenegraph.text.BitmapText;
import gov.nasa.arc.dert.scenegraph.text.Text.AlignType;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.UIUtil;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.BillboardNode;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Base class for marker objects such as Landmarks and Tool actuators. Carries a
 * label.
 */
public abstract class Marker extends Movable implements ViewDependent {
	
	public static double PIXEL_SIZE;

	// marker size
	protected double size = 1;
	// scale factor for viewpoint resizing
	protected double scale = 1, oldScale = 1;

	// marker color
	protected Color color;
	// Ardor3D version of color
	protected ColorRGBA colorRGBA;
	// label color
	protected ReadOnlyColorRGBA labelColorRGBA = ColorRGBA.WHITE;
	// material state
	protected MaterialState materialState;

	// marker label
	protected BitmapText label;
	// label contents
	protected String labelStr = "";
	protected BillboardNode billboard;

	protected Vector3 worldLoc;

	// contains label and spatials specific to marker type
	protected Node contents;
	
	protected boolean labelVisible;

	/**
	 * Constructor
	 */
	public Marker(String name, ReadOnlyVector3 point, double size, double zOff, Color color, boolean labelVisible, boolean locked) {
		super(name);
		billboard = new BillboardNode("_billboard");
		this.labelStr = name;
		this.labelVisible = labelVisible;
		this.size = size;
		this.zOff = zOff;
		setLocked(locked);
		worldLoc = new Vector3();
		if (point != null)
			setLocation(point, false);

		// default states
		// for transparency
		BlendState bs = new BlendState();
		bs.setBlendEnabled(true);
		setRenderState(bs);
		// turn off textures by default to block inherited textures
		TextureState textureState = new TextureState();
		textureState.setEnabled(false);
		setRenderState(textureState);
		// set the color and material state
		setColor(color);

		contents = new Node("_marker_contents");
		contents.setScale(size);
		attachChild(contents);

		// create the label object
		createLabel();
		billboard.attachChild(label);
		contents.attachChild(billboard);

		getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
	}

	/**
	 * Set the name and label
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		if (label != null) {
			label.setText(name);
			label.markDirty(DirtyType.RenderState);
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the size
	 * 
	 * @return
	 */
	public double getSize() {
		return (size);
	}

	/**
	 * Set the size
	 * 
	 * @param size
	 */
	public void setSize(double size) {
		if (this.size == size) {
			return;
		}
		this.size = size;
		scaleShape(scale);
	}

	/**
	 * Determine visibility
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return (SpatialUtil.isDisplayed(this));
	}

	/**
	 * Set visibility
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the color
	 * 
	 * @return
	 */
	public Color getColor() {
		return (color);
	}

	/**
	 * Set the color
	 * 
	 * @param newColor
	 */
	public void setColor(Color newColor) {
		color = newColor;
		colorRGBA = UIUtil.colorToColorRGBA(color);
		if (materialState == null) {
			// add a material state
			materialState = new MaterialState();
			materialState.setColorMaterial(ColorMaterial.None);
			materialState.setEnabled(true);
			setRenderState(materialState);
		}
		setMaterialState();
		markDirty(DirtyType.RenderState);
	}

	protected abstract void setMaterialState();

	protected void createLabel() {
		label = new BitmapText("_label", BitmapText.DEFAULT_FONT, labelStr, AlignType.Center, true);
		label.setScaleFactor((float) (0.75 * size));
		label.setColor(labelColorRGBA);
		label.setTranslation(0, 2, 0);
		label.setVisible(labelVisible);
	}

	/**
	 * Set label visibility
	 * 
	 * @param labelVisible
	 */
	public void setLabelVisible(boolean labelVisible) {
		this.labelVisible = labelVisible;
		label.setVisible(labelVisible);
	}

	/**
	 * Set label text
	 * 
	 * @param str
	 */
	public void setLabel(String str) {
		if (str == null) {
			str = "";
		}
		if (labelStr.equals(str)) {
			return;
		}
		labelStr = str;
		label.setText(labelStr);
		updateWorldBound(true);
	}

	/**
	 * Get label text
	 * 
	 * @return
	 */
	public String getLabel() {
		return (labelStr);
	}

	/**
	 * Determine label visibility
	 * 
	 * @return
	 */
	public boolean isLabelVisible() {
		return (labelVisible);
	}

	/**
	 * Update size depending on camera location.
	 */
	@Override
	public void update(BasicCamera camera) {
		scale = camera.getPixelSizeAt(getWorldTranslation(), true) * PIXEL_SIZE;
		if (Math.abs(scale - oldScale) > 0.0000001) {
			oldScale = scale;
			scaleShape(scale);
		}
	}

	protected void scaleShape(double scale) {
		contents.setScale(size * scale);
	}
	
	public double getShapeScale() {
		return(scale);
	}

	/**
	 * Set the vertical exaggeration
	 * 
	 * @param vertExag
	 * @param oldVertExag
	 * @param minZ
	 */
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		ReadOnlyVector3 wTrans = getWorldTranslation();
		Vector3 tmp = new Vector3(wTrans.getX(), wTrans.getY(), wTrans.getZ() * vertExag / oldVertExag);
		getParent().worldToLocal(tmp, tmp);
		setTranslation(tmp);
	}

	/**
	 * Update the Z coordinate when elevation changes
	 * 
	 * @param quadTree
	 * @return
	 */
	public boolean updateElevation(QuadTree quadTree) {
		ReadOnlyVector3 t = getLocation();
		if (quadTree.contains(t.getX(), t.getY())) {
			double z = Landscape.getInstance().getZ(t.getX(), t.getY(), quadTree);
			if (!Double.isNaN(z)) {
				setLocation(t.getX(), t.getY(), z, false);
				return (true);
			}
		}
		return (false);
	}

	@Override
	public String toString() {
		return (getName());
	}

}
