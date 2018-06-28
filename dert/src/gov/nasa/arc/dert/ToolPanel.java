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

package gov.nasa.arc.dert;

import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.PopupMenuAction;
import gov.nasa.arc.dert.action.UndoHandler;
import gov.nasa.arc.dert.action.edit.BackgroundColorDialog;
import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.action.edit.PreferencesDialog;
import gov.nasa.arc.dert.action.edit.StereoDialog;
import gov.nasa.arc.dert.action.file.AboutAction;
import gov.nasa.arc.dert.action.file.DeleteConfigAction;
import gov.nasa.arc.dert.action.file.ExitAction;
import gov.nasa.arc.dert.action.file.OpenConfigAction;
import gov.nasa.arc.dert.action.file.SaveConfigAction;
import gov.nasa.arc.dert.action.file.SaveConfigAsAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tapemeasure.ActivateTapeMeasureAction;
import gov.nasa.arc.dert.state.ColorBarsState;
import gov.nasa.arc.dert.state.Configuration;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.HelpState;
import gov.nasa.arc.dert.state.LightPositionState;
import gov.nasa.arc.dert.state.LightingState;
import gov.nasa.arc.dert.state.MapElementsState;
import gov.nasa.arc.dert.state.MarbleState;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.state.SurfaceAndLayersState;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.lighting.LightPositionView;
import gov.nasa.arc.dert.viewpoint.ActivateZoomAction;
import gov.nasa.arc.dert.viewpoint.Compass;
import gov.nasa.arc.dert.viewpoint.ViewpointMenuAction;

import java.awt.CheckboxMenuItem;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ardor3d.math.type.ReadOnlyVector3;

