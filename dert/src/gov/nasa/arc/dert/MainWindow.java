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
import gov.nasa.arc.dert.action.file.OpenConfigAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tapemeasure.ActivateTapeMeasureAction;
import gov.nasa.arc.dert.state.Configuration;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.lighting.LightPositionView;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ActivateZoomAction;
import gov.nasa.arc.dert.viewpoint.Compass;
import gov.nasa.arc.dert.viewpoint.ViewpointMenuAction;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Menu;
import java.awt.PopupMenu;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * A JFrame that provides the main application window for DERT.
 *
 */
public class MainWindow extends JFrame {

	// The world view
	private WorldView worldView;

	// The current configuration
	private Configuration currentConfig;

	// Menu for undo/redo and a few general operations
	private PopupMenuAction editMenu;
	
	// Menu for viewpoint modes
	private ViewpointMenuAction viewpointMenuAction;

	// Tool bar
	private JPanel toolBar;

	// Tool bar buttons for opening views
	private ButtonAction marbleAction, helpAction, consoleAction, colorbarAction;
	private ButtonAction surfaceAndLayersAction, lightingAndShadowsAction, mapElementsAction;
	private ButtonAction lightAction, resetAction;
	private CoordAction coordAction;

	// Tool bar button for activating magnify mode
	private ActivateZoomAction zoomAction;

	// Tool bar button for activating the tape measure
	private ActivateTapeMeasureAction measuringAction;

	// Tool bar text field that holds the current marble location
	private CoordTextField marbleLocField;

	// Tool bar button that opens takes the viewpoint to the marble
	private ButtonAction gotoMarble;

	// Tool bar compass
	private Compass compass;

	// Marble location object
//	private Vector3 marbleLocation;

	// String for about window
//	private String aboutStr;

	// Version string for configuration
	private String version;

