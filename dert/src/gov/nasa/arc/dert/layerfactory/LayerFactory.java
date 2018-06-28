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

package gov.nasa.arc.dert.layerfactory;

import gov.nasa.arc.dert.action.file.AboutBox;
import gov.nasa.arc.dert.landscape.layer.LayerInfo.LayerType;
import gov.nasa.arc.dert.raster.RasterFile;
import gov.nasa.arc.dert.raster.geotiff.GTIF;
import gov.nasa.arc.dert.raster.pds.PDS;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * Provides a tool to create a multi-resolution tiled pyramid for use as a layer
 * in a landscape. Supported file formats are PDS and GeoTIFF including BigTIFF.
 * 
 * If given all required arguments, the tool will run headless. Otherwise it
 * will produce a GUI.
 *
 */
public class LayerFactory {

	// The DERT version
	public static String VERSION;

	// The globe name selection for the user
	public static String[] GLOBE_NAME = { "Use Metadata", "Earth", "Mars", "Moon" };

	// The tile size selection for the user
	public static final String[] TILE_SIZE = { "2048", "1024", "512", "256", "128", "64", "32", "16" };

	// The layer type selection
	public static final LayerType[] LAYER_TYPE = { LayerType.elevation, LayerType.grayimage, LayerType.colorimage, LayerType.field };

	// GUI components
	private JButton applyButton, cancelButton;
	private JTextField messageText;
	private JFrame mainFrame;
	private JTabbedPane tabbedPane;

	// Properties
	private Properties dertProperties;

	// Command line arguments
	private String landscapePath;
	private String filePath;
	private String missing;
	private int tileSize;
	private LayerType layerType;
	private String layerName;
	private String globe;
	private int[] margin;
	private Color color;
	private String elevAttrName;

	// This is a vector file so it needs to be rendered
	private boolean isVector;

	// Panel that handles raster files
	private RasterLayerPanel rasterLayerPanel;

	// Panel that handles vector files
	private VectorLayerPanel vectorLayerPanel;
	
	// About
	private String version;
	
	private String[] args;
	private String pathStr;

	/**
	 * Main
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("-usage")) {
				System.out.println("layerfactory -landscape=landscapePath -file=inputFilePath -tilesize=tileSize -type=layerType "+
						"[-globe=globename] [-missing=missingValue] [-name=layerName] [-leftmargin=numPixels] [-rightmargin=numPixels]"+
						" [-bottommargin=numPixels] [-topmargin=numPixels] [-color=R,G,B,A] [-elevattrname=elevation attribute name]");
				System.exit(0);
			}
		}
		LayerFactory lf = new LayerFactory(args);
		VERSION = "LayerFactory " + lf.version;
		lf.createLayer();
	}

	/**
	 * Create the dialog
	 * 
	 * @param args
	 *            command line arguments
	 */
	public LayerFactory(String[] args) {
		this.args = args;

		// Find the application location.
		pathStr = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		if (pathStr.toLowerCase().endsWith(".jar")) {
			int p = pathStr.lastIndexOf('/');
			pathStr = pathStr.substring(0, p + 1);
		} else {
			pathStr += "../";
		}

		// Load properties.
		dertProperties = new Properties();
		try {
			File file = new File(pathStr, "dert.properties");
			dertProperties.load(new FileInputStream(file));
			version = dertProperties.getProperty("Dert.Version", "");
			setGlobes(dertProperties);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static void setGlobes(Properties properties) {
		String globes = properties.getProperty("Globes", null);
		if (globes != null) {
			GLOBE_NAME = globes.split(",");
		}
		String defaultGlobe = properties.getProperty("DefaultGlobe", null);
		defaultGlobe = (String) StringUtil.findString(defaultGlobe, GLOBE_NAME, true);
		PyramidLayerFactory.defaultGlobe = defaultGlobe;
	}
	
	public boolean createLayer() {

		// Do the build if all arguments are present and then exit.
		if (checkArgs(args)) {
			try {
				if (isVector) {
					VectorPyramidLayerFactory factory = new VectorPyramidLayerFactory(filePath);
					factory.buildPyramid(landscapePath, layerName, color, elevAttrName, messageText);
				} else {
					RasterFile rf = null;
					if (filePath.toLowerCase().endsWith(".img")) {
						rf = new PDS(filePath, dertProperties);
					} else {
						rf = new GTIF(filePath, dertProperties);
					}
					rf.open("r");
					rf.close();

					// Get optional temporary file path
					String tmpPath = dertProperties.getProperty("LayerTemporaryPath", null);
					if ((tmpPath != null) && tmpPath.startsWith("$"))
						tmpPath = System.getProperty(tmpPath.substring(1));

					RasterPyramidLayerFactory factory = new RasterPyramidLayerFactory(rf, tmpPath);
					factory.buildPyramid(landscapePath, globe, layerType, layerName, tileSize, missing, margin, null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return(false);
			}
			return(true);
		}
		// Show GUI to get more arguments.
		else {

			mainFrame = new JFrame("DERT Layer Factory");

			mainFrame.getContentPane().setLayout(new BorderLayout());
			messageText = new JTextField();
			messageText.setEditable(false);
			messageText.setBackground(mainFrame.getBackground());
			messageText.setForeground(Color.blue);
			messageText.setBorder(null);
			mainFrame.getContentPane().add(messageText, BorderLayout.NORTH);
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			JButton aboutButton = new JButton("About");
			aboutButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					AboutBox box = new AboutBox(mainFrame, "DERT Layer Factory", version);
					box.open();
				}
			});
			buttonsPanel.add(aboutButton);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					cancelPressed();
				}
			});
			buttonsPanel.add(cancelButton);

