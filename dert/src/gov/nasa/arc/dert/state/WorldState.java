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

package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.landscape.io.FileSystemTileSource;
import gov.nasa.arc.dert.landscape.io.TileSource;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.featureset.FeatureSets;
import gov.nasa.arc.dert.scene.landmark.Landmarks;
import gov.nasa.arc.dert.scene.tool.Tools;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a state object for the World.
 *
 */
public class WorldState extends State {

	// Color
	public Color surfaceColor;

	// Vertical exaggeration value
	public double verticalExaggeration = 1;
	public boolean hiddenDashed;

	// The time
	public long time;

	// Viewpoint
	public ViewpointStore currentViewpoint;
	
	// Coordinate display
	public boolean useLonLat;

	// Persisted components
	protected Lighting lighting;
	protected LayerManager layerManager;

	// Transient components
	protected transient World world;
	protected transient TileSource tileSource;
	protected transient String username, password;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public WorldState() {
		super(null, StateType.World, new ViewData(20, 20, 960, 600, false));
		surfaceColor = Color.WHITE;
		time = System.currentTimeMillis();
		viewData.setVisible(true);
		lighting = new Lighting();
		layerManager = new LayerManager();
		tileSource = new FileSystemTileSource();
		username = "dert";
		password = "dert";
	}
	
	public WorldState(Map<String,Object> map) {
		super(map);
		useLonLat = StateUtil.getBoolean(map, "UseLonLat", false);
		surfaceColor = StateUtil.getColor(map, "SurfaceColor", Color.WHITE);
		verticalExaggeration = StateUtil.getDouble(map, "VerticalExaggeration", verticalExaggeration);
		time = StateUtil.getLong(map, "Time", System.currentTimeMillis());
		currentViewpoint = ViewpointStore.fromHashMap((HashMap<String,Object>)map.get("CurrentViewpoint"));		
		lighting = new Lighting((HashMap<String,Object>)map.get("Lighting"));
		layerManager = new LayerManager((HashMap<String,Object>)map.get("LayerManager"));
		hiddenDashed = StateUtil.getBoolean(map, "HiddenDashed", World.defaultHiddenDashed);
		tileSource = new FileSystemTileSource();
		username = "dert";
		password = "dert";
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof WorldState)) 
			return(false);
		WorldState that = (WorldState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (this.useLonLat != that.useLonLat)
			return(false);
		if (this.time != that.time)
			return(false);
		if (this.verticalExaggeration != that.verticalExaggeration)
			return(false);
		if (this.hiddenDashed != that.hiddenDashed)
			return(false);
		if (!surfaceColor.equals(that.surfaceColor)) 
			return(false);
		// the same viewdata objects or both are null
		if (this.currentViewpoint == that.currentViewpoint)
			return(true);
		// this view data is null but the other isn't
		if (this.currentViewpoint == null)
			return(false);
		// the other view data is null but this one isn't
		if (that.currentViewpoint == null)
			return(false);
		// see if the view datas are equal
		return(this.currentViewpoint.isEqualTo(that.currentViewpoint));
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str += " time="+time+" surfaceColor="+surfaceColor+" vertexag="+verticalExaggeration+" VP="+currentViewpoint;
		return(str);
	}

	/**
	 * Create the world.
	 * 
	 * @param landscape
	 * @return the world
	 */
	public World createWorld(String landscapeName, Configuration config) {
		if (!tileSource.connect(landscapeName, username, password)) {
			return (null);
		}
		String[][] layerInfo = tileSource.getLayerInfo();

		if (layerInfo.length == 0) {
			Console.println("No valid layers.");
			return (null);
		}
		lighting.initialize();
		if (!layerManager.initialize(tileSource)) {
			return (null);
		}
		// create Landscape before world
		Landscape.createInstance(tileSource, layerManager, surfaceColor);
		world = World.createInstance(name, new Landmarks(config.getLandmarkStates()),
			new Tools(config.getToolStates()), new FeatureSets(config.getFeatureSetStates()), lighting, time);
		world.setUseLonLat(useLonLat);
		if (verticalExaggeration != 1) {
			world.setVerticalExaggeration(verticalExaggeration);
		}
		return (world);
	}

	@Override
	public void dispose() {
		if (world != null)
			world.dispose();
		world = null;
		tileSource = null;
		System.gc();
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		
		map.put("Lighting", lighting.saveAsHashMap());
		map.put("LayerManager", layerManager.saveAsHashMap());
		if (viewData != null) {
			WorldView wv = (WorldView)viewData.getView();
			if (wv != null)
				currentViewpoint = wv.getViewpoint().get(currentViewpoint);
		}
		map.put("CurrentViewpoint", currentViewpoint.toHashMap());
		verticalExaggeration = world.getVerticalExaggeration();
		map.put("VerticalExaggeration", new Double(verticalExaggeration));
		surfaceColor = Landscape.getInstance().getSurfaceColor();
		map.put("SurfaceColor", surfaceColor);
		time = world.getTime();
		map.put("Time", new Long(time));
		useLonLat = world.getUseLonLat();
		map.put("UseLonLat", new Boolean(useLonLat));
		map.put("HiddenDashed", new Boolean(world.isHiddenDashed()));
		
		return(map);
	}
	
	public void initWorld() {
		// nothing here
	}

}
