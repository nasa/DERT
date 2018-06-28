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

package gov.nasa.arc.dert.scene;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.render.DirtyEventHandler;
import gov.nasa.arc.dert.render.SelectionHandler;
import gov.nasa.arc.dert.scene.featureset.FeatureSets;
import gov.nasa.arc.dert.scene.landmark.Landmarks;
import gov.nasa.arc.dert.scene.tapemeasure.TapeMeasure;
import gov.nasa.arc.dert.scene.tool.Tools;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.MarbleState;
import gov.nasa.arc.dert.util.SpatialPickResults;
import gov.nasa.arc.dert.util.SpatialUtil;

import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.ColorMaskState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.event.SceneGraphManager;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.NormalsMode;

public class World extends GroupNode {

	// Defaults
	public static double defaultStereoFocalDistance = 1;
	public static double defaultStereoEyeSeparation = 0.0333333;
	public static boolean defaultHiddenDashed = false;

	// The world is a singleton, holds viewpoint node
	private static World INSTANCE;

	// Root node of scene graph, holds light and contents nodes
	private GroupNode root;

	// Contents of scene graph, holds landscape and map elements
	private GroupNode contents;

	// Landmark map elements group
	private Landmarks landmarks;

	// Tool map elements group
	private Tools tools;

	// FeatureSet map elements group
	private FeatureSets featureSets;

	// Special figure
	private Marble marble;

	// The current time
	private long timeUTC;

	// Lighting for this world
	private Lighting lighting;

	// Handler for events from the scene framework
	private DirtyEventHandler dirtyEventHandler;

	// This world has been initialized
	private boolean initialized;

	// The tape measure
	private TapeMeasure ruler;
	
	// Show hidden lines as dashed lines
	private boolean hiddenDashed;

	// The texture state for shadows
	private TextureState textureState;

	// The color mask state for stereo
	private ColorMaskState colorMaskState;

	// The selection handler for picking
	private SelectionHandler selectionHandler;

	// Stereo parameters
	public double stereoFocalDistance = defaultStereoFocalDistance;
	public double stereoEyeSeparation = defaultStereoEyeSeparation;
	
	// Coordinate display
	private boolean useLonLat;

	/**
	 * Create a new world
	 * 
	 * @param name
	 * @param landmarks
	 * @param tools
	 * @param featureSets
	 * @param lighting
	 * @param timeUTC
	 * @return
	 */
	public static World createInstance(String name, Landmarks landmarks, Tools tools,
		FeatureSets featureSets, Lighting lighting, long timeUTC) {
		INSTANCE = new World(name, landmarks, tools, featureSets, lighting, timeUTC);
		return (INSTANCE);
	}

	/**
	 * Get the singleton instance of this world
	 * 
	 * @return
	 */
	public static World getInstance() {
		return (INSTANCE);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param landscape
	 * @param landmarks
	 * @param tools
	 * @param featureSets
	 * @param lighting
	 * @param background
	 * @param timeUTC
	 */
	protected World(String name, Landmarks landmarks, Tools tools, FeatureSets featureSets, Lighting lighting, long timeUTC) {
		super(name);
		this.landmarks = landmarks;
		this.tools = tools;
		this.featureSets = featureSets;
		this.lighting = lighting;
		this.timeUTC = timeUTC;

		// create the scenegraph
		root = new GroupNode("Root");
		contents = new GroupNode("Contents");
		textureState = new TextureState();
		textureState.setEnabled(true);
		contents.setRenderState(textureState);
		colorMaskState = new ColorMaskState();
		colorMaskState.setEnabled(true);
		lighting.setTarget(contents);
		root.attachChild(lighting.getLight());
		root.attachChild(contents);
		attachChild(root);

		// set up event and pick handling
		dirtyEventHandler = new DirtyEventHandler(contents);
		SceneGraphManager.getSceneGraphManager().addDirtyEventListener(dirtyEventHandler);
		selectionHandler = new SelectionHandler();
	}

	/**
	 * Initialize this world. This is carried out after an OpenGL context is
	 * created.
	 */
	public void initialize() {
		// been here before
		if (initialized) {
			return;
		}

		// initialize the scenegraph
		Landscape.getInstance().initialize();
		contents.attachChild(Landscape.getInstance());
		updateGeometricState(0); // create a world bounds

		// initialize the map elements
		landmarks.initialize();
		tools.initialize();
		featureSets.initialize();
		contents.attachChild(landmarks);
		contents.attachChild(tools);
		contents.attachChild(featureSets);

		// initialize lighting
		root.setRenderState(lighting.getGlobalLightState());
		lighting.getLight().setTranslation(Landscape.getInstance().getWorldBound().getCenter());

		// initialize the marble
		MarbleState marbleState = (MarbleState)ConfigurationManager.getInstance().getCurrentConfiguration().getState("MarbleState");
		marble = new Marble(marbleState);
		marble.setNormal(Vector3.UNIT_Z);
		marble.setTranslation(Landscape.getInstance().getCenter());
		marble.setSolarDirection(lighting.getLightDirection());
		contents.attachChild(marble);

		// initialize the ruler
		ruler = new TapeMeasure();
		ruler.getSceneHints().setCullHint(CullHint.Always);
		CoordAction.listenerList.add(ruler);
		contents.attachChild(ruler);

		// initialize shadows
		setTime(timeUTC);
		lighting.enableShadow(lighting.isShadowEnabled());

		SceneGraphManager.getSceneGraphManager().listenOnSpatial(root);
		updateGeometricState(0);

		// Initialize root node
		ZBufferState buf = new ZBufferState();
		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		buf.setEnabled(true);
		root.setRenderState(buf);

		BlendState as = new BlendState();
		as.setBlendEnabled(true);
		root.setRenderState(as);

		root.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);
		root.getSceneHints().setNormalsMode(NormalsMode.NormalizeIfScaled);

		updateGeometricState(0, true);
		initialized = true;
	}