			applyButton = new JButton("Apply");
			applyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					applyPressed();
				}
			});
			buttonsPanel.add(applyButton);
			mainFrame.getRootPane().setDefaultButton(applyButton);
			mainFrame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

			tabbedPane = new JTabbedPane();
			mainFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

			rasterLayerPanel = new RasterLayerPanel(messageText, dertProperties, landscapePath, filePath, missing,
				tileSize, layerType, layerName, globe, margin);
			rasterLayerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			tabbedPane.addTab("Raster Layer", rasterLayerPanel);

			vectorLayerPanel = new VectorLayerPanel(messageText, landscapePath, filePath, layerName, color,
				elevAttrName);
			vectorLayerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			tabbedPane.addTab("Vector Layer", vectorLayerPanel);

			tabbedPane.setSelectedIndex(0);

			mainFrame.setMinimumSize(new Dimension(800, 350));
			mainFrame.pack();
			mainFrame.setVisible(true);
			mainFrame.validate();
		}
		return(true);
	}

	/**
	 * Perform the pyramid build if all required arguments are present.
	 */
	protected boolean checkArgs(String[] args) {

		landscapePath = null;
		layerName = null;
		filePath = null;
		missing = null;
		tileSize = 0;
		layerType = null;
		margin = null;
		globe = PyramidLayerFactory.defaultGlobe;
		color = Color.white;
		elevAttrName = null;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].startsWith("-landscape=")) {
				landscapePath = args[i].substring(11);
				if (landscapePath.isEmpty()) {
					landscapePath = null;
				}
			} else if (args[i].startsWith("-globe=")) {
				globe = args[i].substring(7);
				if (globe.isEmpty()) {
					globe = null;
				} else {
					globe = globe.substring(0, 1).toUpperCase() + globe.substring(1).toLowerCase();
					globe = (String) StringUtil.findString(globe, GLOBE_NAME, true);
				}
				if (globe == null) {
					globe = PyramidLayerFactory.defaultGlobe;
				}
			} else if (args[i].startsWith("-file=")) {
				filePath = args[i].substring(6);
				if (filePath.isEmpty()) {
					filePath = null;
				}
			} else if (args[i].startsWith("-missing=")) {
				missing = args[i].substring(9);
				if (missing.isEmpty()) {
					missing = null;
				}
			} else if (args[i].startsWith("-tilesize=")) {
				String str = args[i].substring(10);
				if (!str.isEmpty()) {
					str = (String) StringUtil.findString(str, TILE_SIZE, false);
					tileSize = Integer.parseInt(str);
				}
			} else if (args[i].startsWith("-color=")) {
				String str = args[i].substring(7);
				try {
					color = StringUtil.stringToColor(str);
				} catch (Exception e) {
					color = null;
				}
				if (color == null) {
					System.out.println("Invalid color " + str + ". Setting to white.");
					color = Color.white;
				}
			} else if (args[i].startsWith("-type=")) {
				String str = args[i].substring(6);
				if (str.isEmpty()) {
					layerType = null;
				} else {
					layerType = (LayerType) StringUtil.findString(str, LAYER_TYPE, true);
				}
			} else if (args[i].startsWith("-name=")) {
				layerName = args[i].substring(11);
				if (layerName.isEmpty()) {
					layerName = null;
				}
			} else if (args[i].startsWith("-leftmargin=")) {
				if (margin == null) {
					margin = new int[4];
				}
				String str = args[i].substring(12);
				margin[0] = Integer.parseInt(str, 0);
			} else if (args[i].startsWith("-rightmargin=")) {
				if (margin == null) {
					margin = new int[4];
				}
				String str = args[i].substring(13);
				margin[1] = Integer.parseInt(str, 0);
			} else if (args[i].startsWith("-bottommargin=")) {
				if (margin == null) {
					margin = new int[4];
				}
				String str = args[i].substring(14);
				margin[2] = Integer.parseInt(str, 0);
			} else if (args[i].startsWith("-topmargin=")) {
				if (margin == null) {
					margin = new int[4];
				}
				String str = args[i].substring(11);
				margin[3] = Integer.parseInt(str, 0);
			} else if (args[i].startsWith("-elevattrname=")) {
				elevAttrName = args[i].substring(14);
			}
		}
		if ((landscapePath == null) || (filePath == null)) {
			return (false);
		}
		String fPath = filePath.toLowerCase();
		isVector = fPath.endsWith(".json");
		if (isVector) {
			if (layerName == null) {
				layerName = StringUtil.getLabelFromFilePath(filePath);
			}
			if (color == null) {
				color = Color.white;
			}
			return (true);
		}
		if (layerType == null) {
			layerType = LayerType.elevation;
			return (false);
		}
		if (layerType == LayerType.elevation) {
			layerName = "elevation";
		} else if (layerName == null) {
			layerName = StringUtil.getLabelFromFilePath(filePath);
		}
		if (tileSize == 0) {
			return (false);
		}
		if (!(fPath.endsWith(".img") || fPath.endsWith(".tiff") || fPath.endsWith(".tif") || fPath.endsWith(".gtif") || fPath
			.endsWith(".gtiff"))) {
			throw new IllegalArgumentException(
				"Only NASA PDS and GeoTIFF image formats and GeoJSON vector formats are supported.");
		}
		return (true);
	}

	/**
	 * Apply button was pressed
	 */
	protected void applyPressed() {
		int index = tabbedPane.getSelectedIndex();
		Thread thread = null;
		switch (index) {
		case 0:
			if (!rasterLayerPanel.applyPressed()) {
				return;
			}
			applyButton.setEnabled(false);
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Thread.yield();
					if (rasterLayerPanel.run()) {
						mainFrame.dispose();
						System.exit(0);
					} else {
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								applyButton.setEnabled(true);
							}
						});
					}
				}
			});
			break;
		case 1:
			if (!vectorLayerPanel.applyPressed()) {
				return;
			}
			applyButton.setEnabled(false);
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Thread.yield();
					if (vectorLayerPanel.run()) {
						mainFrame.dispose();
						System.exit(0);
					} else {
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								applyButton.setEnabled(true);
							}
						});
					}
				}
			});
			break;
		}
		if (thread != null) {
			thread.start();
		}
	}

	/**
	 * Cancel button was pressed
	 */
	protected void cancelPressed() {
		boolean close = rasterLayerPanel.cancelPressed() || vectorLayerPanel.cancelPressed();
		if (close) {
			mainFrame.dispose();
			System.exit(0);
		} else {
			applyButton.setEnabled(true);
		}
	}

}