	// Indicate that we have a configuration to save
	private boolean haveConfig;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            path to DERT executable
	 * @param args
	 *            command line arguments
	 * @param properties
	 *            dert properties
	 */
	public MainWindow(String title, String path, String[] args, Properties properties) {
		super(title);

		// Initialize
//		marbleLocation = new Vector3();
		version = properties.getProperty("Dert.Version");

		// User interface details.
		setDefaultFont();
		setIconImage(Icons.getImage("dert_24.png"));

		// Catch close operations for a clean exit.
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Dert.quit();
			}
		});

		setLayout(new BorderLayout());

		// Tool Bar
		String filler = "    "; // spacer for grouping tool bar buttons
		boolean buttonBorder = false; // no button borders on tool bar
		toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
		toolBar.setBorder(BorderFactory.createEmptyBorder());

		// Menu for file operations (loading configurations, exit).
		PopupMenuAction fileMenu = new PopupMenuAction("file actions", "File", null) {
			@Override
			protected void fillMenu(PopupMenu menu) {
				fillFileMenu(menu);
			}
		};
		toolBar.add(fileMenu);

		// Menu for edit operations (undo, redo, background color, stereo, CoR
		// cross hair visibility).
		editMenu = new PopupMenuAction("general application actions", "Edit", null) {
			@Override
			protected void fillMenu(PopupMenu menu) {
				fillEditMenu(menu);
			}
		};
		editMenu.setEnabled(false);
		toolBar.add(editMenu);

		toolBar.add(new JLabel(filler));

		// Open the console view.
		consoleAction = new ButtonAction("open console", null, "console.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.consoleState.open(true);
			}
		};
		toolBar.add(consoleAction);

		// Open the help view.
		helpAction = new ButtonAction("open help", null, "help.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.helpState.open(true);
			}
		};
		toolBar.add(helpAction);

		toolBar.add(new JLabel(filler));

		// Open the surface and layers view.
		surfaceAndLayersAction = new ButtonAction("configure landscape surface and layers", null, "surfandlayer.png",
			buttonBorder) {
			@Override
			public void run() {
				currentConfig.surfAndLayerState.open(true);
			}
		};
		surfaceAndLayersAction.setEnabled(false);
		toolBar.add(surfaceAndLayersAction);

		// Open the map elements view.
		mapElementsAction = new ButtonAction("edit map elements", null, "mapelements.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.mapElementsState.open(true);
			}
		};
		mapElementsAction.setEnabled(false);
		toolBar.add(mapElementsAction);

		// Open the color bar view.
		colorbarAction = new ButtonAction("show color bars", null, "colorbar.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.colorBarsState.open(true);
			}
		};
		colorbarAction.setEnabled(false);
		toolBar.add(colorbarAction);

		// Activate the tape measure.
		measuringAction = new ActivateTapeMeasureAction();
		measuringAction.setEnabled(false);
		toolBar.add(measuringAction);

		// Change between projected and unprojected coordinates.
		coordAction = new CoordAction();
		coordAction.setEnabled(false);
		toolBar.add(coordAction);

		toolBar.add(new JLabel(filler));

		// Open the lighting and shadows view.
		lightingAndShadowsAction = new ButtonAction("edit lighting and shadows", null, "lightandshadow.png",
			buttonBorder) {
			@Override
			public void run() {
				currentConfig.lightingState.open(true);
			}
		};
		lightingAndShadowsAction.setEnabled(false);
		toolBar.add(lightingAndShadowsAction);

		// Open the light positioning view.
		lightAction = new ButtonAction("set light position", null, "luxo.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.lightPosState.open(true);
			}
		};
		lightAction.setEnabled(false);
		toolBar.add(lightAction);

		toolBar.add(new JLabel(filler));

		// Open the viewpoint view.
		viewpointMenuAction = ViewpointMenuAction.getInstance();
		viewpointMenuAction.setEnabled(false);
		toolBar.add(viewpointMenuAction);

		// Reset the viewpoint to the default overhead view.
		resetAction = new ButtonAction("reset viewpoint to overhead position", null, "reset.png", buttonBorder) {
			@Override
			public void run() {
				worldView.getViewpoint().reset();
			}
		};
		resetAction.setEnabled(false);
		toolBar.add(resetAction);

		// Activate/deactivate magnify mode.
		zoomAction = new ActivateZoomAction();
		zoomAction.setEnabled(false);
		toolBar.add(zoomAction);

		// Add the compass.
		compass = new Compass();
		toolBar.add(compass);

		toolBar.add(new JLabel(filler));

		// Open the marble info window.
		marbleAction = new ButtonAction("show terrain attributes at marble location", null, "marble.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.marbleState.open(true);
			}
		};
		marbleAction.setEnabled(false);
		toolBar.add(marbleAction);

		// Move the viewpoint to the current marble location.
		gotoMarble = new ButtonAction("go to marble", null, "gotomarble.png", buttonBorder) {
			@Override
			public void run() {
				// seek marble
				worldView.getScenePanel().getViewpointController().seek(World.getInstance().getMarble());
			}
		};
		gotoMarble.setEnabled(false);
		toolBar.add(gotoMarble);

		// The marble location field.
		marbleLocField = new CoordTextField(20, "current marble location", "0.000", true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				World.getInstance().getMarble().update(result, null, null);
			}
		};
		marbleLocField.setEnabled(false);
		toolBar.add(marbleLocField);

		add(toolBar, BorderLayout.NORTH);
		currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();

		// Create the worldview.
		worldView = new WorldView();
		add(worldView, BorderLayout.CENTER);

		// Do not use preferred size here. It seems to cause the SceneCanvasPanel to randomly cover the SceneCanvas.
		setSize(currentConfig.worldState.getViewData().getWidth(), currentConfig.worldState.getViewData().getHeight());
		setLocation(currentConfig.worldState.getViewData().getX(), currentConfig.worldState.getViewData().getY());
		setVisible(true);
		requestFocus();
	}
	