public class ToolPanel
	extends JPanel {

	// Menu for undo/redo and a few general operations
	private PopupMenuAction editMenu;

	// Buttons for opening views
	private ButtonAction marbleAction, helpAction, consoleAction, colorbarAction;
	private ButtonAction surfaceAndLayersAction, lightingAndShadowsAction, mapElementsAction;
	private ButtonAction lightAction, resetAction;
	private CoordAction coordAction;

	// Button for activating the tape measure
	private ActivateTapeMeasureAction measuringAction;
	
	// Menu for viewpoint modes
	private ViewpointMenuAction viewpointMenuAction;

	// Button for activating magnify mode
	private ActivateZoomAction zoomAction;

	// Compass display
	private Compass compass;

	// Tool bar button that opens takes the viewpoint to the marble
	private ButtonAction gotoMarble;

	// Tool bar text field that holds the current marble location
	private CoordTextField marbleLocField;

	// Indicate that we have a configuration to save
	protected boolean haveConfig;
	
	// spacer for grouping buttons
	protected String filler = "    ";
	// no button borders on tool bar
	protected boolean buttonBorder = false;
	
	public ToolPanel() {
		super(new FlowLayout(FlowLayout.LEFT, 0, 2));
		// Tool Bar
		setBorder(BorderFactory.createEmptyBorder());
	}
	
	public void populate() {
		addMenus();
		addConsoleAndHelpButtons();
		addLandscapeButtons();
		addLightingButtons();
		addViewpointControls();
		addMarbleControls();
	}
	
	public void addMenus() {

		// Menu for file operations (loading configurations, exit).
		PopupMenuAction fileMenu = new PopupMenuAction("file actions", "File", null) {
			@Override
			protected void fillMenu(PopupMenu menu) {
				fillFileMenu(menu);
			}
		};
		add(fileMenu);

		// Menu for edit operations (undo, redo, background color, stereo, CoR
		// cross hair visibility).
		editMenu = new PopupMenuAction("general application actions", "Edit", null) {
			@Override
			protected void fillMenu(PopupMenu menu) {
				fillEditMenu(menu);
			}
		};
		editMenu.setEnabled(false);
		add(editMenu);

	}
	
	public void addConsoleAndHelpButtons() {

		add(new JLabel(filler));

		// Open the console view.
		consoleAction = new ButtonAction("open console", null, "console.png", buttonBorder) {
			@Override
			public void run() {
				Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				currentConfig.consoleState.open(true);
			}
		};
		add(consoleAction);

		// Open the help view.
		helpAction = new ButtonAction("open help", null, "help.png", buttonBorder) {
			@Override
			public void run() {
				Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				HelpState state = (HelpState)currentConfig.getState("HelpState");
				state.open(true);
			}
		};
		add(helpAction);

	}
	
	public void addLandscapeButtons() {

		add(new JLabel(filler));

		// Open the surface and layers view.
		surfaceAndLayersAction = new ButtonAction("configure landscape surface and layers", null, "surfandlayer.png",
			buttonBorder) {
			@Override
			public void run() {
				Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				SurfaceAndLayersState state = (SurfaceAndLayersState)currentConfig.getState("SurfaceAndLayersState");
				state.open(true);
			}
		};
		surfaceAndLayersAction.setEnabled(false);
		add(surfaceAndLayersAction);

		// Open the map elements view.
		mapElementsAction = new ButtonAction("edit map elements", null, "mapelements.png", buttonBorder) {
			@Override
			public void run() {
				Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				MapElementsState state = (MapElementsState)currentConfig.getState("MapElementsState");
				state.open(true);
			}
		};
		mapElementsAction.setEnabled(false);
		add(mapElementsAction);

		// Open the color bar view.
		colorbarAction = new ButtonAction("show color bars", null, "colorbar.png", buttonBorder) {
			@Override
			public void run() {
				Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				ColorBarsState state = (ColorBarsState)currentConfig.getState("ColorBarsState");
				state.open(true);
			}
		};
		colorbarAction.setEnabled(false);
		add(colorbarAction);

		// Activate the tape measure.
		measuringAction = new ActivateTapeMeasureAction();
		measuringAction.setEnabled(false);
		add(measuringAction);

		// Change between projected and unprojected coordinates.
		coordAction = new CoordAction();
		coordAction.setEnabled(false);
		add(coordAction);

	}
	
	public void addLightingButtons() {

		add(new JLabel(filler));

		// Open the lighting and shadows view.
		lightingAndShadowsAction = new ButtonAction("edit lighting and shadows", null, "lightandshadow.png",
			buttonBorder) {
			@Override
			public void run() {
				Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				LightingState state = (LightingState)currentConfig.getState("LightingState");
				state.open(true);
			}
		};
		lightingAndShadowsAction.setEnabled(false);
		add(lightingAndShadowsAction);

		// Open the light positioning view.
		lightAction = new ButtonAction("set light position", null, "luxo.png", buttonBorder) {
			@Override
			public void run() {
				Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				LightPositionState state = (LightPositionState)currentConfig.getState("LightPositionState");
				state.open(true);
			}
		};
		lightAction.setEnabled(false);
		add(lightAction);

	}
	
	public void addViewpointControls() {

		add(new JLabel(filler));

		// Open the viewpoint view.
		viewpointMenuAction = ViewpointMenuAction.getInstance();
		viewpointMenuAction.setEnabled(false);
		add(viewpointMenuAction);

		// Reset the viewpoint to the default overhead view.
		resetAction = new ButtonAction("reset viewpoint to overhead position", null, "reset.png", buttonBorder) {
			@Override
			public void run() {
				Dert.getWorldView().getViewpoint().reset();
			}
		};
		resetAction.setEnabled(false);
		add(resetAction);

		// Activate/deactivate magnify mode.
		zoomAction = new ActivateZoomAction();
		zoomAction.setEnabled(false);
		add(zoomAction);

		// Add the compass.
		compass = new Compass();
		add(compass);

	}
	
	public void addMarbleControls() {

		add(new JLabel(filler));

		// Open the marble info window.
		marbleAction = new ButtonAction("show terrain attributes at marble location", null, "marble.png", buttonBorder) {
			@Override
			public void run() {
				Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				MarbleState state = (MarbleState)currentConfig.getState("MarbleState");
				state.open(true);
			}
		};
		marbleAction.setEnabled(false);
		add(marbleAction);

		// Move the viewpoint to the current marble location.
		gotoMarble = new ButtonAction("go to marble", null, "gotomarble.png", buttonBorder) {
			@Override
			public void run() {
				// seek marble
				Dert.getWorldView().getScenePanel().getViewpointController().seek(World.getInstance().getMarble());
			}
		};
		gotoMarble.setEnabled(false);
		add(gotoMarble);

		// The marble location field.
		marbleLocField = new CoordTextField(20, "current marble location", "0.000", true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				World.getInstance().getMarble().update(result, null, null);
			}
		};
		marbleLocField.setEnabled(false);
		add(marbleLocField);

	}

	protected void fillFileMenu(PopupMenu fileMenu) {
		fileMenu.add(new AboutAction());
		fileMenu.add(new OpenConfigAction());
		fileMenu.add(getRecentSubmenu());
		
		MenuItemAction menuItemAction = new SaveConfigAction();
		menuItemAction.setEnabled(haveConfig);
		fileMenu.add(menuItemAction);
		
		menuItemAction = new SaveConfigAsAction();
		menuItemAction.setEnabled(haveConfig);
		fileMenu.add(menuItemAction);

		fileMenu.add(getDeleteMenuItem());

		fileMenu.addSeparator();
		
		menuItemAction = new ExitAction();
		fileMenu.add(menuItemAction);
	}

	protected void fillEditMenu(PopupMenu menu) {
		UndoHandler undoHandler = UndoHandler.getInstance();
		menu.add(undoHandler.getUndoAction());
		menu.add(undoHandler.getRedoAction());
		menu.addSeparator();

		MenuItemAction bgColAction = new MenuItemAction("Change Background Color") {
			@Override
			protected void run() {
				BackgroundColorDialog dialog = new BackgroundColorDialog();
				dialog.open();
			}
		};
		menu.add(bgColAction);

		MenuItemAction stereoAction = new MenuItemAction("Stereo") {
			@Override
			protected void run() {
				StereoDialog dialog = new StereoDialog();
				dialog.open();
			}
		};
		menu.add(stereoAction);

		CheckboxMenuItem corXhair = new CheckboxMenuItem("Show Crosshair at Center of Rotation");
		corXhair.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				Dert.getWorldView().getScenePanel().setShowCrosshair(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		corXhair.setState(Dert.getWorldView().getScenePanel().isShowCrosshair());
		menu.add(corXhair);

		CheckboxMenuItem marble = new CheckboxMenuItem("Show Marble");
		marble.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				World.getInstance().getMarble().setVisible(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		marble.setState(World.getInstance().getMarble().isVisible());
		menu.add(marble);

		CheckboxMenuItem textOverlay = new CheckboxMenuItem("Show Text Overlay");
		textOverlay.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				Dert.getWorldView().getScenePanel().setShowTextOverlay(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		textOverlay.setState(Dert.getWorldView().getScenePanel().isShowTextOverlay());
		menu.add(textOverlay);

		CheckboxMenuItem scaleOverlay = new CheckboxMenuItem("Show Center Scale Overlay");
		scaleOverlay.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				Dert.getWorldView().getScenePanel().setShowCenterScale(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		scaleOverlay.setState(Dert.getWorldView().getScenePanel().isShowCenterScale());
		menu.add(scaleOverlay);

		CheckboxMenuItem mapElementsOnTopAction = new CheckboxMenuItem("Map Elements On Top");
		mapElementsOnTopAction.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				World.getInstance().setMapElementsOnTop(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		mapElementsOnTopAction.setState(World.getInstance().isMapElementsOnTop());
		menu.add(mapElementsOnTopAction);

		CheckboxMenuItem toolHiddenDashedAction = new CheckboxMenuItem("Show Hidden Lines As Dashes");
		toolHiddenDashedAction.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				World.getInstance().setHiddenDashed(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		toolHiddenDashedAction.setState(World.getInstance().isHiddenDashed());
		menu.add(toolHiddenDashedAction);

		MenuItemAction prefAction = new MenuItemAction("Edit Map Element Preferences") {
			@Override
			protected void run() {
				PreferencesDialog dialog = new PreferencesDialog();
				dialog.open();
			}
		};
		menu.add(prefAction);

		MenuItemAction canvasSizeAction = new MenuItemAction("Set WorldView Canvas Dimensions") {
			@Override
			protected void run() {
				String result = OptionDialog.showSingleInputDialog(Dert.getMainWindow(), "Enter dimensions (width,height).", 
						Dert.getWorldView().getScenePanel().getWidth()+","+Dert.getWorldView().getScenePanel().getHeight());
				if ((result != null) && !result.isEmpty()) {
					String[] token = result.trim().split(",");
					if (token.length < 2) {
						OptionDialog.showErrorMessageDialog(Dert.getMainWindow(), "Invalid entry "+result+".");
						return;
					}
					try {
						int width = Integer.parseInt(token[0]);
						int height = Integer.parseInt(token[1]);
						Dert.getWorldView().getScenePanel().setPreferredSize(new Dimension(width, height));
						Dert.getMainWindow().pack();
						Console.println("WorldView Canvas Dimensions: "+Dert.getWorldView().getScenePanel().getCanvas().getWidth()+","+Dert.getWorldView().getScenePanel().getCanvas().getHeight());
					}
					catch (Exception e) {
						OptionDialog.showErrorMessageDialog(Dert.getMainWindow(), "Invalid entry "+result+".");
					}
				}
			}
		};
		menu.add(canvasSizeAction);
	}

	protected Menu getRecentSubmenu() {
		String[] recent = ConfigurationManager.getInstance().getRecentConfigurations();
		MenuItemAction[] menuItem = new MenuItemAction[recent.length];
		for (int i = 0; i < recent.length; ++i) {
			menuItem[i] = new MenuItemAction(recent[i], recent[i]) {
				@Override
				public void run() {
					String configPath = ConfigurationManager.getInstance().getRecentConfigurationPath((String) arg);
					if (configPath != null) {
						Configuration config = ConfigurationManager.getInstance().loadConfiguration(configPath);
						if (config != null)
							ConfigurationManager.getInstance().openConfiguration(config);
					}
				}
			};
		}
		Menu menu = new Menu("Open Recent Configuration");
		for (int i = 0; i < menuItem.length; ++i) {
			menu.add(menuItem[i]);
		}
		return (menu);
	}

	protected MenuItem getDeleteMenuItem() {
		String path = Dert.getUserPath();
		File file = new File(path, "config");
		if (file.exists()) {
			String[] configNames = ConfigurationManager.getInstance().getConfigList(path);
			if (configNames.length > 0) {
				MenuItemAction[] menuItem = new MenuItemAction[configNames.length];
				for (int i = 0; i < configNames.length; ++i) {
					menuItem[i] = new MenuItemAction(configNames[i], configNames[i]) {
						@Override
						public void run() {
							String[] filePath = new String[] {(String)arg};
							DeleteConfigAction.doDelete(filePath);
						}
					};
				}
				Menu menu = new Menu("Delete Configuration");
				for (int i = 0; i < menuItem.length; ++i) {
					menu.add(menuItem[i]);
				}
				menu.addSeparator();
				menu.add(new DeleteConfigAction("Select Landscape ..."));
				return (menu);
			}
		}
		return(new DeleteConfigAction("Delete Configuration ..."));
	}

	/**
	 * Set the current configuration and enable the tool bar controls.
	 * 
	 * @param config
	 *            the configuration
	 */
	public void setConfiguration(final Configuration config) {
		haveConfig = true;
		editMenu.setEnabled(true);
		marbleAction.setEnabled(true);
		surfaceAndLayersAction.setEnabled(true);
		lightingAndShadowsAction.setEnabled(true);
		mapElementsAction.setEnabled(true);
		colorbarAction.setEnabled(true);
		lightAction.setEnabled(true);
		measuringAction.setEnabled(true);
		coordAction.setEnabled(true);
		coordAction.setSelected(World.getInstance().getUseLonLat());
		viewpointMenuAction.setEnabled(true);
		zoomAction.setEnabled(true);
		gotoMarble.setEnabled(true);
		resetAction.setEnabled(true);
		updateLightIcon();
		marbleLocField.setFormat(Landscape.format);
		marbleLocField.setEnabled(true);
		CoordAction.listenerList.add(marbleLocField);
	}

	/**
	 * Update the light positioning icon according to solar or artificial light.
	 */
	public void updateLightIcon() {
		Lighting lighting = World.getInstance().getLighting();
		if (lighting.isLampMode()) {
			lightAction.setIcon(Icons.getImageIcon("luxo.png"));
		} else {
			lightAction.setIcon(Icons.getImageIcon("sun.png"));
		}
		State state = ConfigurationManager.getInstance().getCurrentConfiguration().getState("LightPositionState");
		if (state != null) {
			LightPositionView view = (LightPositionView) state.getViewData().getView();
			if (view != null) {
				view.setMode();
			}
		}
	}

	/**
	 * Update the marble location field contents when the marble moves.
	 */
	public void updateMarbleLocationField() {
		marbleLocField.setLocalValue(World.getInstance().getMarble().getTranslation());
	}

	/**
	 * Update the compass as the viewpoint moves.
	 * 
	 * @param azimuth
	 *            viewpoint azimuth
	 */
	public void updateCompass(double azimuth) {
		compass.setValue(azimuth);
	}

}
