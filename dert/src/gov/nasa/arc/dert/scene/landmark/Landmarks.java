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

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.io.CsvWriter;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.scene.Marble;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.FigureState;
import gov.nasa.arc.dert.state.ImageBoardState;
import gov.nasa.arc.dert.state.LandmarkState;
import gov.nasa.arc.dert.state.ModelState;
import gov.nasa.arc.dert.state.PlacemarkState;
import gov.nasa.arc.dert.view.Console;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Group of Landmarks
 *
 */
public class Landmarks extends GroupNode {

	// Landmark state list
	private ArrayList<LandmarkState> landmarkList;
	
	private ZBufferState zBufferState;

	/**
	 * Constructor
	 * 
	 * @param landmarkList
	 */
	public Landmarks(ArrayList<LandmarkState> landmarkList) {
		super("Landmarks");
		this.landmarkList = landmarkList;
	}

	/**
	 * Initialize this object Create landmark objects
	 */
	public void initialize() {
		// Turn off any parent textures
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);

		// create landmark objects
		for (int i = 0; i < landmarkList.size(); ++i) {
			LandmarkState state = landmarkList.get(i);
			addLandmark(state, false);
		}

		zBufferState = new ZBufferState();
		zBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		zBufferState.setEnabled(true);
		setRenderState(zBufferState);
	}

	/**
	 * Landscape changed, update the Z coordinate of the landmarks.
	 * 
	 * @param quadTree
	 */
	public void landscapeChanged(final QuadTree quadTree) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			final Spatial child = getChild(i);
			if (child instanceof Landmark) {
				((Landmark) child).updateElevation(quadTree);
			}
		}
	}

	/**
	 * Create and add a landmark object.
	 * 
	 * @param state
	 * @param update
	 * @return
	 */
	public Landmark addLandmark(LandmarkState state, boolean update) {
		Landmark landmark = null;
		switch (state.mapElementType) {
		case Figure:
			FigureState fState = (FigureState) state;
			landmark = new Figure(fState);
			break;
		case Placemark:
			PlacemarkState pState = (PlacemarkState) state;
			landmark = new Placemark(pState);
			break;
		case Billboard:
			ImageBoardState iState = (ImageBoardState) state;
			landmark = new ImageBoard(iState);
			break;
		case Model:
			ModelState mState = (ModelState) state;
			try {
				landmark = new Model(mState);
			}
			catch (Exception e) {
				e.printStackTrace();
				Console.println("Error loading model.  See log.");
				landmark = null;
			}
			break;
		case Path:
		case Plane:
		case Profile:
		case FieldCamera:
		case FeatureSet:
		case Feature:
		case CartesianGrid:
		case RadialGrid:
		case Waypoint:
		case Marble:
		case Scale:
			break;
		}
		if (landmark != null) {
			Spatial spatial = (Spatial) landmark;
			attachChild(spatial);
			if (update) {
				// update geometric state so we know where landmark is attached
				spatial.updateGeometricState(0, true);
				// update landmark scale according to location
				landmark.update(Dert.getWorldView().getViewpoint().getCamera());
			}
			spatial.markDirty(DirtyType.RenderState);
		}
		return (landmark);
	}

	/**
	 * Show all the landmarks
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		Marble marble = World.getInstance().getMarble();
		marble.setVisible(visible);
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			((Landmark) getChild(i)).setVisible(visible);
		}
	}

	/**
	 * Pin all the landmarks
	 * 
	 * @param pin
	 */
	public void setAllLocked(boolean pin) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			((Landmark) getChild(i)).setLocked(pin);
		}
	}

	public void saveAsCsv(String filename) {
		CsvWriter csvWriter = null;
		DecimalFormat formatter = new DecimalFormat(Landscape.format);
		try {
			int n = getNumberOfChildren();
			String[] column = { "Index", "Name", "X", "Y", "Z", "Annotation" };
			csvWriter = new CsvWriter(filename, column);
			csvWriter.open();
			String[] value = new String[column.length];
			for (int i = 0; i < n; ++i) {
				Landmark ldmk = (Landmark) getChild(i);
				ReadOnlyVector3 loc = ((Spatial) ldmk).getTranslation();
				value[0] = Integer.toString(i);
				value[1] = ldmk.getName();
				value[2] = formatter.format(loc.getX());
				value[3] = formatter.format(loc.getY());
				value[4] = formatter.format(loc.getZ());
				value[5] = ldmk.getState().getAnnotation();
				csvWriter.writeLine(value);
			}
			csvWriter.close();
			Console.println(n + " records saved to " + filename);
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
	
	public void setOnTop(boolean onTop) {
		zBufferState.setEnabled(!onTop);
	}
	
	public boolean isOnTop() {
		return(!zBufferState.isEnabled());
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		Placemark.saveDefaultsToProperties(properties);
		Figure.saveDefaultsToProperties(properties);
		ImageBoard.saveDefaultsToProperties(properties);
	}

}
