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
import gov.nasa.arc.dert.scenegraph.BillboardMarker;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.PlacemarkState;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;

/**
 * Provides a place marker map element
 *
 */
public class Placemark extends BillboardMarker implements Landmark {

	public static final Icon icon = Icons.getImageIcon("placemark_16.png");
	
	public static enum IconId { pushpin, flag, mappin }	
	public static final String[] ICON_LABEL = { "Pushpin", "Flag", "Map Pin" };
	public static final Icon[] icons;	
	static {
		icons = new Icon[Placemark.IconId.values().length];
		for (int i = 0; i < icons.length; ++i) {
			icons[i] = Icons.getImageIcon(Placemark.IconId.values()[i] + "_24.png");
		}
	}

	// Defaults
	public static Color defaultColor = Color.yellow;
	public static double defaultSize = 1.0f;
	public static int defaultTextureIndex = 0;
	public static boolean defaultLabelVisible = true;

	// Icon selections
//	public static final String[] ICON_NAME = { "pushpin", "flag", "mappin" };

	// Texture selections
	protected static final Texture[] nominalTexture = new Texture[IconId.values().length];
	protected static final Texture[] highlightTexture = new Texture[IconId.values().length];

	protected int textureIndex = -1;
	protected PlacemarkState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Placemark(PlacemarkState state) {
		super(state.name, state.position, state.size, state.zOff, state.color, state.labelVisible, state.locked);
		setTexture(state.textureIndex);
		setVisible(state.visible);
		this.state = state;
		state.setMapElement(this);
	}

	/**
	 * Get the map element state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Set the texture index for the icon
	 * 
	 * @param textureIndex
	 */
	public void setTexture(int textureIndex) {
		if (this.textureIndex == textureIndex) {
			return;
		}
		this.textureIndex = textureIndex;
		if (nominalTexture[textureIndex] == null) {
			nominalTexture[textureIndex] = ImageUtil.createTexture(Icons.getIconURL(IconId.values()[textureIndex] + ".png"),
				true);
			highlightTexture[textureIndex] = ImageUtil.createTexture(
				Icons.getIconURL(IconId.values()[textureIndex] + "-highlight.png"), true);
		}
		setTexture(nominalTexture[textureIndex], highlightTexture[textureIndex]);
	}

	/**
	 * Get the current texture index
	 * 
	 * @return
	 */
	public int getTextureIndex() {
		return (textureIndex);
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
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.Placemark);
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.Placemark.defaultColor", defaultColor, false);
		defaultSize = (float) StringUtil.getDoubleValue(properties, "MapElement.Placemark.defaultSize", true,
			defaultSize, false);
		defaultTextureIndex = StringUtil.getIntegerValue(properties, "MapElement.Placemark.defaultTextureIndex", true,
			defaultTextureIndex, false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.Placemark.defaultLabelVisible",
			defaultLabelVisible, false);
	}

	/**
	 * Get the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.Placemark.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.Placemark.defaultSize", Double.toString(defaultSize));
		properties.setProperty("MapElement.Placemark.defaultTextureIndex", Integer.toString(defaultTextureIndex));
		properties.setProperty("MapElement.Placemark.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
	}
}
