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

import javax.swing.JDialog;

/**
 * Provides data structure for the persistence of metadata concerning the layout
 * of a View.
 *
 */
public class ViewData /*implements Serializable*/ {
	
	public static int DEFAULT_WINDOW_WIDTH = 800;
	public static int DEFAULT_WINDOW_HEIGHT = 600;

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
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.onTop = onTop;
		if ((windowWidth < 0) || (windowHeight < 0)) {
			packIt = true;
		}
	}
	
	/**
	 * Copy constructor
	 * @param that
	 */
	public ViewData(ViewData that) {
		this.windowX = that.windowX;
		this.windowY = that.windowY;
		this.windowWidth = that.windowWidth;
		this.windowHeight = that.windowHeight;
		this.onTop = that.onTop;
		this.packIt = that.packIt;
		this.visible = that.visible;
	}
	
	public boolean isEqualTo(ViewData that) {
		if (this.windowX != that.windowX) 
			return(false);
		if (this.windowY != that.windowY) 
			return(false);
		if (this.windowWidth != that.windowWidth) 
			return(false);
		if (this.windowHeight != that.windowHeight) 
			return(false);
		if (this.onTop != that.onTop) 
			return(false);
		if (this.packIt != that.packIt) 
			return(false);
		if (this.visible != that.visible) 
			return(false);
		return(true);
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
	 * Get the window X position
	 * 
	 * @return
	 */
	public int getX() {
		return (windowX);
	}

	/**
	 * Get the window Y position
	 * 
	 * @return
	 */
	public int getY() {
		return (windowY);
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
	
	public void save() {
		Window window = getViewWindow();
		if (window == null)
			return;
		windowX = window.getX();
		windowY = window.getY();
		windowWidth = window.getWidth();
		windowHeight = window.getHeight();
		visible = window.isVisible();
	}
		
	public int[] toArray() {
		int[] array = new int[7];
		array[0] = windowX;
		array[1] = windowY;
		array[2] = windowWidth;
		array[3] = windowHeight;
		array[4] = (visible ? 1 : 0);
		array[5] = (onTop ? 1 : 0);
		array[6] = (packIt ? 1 : 0);
		return(array);
	}

	public static ViewData fromArray(int[] array) {
		if (array == null)
			return(null);
		ViewData viewData = new ViewData(array[0], array[1], array[2], array[3], (array[5] == 1));
		viewData.setVisible((array[4] == 1));
		viewData.packIt = (array[6] == 1);
		return(viewData);
	}
	
	@Override
	public String toString() {
		String str = "ViewData["+windowX+","+windowY+","+windowWidth+","+windowHeight+","+visible+","+onTop+","+packIt+"]";
		return(str);
	}
}
