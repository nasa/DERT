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

package gov.nasa.arc.dert.action.edit;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

/**
 * Dialog to edit the background color of the worldview. The dert.properties
 * file provides a list of predefined colors. A color chooser is also available.
 *
 */
public class BackgroundColorDialog extends AbstractDialog {

	/**
	 * Data structure to hold color information.
	 *
	 */
	public static class ColorEntry {
		public String name;
		public ReadOnlyColorRGBA color;

		public ColorEntry(String name, ReadOnlyColorRGBA color) {
			this.name = name;
			this.color = color;
		}

		@Override
		public String toString() {
			return (name);
		}
	}

	// Predefined colors from dert.properties
	protected static ColorEntry[] predefined;
	protected ColorSelectionPanel csp;
	protected ReadOnlyColorRGBA bgCol;

	/**
	 * Constructor
	 */
	public BackgroundColorDialog() {
		super(Dert.getMainWindow(), "Background Color", true, false);
	}

	@Override
	protected void build() {
		super.build();		
		contentArea.setLayout(new FlowLayout());
		bgCol = World.getInstance().getLighting().getBackgroundColor();
		csp = new ColorSelectionPanel(UIUtil.colorRGBAToColor(bgCol)) {
			public void doColor(Color color) {
				bgCol = UIUtil.colorToColorRGBA(color);
			}
		};
		contentArea.add(csp);
		contentArea.add(new JLabel("  Predefined", SwingConstants.RIGHT));
		JComboBox combo = new JComboBox(predefined);
		contentArea.add(combo);
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JComboBox combo = (JComboBox) event.getSource();
				ColorEntry ce = (ColorEntry) combo.getSelectedItem();
				if (ce.color == null) {
					return;
				}
				csp.setColor(UIUtil.colorRGBAToColor(ce.color));
				bgCol = ce.color;
			}
		});
	}

	/**
	 * Set the background color.
	 */
	@Override
	public boolean okPressed() {
		World.getInstance().getLighting().setBackgroundColor(bgCol);
		return (true);
	}

	/**
	 * Set predefined colors from dert.properties file.
	 * 
	 * @param dertProperties
	 *            data from dert.properties file
	 */
	public static void setPredefinedBackgroundColors(Properties dertProperties) {
		Object[] key = dertProperties.keySet().toArray();
		ArrayList<ColorEntry> list = new ArrayList<ColorEntry>();
		for (int i = 0; i < key.length; ++i) {
			if (((String) key[i]).startsWith("Background.")) {
				String name = (String) key[i];
				Color color = StringUtil.stringToColor((String) dertProperties.get(name));
				if (color != null) {
					ReadOnlyColorRGBA colorRGBA = UIUtil.colorToColorRGBA(color);
					int p = name.indexOf('.');
					if (p < 0) {
						throw new IllegalArgumentException("Background property " + name
							+ " from dert.properties is invalid.");
					}
					name = name.substring(p + 1);
					if (name.toLowerCase().equals("default")) {
						Lighting.defaultBackgroundColor = colorRGBA;
					} else {
						ColorEntry ce = new ColorEntry(name, colorRGBA);
						list.add(ce);
					}
				}
			}
		}
		list.add(0, new ColorEntry("Default", Lighting.defaultBackgroundColor));
		list.add(0, new ColorEntry("Select", null));
		predefined = new ColorEntry[list.size()];
		list.toArray(predefined);
	}

}
