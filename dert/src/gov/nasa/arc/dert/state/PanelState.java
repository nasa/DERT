package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;

import java.awt.Window;
import java.util.Map;

/**
 * Base class for state objects based on AWT or Swing panels.
 *
 */
public abstract class PanelState extends State {

	public String title;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param title
	 * @param viewData
	 */
	public PanelState(String name, String title, ViewData viewData) {
		super(name, StateType.Panel, viewData);
		this.title = title;
	}
	
	/**
	 * Constructor from hash map.
	 */
	public PanelState(Map<String,Object> map) {
		super(map);
		title = StateUtil.getString(map, "PanelTitle", "");
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof PanelState)) 
			return(false);
		PanelState that = (PanelState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (!this.title.equals(that.title)) 
			return(false);
		return(true);
	}

	/**
	 * Save contents
	 */
	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		map.put("PanelTitle", title);
		return(map);
	}

	/**
	 * Open the view
	 * 
	 * @return
	 */
	@Override
	public View open(boolean doIt) {
		if (doIt)
			viewData.setVisible(true);
		if (!viewData.isVisible())
			return(null);
		
		Window window = viewData.getViewWindow();
		View view = viewData.getView();
		if (window != null) {
			window.setVisible(true);
			return (view);
		}
//		int xOffset = 20;
//		int yOffset = 20;
		if (view == null) {
			view = createView();
			setView(view);
		}
		window = viewData.createWindow(Dert.getMainWindow(), title);
//		if (window == null) {
//			window = viewData.createWindow(Dert.getMainWindow(), title, xOffset, yOffset);
//		} else {
//			viewData.setViewWindow(window, true, xOffset, yOffset);
//			viewData.setViewWindow(window, true);
//		}
		window.setVisible(true);
		return (view);
	}
	
	protected abstract View createView();

}
