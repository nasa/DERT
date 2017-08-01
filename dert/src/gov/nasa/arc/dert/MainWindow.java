package gov.nasa.arc.dert;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.Configuration;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.view.world.WorldView;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

/**
 * A JFrame that provides the main application window for DERT.
 *
 */
public class MainWindow extends JFrame {

	// The world view
	private WorldView worldView;

	// The current configuration
	private Configuration currentConfig;

	// Tool Panel
	private ToolPanel toolPanel;

	// Version string for configuration
	protected Properties properties;
	
	// Title string
	protected String title;

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
		this.title = title;

		// Initialize
		this.properties = properties;

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

		// Tool Panel
		toolPanel = new ToolPanel();
		add(toolPanel, BorderLayout.NORTH);
		currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();

		// Create the worldview.
		worldView = new WorldView();
		add(worldView, BorderLayout.CENTER);

		// Do not use preferred size here. It seems to cause the SceneCanvasPanel to randomly cover the SceneCanvas.
		setSize(currentConfig.worldState.getViewData().getWidth(), currentConfig.worldState.getViewData().getHeight());
		setLocation(currentConfig.worldState.getViewData().getX(), currentConfig.worldState.getViewData().getY());
	}
	
	public String getTitleString() {
		return(title);
	}
	
	public ToolPanel getToolPanel() {
		return(toolPanel);
	}

	/**
	 * Set the current configuration and enable the tool bar controls.
	 * 
	 * @param config
	 *            the configuration
	 */
	public void setConfiguration(final Configuration config) {
		currentConfig = config;
		toolPanel.setConfiguration(config);
		setTitle(title+" - " + Landscape.getInstance().getGlobeName() + ":"
			+ World.getInstance().getName() + ":" + currentConfig.toString());
		worldView.getScenePanel().getCanvas().requestFocusInWindow();
	}

	/**
	 * Get the world view.
	 * 
	 * @return the worldview
	 */
	public WorldView getWorldView() {
		return (worldView);
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
