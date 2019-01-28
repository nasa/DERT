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

package gov.nasa.arc.dert.scene.landmark;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.ModelState;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.resource.SimpleResourceLocator;

/**
 * Provides a 3D figure map element. This element has depth and causes shadows.
 *
 */
public class Model extends FigureMarker implements Landmark {

	public static final Icon icon = Icons.getImageIcon("model.png");

	// Defaults
	public static Color defaultColor = Color.lightGray;
	public static double defaultSize = 1.0f;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultSurfaceNormalVisible = false;
	public static double defaultAzimuth = 0;
	public static double defaultTilt = 0;

	// The map element state
	private ModelState state;
	
	private String filePath;
	private Node rootNode;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Model(ModelState state)
		throws URISyntaxException, IOException {
		super(state.name, state.position, state.size, state.zOff, state.color, state.labelVisible, false, state.locked);
		filePath = state.filePath;
		if (filePath != null) {
			File file = new File(filePath);
			ColladaImporter importer = new ColladaImporter();
			importer.setLoadAnimations(false);
			SimpleResourceLocator resourceLocator = new SimpleResourceLocator(new File(file.getParent()).toURI());
			importer.setModelLocator(resourceLocator);
			importer.setTextureLocator(resourceLocator);
			ColladaStorage storage = importer.load(file.getName());
			rootNode = storage.getScene();
			contents.attachChild(rootNode);
			MaterialState materialState = new MaterialState();
			materialState.setColorMaterial(ColorMaterial.None);
			contents.setRenderState(materialState);
			contents.updateGeometricState(0);
			rootNode.updateWorldBound(true);
			label.setTranslation(0, rootNode.getWorldBound().getRadius()*size, 0);
		}
		setAzimuth(state.azimuth);
		setTilt(state.tilt);
		setSurfaceNormalVisible(state.showNormal);
		setVisible(state.visible);
		this.state = state;
		state.setMapElement(this);
		updateWorldTransform(true);
		updateWorldBound(true);
		getSceneHints().setRenderBucketType(RenderBucketType.Inherit);
	}

	/**
	 * Get the map element state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}
	
	public String getFilePath() {
		return(filePath);
	}

	/**
	 * Get the point and distance to seek to
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		point.set(getTranslation());
		return (getSize()*10*Landscape.getInstance().getPixelWidth());
	}

	/**
	 * Show the surface normal
	 * 
	 * @param show
	 */
	public void setSurfaceNormalVisible(boolean show) {
		surfaceNormalArrow.getSceneHints().setCullHint(show ? CullHint.Inherit : CullHint.Always);
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Find out if the surface normal is visible
	 * 
	 * @return
	 */
	public boolean isSurfaceNormalVisible() {
		return (SpatialUtil.isDisplayed(surfaceNormalArrow));
	}

	/**
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.Model);
	}

	/**
	 * Get the map element icon
	 */
	@Override
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Set the size
	 * 
	 * @param size
	 */
	@Override
	public void setSize(double size) {
		if (this.size == size) {
			return;
		}
		this.size = size;
		rootNode.setScale(size, size, size);
		rootNode.updateWorldBound(true);
		label.setTranslation(0, rootNode.getWorldBound().getRadius()*size, 0);
		updateWorldTransform(true);
		updateWorldBound(true);
	}

	/**
	 * Set the azimuth
	 * 
	 * @param azimuth
	 */
	@Override
	public void setAzimuth(double azimuth) {
		if (this.azimuth == azimuth) {
			return;
		}
		this.azimuth = azimuth;
		if (rootNode != null) {
			rotMat = new Matrix3();
			rootNode.setRotation(rotMat.fromAngles(Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
		}
	}

	/**
	 * Set the tilt
	 * 
	 * @param tilt
	 */
	@Override
	public void setTilt(double tilt) {
		if (this.tilt == tilt) {
			return;
		}
		this.tilt = tilt;
		if (rootNode != null) {
			rotMat = new Matrix3();
			rootNode.setRotation(rotMat.fromAngles(Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
		}
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.Model.defaultColor", defaultColor, false);
		defaultSize = (float) StringUtil.getDoubleValue(properties, "MapElement.Model.defaultSize", true, defaultSize,
			false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.Model.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultSurfaceNormalVisible = StringUtil.getBooleanValue(properties,
			"MapElement.Model.defaultSurfaceNormalVisible", defaultSurfaceNormalVisible, false);
		defaultAzimuth = StringUtil.getDoubleValue(properties, "MapElement.Model.defaultAzimuth", false,
			defaultAzimuth, false);
		defaultTilt = StringUtil.getDoubleValue(properties, "MapElement.Model.defaultTilt", false, defaultTilt, false);
	}

	/**
	 * Save the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.Model.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.Model.defaultSize", Double.toString(defaultSize));
		properties.setProperty("MapElement.Model.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.Model.defaultSurfaceNormalVisible", Boolean.toString(defaultSurfaceNormalVisible));
		properties.setProperty("MapElement.Model.defaultAzimuth", Double.toString(defaultAzimuth));
		properties.setProperty("MapElement.Model.defaultTilt", Double.toString(defaultTilt));
	}
}