	/**
	 * Select a point in the scene
	 * 
	 * @param pickRay
	 * @param position
	 * @param normal
	 * @param pickTop
	 * @param noQuadTree
	 * @return
	 */
	public Spatial select(Ray3 pickRay, Vector3 position, Vector3 normal, Node pickTop, boolean shiftDown) {
		SpatialPickResults boundsPick = SpatialUtil.pickBounds(contents, pickRay, pickTop);
		if (boundsPick != null) {
			return (selectionHandler.doSelection(pickRay, position, normal, boundsPick, shiftDown));
		}
		return (null);
	}

	/**
	 * Get the dirty event handler
	 * 
	 * @return
	 */
	public DirtyEventHandler getDirtyEventHandler() {
		return (dirtyEventHandler);
	}

	/**
	 * Get the texture state for the world
	 * 
	 * @return
	 */
	public TextureState getTextureState() {
		return (textureState);
	}

	/**
	 * Dispose of resources (landscape and lighting)
	 */
	public void dispose() {
		SceneGraphManager.getSceneGraphManager().removeDirtyEventListener(dirtyEventHandler);
		Landscape.getInstance().dispose();
		lighting.dispose();
		if (ruler != null)
			CoordAction.listenerList.remove(ruler);
	}

	/**
	 * Get the colormask state for stereo
	 * 
	 * @return
	 */
	public ColorMaskState getColorMaskState() {
		return (colorMaskState);
	}

	/**
	 * Get the group of landmarks
	 * 
	 * @return
	 */
	public Landmarks getLandmarks() {
		return (landmarks);
	}

	/**
	 * Get the group of tools
	 * 
	 * @return
	 */
	public Tools getTools() {
		return (tools);
	}

	/**
	 * Get the group of line sets
	 * 
	 * @return
	 */
	public FeatureSets getFeatureSets() {
		return (featureSets);
	}

	/**
	 * Get the contents of the scene graph
	 * 
	 * @return
	 */
	public GroupNode getContents() {
		return (contents);
	}

	/**
	 * Get the root node of the scene graph
	 * 
	 * @return
	 */
	public GroupNode getRoot() {
		return (root);
	}

	/**
	 * Set the vertical exaggeration of the terrain
	 * 
	 * @param vertExag
	 */
	public void setVerticalExaggeration(double vertExag) {
		if (vertExag <= 0) {
			return;
		}
		Landscape landscape = Landscape.getInstance();
		if (landscape == null) {
			return;
		}
		double oldVal = landscape.getVerticalExaggeration();
		landscape.setVerticalExaggeration(vertExag);
		double minZ = landscape.getMinimumElevation();
		contents.setVerticalExaggeration(vertExag, oldVal, minZ);
	}

	/**
	 * Set the vertical exaggeration of a new map element
	 * 
	 * @param vertExag
	 */
	public void setVerticalExaggeration(MapElement me) {
		Landscape landscape = Landscape.getInstance();
		if (landscape == null) {
			return;
		}
		double oldVal = landscape.getVerticalExaggeration();
		double minZ = landscape.getMinimumElevation();
		me.setVerticalExaggeration(oldVal, oldVal, minZ);
		((Spatial)me).updateGeometricState(0);
	}

	/**
	 * Get the vertical exaggeration of the terrain
	 * 
	 * @return
	 */
	public double getVerticalExaggeration() {
		Landscape landscape = Landscape.getInstance();
		if (landscape == null) {
			return (1);
		}
		return (landscape.getVerticalExaggeration());
	}

	/**
	 * Get the marble
	 * 
	 * @return
	 */
	public Marble getMarble() {
		return (marble);
	}

	/**
	 * Set the current time
	 * 
	 * @param timeUTC
	 */
	public void setTime(long timeUTC) {
		this.timeUTC = timeUTC;
		lighting.setTime(timeUTC);
	}

	/**
	 * Get the current time
	 * 
	 * @return
	 */
	public long getTime() {
		return (timeUTC);
	}

	/**
	 * Get the lighting object
	 * 
	 * @return
	 */
	public Lighting getLighting() {
		return (lighting);
	}

	/**
	 * Get the tape measure
	 * 
	 * @return
	 */
	public TapeMeasure getTapeMeasure() {
		return (ruler);
	}
	
	/**
	 * Get the hidden line representation flag
	 */
	public boolean isHiddenDashed() {
		return(hiddenDashed);
	}
	
	/**
	 * Set the hidden line representation flag
	 */
	public void setHiddenDashed(boolean hiddenDashed) {
		this.hiddenDashed = hiddenDashed;
		tools.setHiddenDashed(hiddenDashed);
		ruler.setHiddenDashed(hiddenDashed);
		root.markDirty(DirtyType.RenderState);
	}
	
	public void setMapElementsOnTop(boolean onTop) {
		landmarks.setOnTop(onTop);
		tools.setOnTop(onTop);
		featureSets.setOnTop(onTop);
		root.markDirty(DirtyType.RenderState);
	}
	
	public boolean isMapElementsOnTop() {
		return(landmarks.isOnTop());
	}
	
	public void setUseLonLat(boolean useLonLat) {
		this.useLonLat = useLonLat;
	}
	
	public boolean getUseLonLat() {
		return(useLonLat);
	}
	
	public static void markClean() {
		if (INSTANCE == null)
			return;
		INSTANCE.dirtyEventHandler.changed.set(false);
		INSTANCE.dirtyEventHandler.terrainChanged.set(false);
	}
}
