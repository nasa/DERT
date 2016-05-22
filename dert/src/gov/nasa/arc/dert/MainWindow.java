package gov.nasa.arc.dert;

import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.PopupMenuAction;
import gov.nasa.arc.dert.action.edit.BackgroundColorDialog;
import gov.nasa.arc.dert.action.edit.CoordAction;
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
import gov.nasa.arc.dert.view.lighting.LightPositionView;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ActivateZoomAction;
import gov.nasa.arc.dert.viewpoint.Compass;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
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

	// Undo/Redo management
	private UndoHandler undoHandler;

	// Menu for undo/redo and a few general operations
	private PopupMenuAction editMenu;

	// Tool bar
	private JPanel toolBar;

	// Tool bar buttons for opening views
	private ButtonAction marbleAction, helpAction, consoleAction, colorbarAction;
	private ButtonAction surfaceAndLayersAction, lightingAndShadowsAction, mapElementsAction;
	private ButtonAction viewpointAction, lightAction, resetAction;
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
		undoHandler = new UndoHandler();
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
		toolBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		// Menu for file operations (loading configurations, exit).
		PopupMenuAction fileMenu = new PopupMenuAction("file actions", "File", null) {
			@Override
			protected void fillMenu(JPopupMenu menu) {
				fillFileMenu(menu);
			}
		};
		toolBar.add(fileMenu);

		// Menu for edit operations (undo, redo, background color, stereo, CoR
		// cross hair visibility).
		editMenu = new PopupMenuAction("edit actions", "Edit", null) {
			@Override
			protected void fillMenu(JPopupMenu menu) {
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
				currentConfig.consoleState.getViewData().setVisible(true);
				currentConfig.consoleState.open();
			}
		};
		toolBar.add(consoleAction);

		// Open the help view.
		helpAction = new ButtonAction("open help", null, "help.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.helpState.getViewData().setVisible(true);
				currentConfig.helpState.open();
			}
		};
		toolBar.add(helpAction);

		toolBar.add(new JLabel(filler));

		// Open the surface and layers view.
		surfaceAndLayersAction = new ButtonAction("edit landscape surface and layers", null, "surfandlayer.png",
			buttonBorder) {
			@Override
			public void run() {
				currentConfig.surfAndLayerState.getViewData().setVisible(true);
				currentConfig.surfAndLayerState.open();
			}
		};
		surfaceAndLayersAction.setEnabled(false);
		toolBar.add(surfaceAndLayersAction);

		// Open the map elements view.
		mapElementsAction = new ButtonAction("edit map elements", null, "mapelements.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.mapElementsState.getViewData().setVisible(true);
				currentConfig.mapElementsState.open();
			}
		};
		mapElementsAction.setEnabled(false);
		toolBar.add(mapElementsAction);

		// Open the color bar view.
		colorbarAction = new ButtonAction("show color maps", null, "colorbar.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.colorBarsState.getViewData().setVisible(true);
				currentConfig.colorBarsState.open();
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
				currentConfig.lightingState.getViewData().setVisible(true);
				currentConfig.lightingState.open();
			}
		};
		lightingAndShadowsAction.setEnabled(false);
		toolBar.add(lightingAndShadowsAction);

		// Open the light positioning view.
		lightAction = new ButtonAction("position artificial light", null, "luxo.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.lightPosState.getViewData().setVisible(true);
				currentConfig.lightPosState.open();
			}
		};
		lightAction.setEnabled(false);
		toolBar.add(lightAction);

		toolBar.add(new JLabel(filler));

		// Open the viewpoint view.
		viewpointAction = new ButtonAction("show viewpoint properties", null, "viewpoint.png", buttonBorder) {
			@Override
			public void run() {
				currentConfig.viewPtState.getViewData().setVisible(true);
				currentConfig.viewPtState.open();
			}
		};
		viewpointAction.setEnabled(false);
		toolBar.add(viewpointAction);

		// Reset the viewpoint to the default overhead view.
		resetAction = new ButtonAction("reset viewpoint to overhead position", null, "reset.png", buttonBorder) {
			@Override
			public void run() {
				worldView.getViewpointNode().reset();
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
				currentConfig.marbleState.getViewData().setVisible(true);
				currentConfig.marbleState.open();
			}
		};
		marbleAction.setEnabled(false);
		toolBar.add(marbleAction);

		// Move the viewpoint to the current marble location.
		gotoMarble = new ButtonAction("go to marble", null, "gotomarble.png", buttonBorder) {
			@Override
			public void run() {
				// seek marble
				worldView.getViewpointNode().seek(World.getInstance().getMarble());
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
		CoordAction.listenerList.add(marbleLocField);
		marbleLocField.setEnabled(false);
		toolBar.add(marbleLocField);

		add(toolBar, BorderLayout.NORTH);
		currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();

		// Create the worldview.
		worldView = new WorldView();
		add(worldView, BorderLayout.CENTER);

		setSize(900, 600);
		setLocation(20, 20);
		setVisible(true);
		requestFocus();
	}
	
//	public void setCursor(Cursor cursor) {
//		super.setCursor(cursor);
//		System.err.println("MainWindow.setCursor "+cursor);
//	}

	protected void fillFileMenu(JPopupMenu fileMenu) {
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

	protected void fillEditMenu(JPopupMenu menu) {
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

		JCheckBoxMenuItem corXhair = new JCheckBoxMenuItem("Show Crosshair at Center of Rotation");
		corXhair.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				worldView.getScenePanel().setShowCrosshair(((JCheckBoxMenuItem) event.getSource()).getState());
			}
		});
		corXhair.setState(worldView.getScenePanel().isShowCrosshair());
		menu.add(corXhair);

		JCheckBoxMenuItem marble = new JCheckBoxMenuItem("Show Marble");
		marble.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				World.getInstance().getMarble().setVisible(((JCheckBoxMenuItem) event.getSource()).getState());
			}
		});
		marble.setState(World.getInstance().getMarble().isVisible());
		menu.add(marble);

		JCheckBoxMenuItem textOverlay = new JCheckBoxMenuItem("Show Text Overlay");
		textOverlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				worldView.getScenePanel().setShowTextOverlay(((JCheckBoxMenuItem) event.getSource()).getState());
			}
		});
		textOverlay.setState(worldView.getScenePanel().isShowTextOverlay());
		menu.add(textOverlay);
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
		viewpointAction.setEnabled(true);
		zoomAction.setEnabled(true);
		gotoMarble.setEnabled(true);
		resetAction.setEnabled(true);
		updateLightIcon();
		marbleLocField.setFormat(Landscape.format);
		marbleLocField.setEnabled(true);
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
			lightAction.setToolTipText("set position of artificial light");
		} else {
			lightAction.setIcon(Icons.getImageIcon("sun.png"));
			lightAction.setToolTipText("set position of sun");
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
//		marbleLocation.set(World.getInstance().getMarble().getTranslation());
//		// Convert from OpenGL to World coordinates
//		Landscape.getInstance().localToWorldCoordinate(marbleLocation);
//		marbleLocField.setValue(marbleLocation);
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

	/**
	 * Get the undo handler.
	 * 
	 * @return the handler
	 */
	public UndoHandler getUndoHandler() {
		return (undoHandler);
	}

	private JMenu getRecentSubmenu() {
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
		JMenu menu = new JMenu("Open Recent Configuration");
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