//	public void setCursor(Cursor cursor) {
//		super.setCursor(cursor);
//		System.err.println("MainWindow.setCursor "+cursor);
//	}

	protected void fillFileMenu(PopupMenu fileMenu) {
		fileMenu.add(new AboutAction(version));
		fileMenu.add(new OpenConfigAction());
		fileMenu.add(getRecentSubmenu());

		MenuItemAction saveItem = new MenuItemAction("Save Configuration") {
			@Override
			public void run() {
				ConfigurationManager.getInstance().saveCurrentConfiguration();
				currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				setTitle("Desktop Exploration of Remote Terrain: " + currentConfig.toString());
			}
		};
		saveItem.setEnabled(haveConfig);
		fileMenu.add(saveItem);

		MenuItemAction saveAsItem = new MenuItemAction("Save Configuration As ...") {
			@Override
			public void run() {
				ConfigurationManager.getInstance().saveCurrentConfigurationAs(true);
				currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
				setTitle("Desktop Exploration of Remote Terrain - " + Landscape.getInstance().getGlobeName()
					+ ":" + World.getInstance().getName() + ":" + currentConfig.toString());
			}
		};
		saveAsItem.setEnabled(haveConfig);
		fileMenu.add(saveAsItem);

		MenuItemAction deleteConfigItem = new DeleteConfigAction();
		fileMenu.add(deleteConfigItem);

		fileMenu.addSeparator();
		MenuItemAction exitItem = new MenuItemAction("Exit") {
			@Override
			public void run() {
				Dert.quit();
			}
		};
		fileMenu.add(exitItem);
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
				worldView.getScenePanel().setShowCrosshair(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		corXhair.setState(worldView.getScenePanel().isShowCrosshair());
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
				worldView.getScenePanel().setShowTextOverlay(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		textOverlay.setState(worldView.getScenePanel().isShowTextOverlay());
		menu.add(textOverlay);

		CheckboxMenuItem scaleOverlay = new CheckboxMenuItem("Show Center Scale Overlay");
		scaleOverlay.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				worldView.getScenePanel().setShowCenterScale(event.getStateChange() == ItemEvent.SELECTED);
			}
		});
		scaleOverlay.setState(worldView.getScenePanel().isShowCenterScale());
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
				String result = OptionDialog.showSingleInputDialog(MainWindow.this, "Enter dimensions (width,height).", 
						worldView.getScenePanel().getWidth()+","+worldView.getScenePanel().getHeight());
				if ((result != null) && !result.isEmpty()) {
					String[] token = result.trim().split(",");
					if (token.length < 2) {
						OptionDialog.showErrorMessageDialog(MainWindow.this, "Invalid entry "+result+".");
						return;
					}
					try {
						int width = Integer.parseInt(token[0]);
						int height = Integer.parseInt(token[1]);
						worldView.getScenePanel().setPreferredSize(new Dimension(width, height));
						pack();
						Console.println("WorldView Canvas Dimensions: "+worldView.getScenePanel().getCanvas().getWidth()+","+worldView.getScenePanel().getCanvas().getHeight());
					}
					catch (Exception e) {
						OptionDialog.showErrorMessageDialog(MainWindow.this, "Invalid entry "+result+".");
					}
				}
			}
		};
		menu.add(canvasSizeAction);
	}

	/**
	 * Set the current configuration and enable the tool bar controls.
	 * 
	 * @param config
	 *            the configuration
	 */
	public void setConfiguration(final Configuration config) {
		currentConfig = config;
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
		setTitle("Desktop Exploration of Remote Terrain - " + Landscape.getInstance().getGlobeName() + ":"
			+ World.getInstance().getName() + ":" + currentConfig.toString());
		worldView.getScenePanel().getCanvas().requestFocusInWindow();
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
		State state = currentConfig.lightPosState;
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

	/**
	 * Get the world view.
	 * 
	 * @return the worldview
	 */
	public WorldView getWorldView() {
		return (worldView);
	}

	private Menu getRecentSubmenu() {
		String[] recent = ConfigurationManager.getInstance().getRecentConfigurations();
		MenuItemAction[] menuItem = new MenuItemAction[recent.length];
		for (int i = 0; i < recent.length; ++i) {
			menuItem[i] = new MenuItemAction(recent[i], recent[i]) {
				@Override
				public void run() {
					String configPath = ConfigurationManager.getInstance().getRecentConfigurationPath((String) arg);
					if (configPath != null) {
						ConfigurationManager.getInstance().openConfiguration(configPath);
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

	private void setDefaultFont() {
		// UIManager.getDefaults().put("defaultFont", new
		// FontUIResource("Lucida-Grande", Font.PLAIN, 10));
		FontUIResource fuir = new FontUIResource("Lucida-Grande", Font.PLAIN, 12);
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if ((value != null) && (value instanceof FontUIResource)) {
				UIManager.put(key, fuir);
			}
		}

	}
}
