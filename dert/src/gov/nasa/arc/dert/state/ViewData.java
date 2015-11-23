package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.view.View;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.JDialog;

/**
 * Provides data structure for the persistence of metadata concerning the layout
 * of a View.
 *
 */
public class ViewData implements Serializable {

	// Window dimensions
	private int windowX = -1, windowY = -1, windowWidth = 900, windowHeight = 600;

	// Indicates visibility
	private boolean visible;

	// Indicates the view is always on top
	private boolean onTop;

	// Indicates the view should be packed when created.
	private boolean packIt;

	// The associated view
	protected transient Window viewWindow;
	protected transient View view;

	/**
	 * Constructor
	 */
	public ViewData() {
	}

	/**
	 * Constructor
	 * 
	 * @param windowX
	 * @param windowY
	 * @param onTop
	 */
	public ViewData(int windowX, int windowY, boolean onTop) {
		this.windowX = windowX;
		this.windowY = windowY;
		this.onTop = onTop;
		this.packIt = true;
	}

	/**
	 * Constructor
	 * 
	 * @param windowX
	 * @param windowY
	 * @param windowWidth
	 * @param windowHeight
	 * @param onTop
	 */
	public ViewData(int windowX, int windowY, int windowWidth, int windowHeight, boolean onTop) {
		this.windowX = windowX;
		this.windowY = windowY;
		if (windowWidth < 0) {
			windowWidth = 800;
		}
		this.windowWidth = windowWidth;
		if (windowHeight < 0) {
			windowHeight = 600;
		}
		this.windowHeight = windowHeight;
		this.onTop = onTop;
		this.packIt = false;
	}

	/**
	 * Dispose of view resources
	 */
	public void dispose() {
		view = null;
		viewWindow = null;
	}

	/**
	 * Get visibility
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return (visible);
	}

	/**
	 * Set visibility
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Determine if this view should be on top of the others
	 * 
	 * @return
	 */
	public boolean isOnTop() {
		return (onTop);
	}

	/**
	 * Set the window for the view
	 * 
	 * @param window
	 * @param setBounds
	 * @param defaultX
	 * @param defaultY
	 */
	public void setViewWindow(Window window, boolean setBounds, int defaultX, int defaultY) {
		this.viewWindow = window;
		if ((viewWindow != null) && setBounds) {
			if (windowX < 0) {
				windowX = defaultX;
			}
			if (windowY < 0) {
				windowY = defaultY;
			}
			window.setLocation(windowX, windowY);
			if (packIt) {
				window.pack();
				windowWidth = window.getWidth();
				windowHeight = window.getHeight();
			} else {
				window.setSize(windowWidth, windowHeight);
			}
		}
	}

	/**
	 * Get the window for the view
	 * 
	 * @return
	 */
	public Window getViewWindow() {
		return (viewWindow);
	}

	/**
	 * Set the view
	 * 
	 * @param view
	 */
	public void setView(View view) {
		this.view = view;
	}

	/**
	 * Get the view
	 * 
	 * @return
	 */
	public View getView() {
		return (view);
	}

	/**
	 * Get the window width
	 * 
	 * @return
	 */
	public int getWidth() {
		return (windowWidth);
	}

	/**
	 * Get the window height
	 * 
	 * @return
	 */
	public int getHeight() {
		return (windowHeight);
	}

	/**
	 * Save the view metadata
	 */
	public void save() {
		if (viewWindow != null) {
			windowX = viewWindow.getX();
			windowY = viewWindow.getY();
			windowWidth = viewWindow.getWidth();
			windowHeight = viewWindow.getHeight();
			visible = viewWindow.isVisible();
		}
	}

	/**
	 * Create a window for the view
	 * 
	 * @param parent
	 * @param title
	 * @param xOffset
	 * @param yOffset
	 * @return
	 */
	public Window createWindow(Frame parent, String title, int xOffset, int yOffset) {
		JDialog dialog = null;
		if (isOnTop()) {
			dialog = new JDialog(parent);
		} else {
			dialog = new JDialog();
		}
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				view.close();
			}
		});
		dialog.setTitle(title);
		dialog.setIconImage(Icons.getImage("dert_24.png"));
		dialog.setLayout(new BorderLayout());
		dialog.getContentPane().add((Component) view, BorderLayout.CENTER);
		Point point = parent.getLocation();
		xOffset += point.x;
		yOffset += point.y;
		setViewWindow(dialog, true, xOffset, yOffset);
		return (dialog);
	}

	/**
	 * Close the view
	 */
	public void close() {
		Window window = getViewWindow();
		setView(null);
		setViewWindow(null, false, 0, 0);
		if (window == null) {
			return;
		}
		window.setVisible(false);
		window.dispose();
	}
}
