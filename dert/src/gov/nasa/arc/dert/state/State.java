package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.view.View;

import java.io.Serializable;

/**
 * Provides a serialized object for persisting the state of DERT components.
 * Components are reconstituted from these objects.
 *
 */
public class State implements Serializable {

	// Types of State object
	public static enum StateType {
		Console, World, Panel, MapElement
	}

	// State name
	public String name;

	// State type
	public StateType type;

	// Data for associated view (if any)
	protected ViewData viewData;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param viewData
	 */
	public State(String name, StateType type, ViewData viewData) {
		this.name = name;
		this.type = type;
		this.viewData = viewData;
	}

	/**
	 * Save contents (called before Configuration is closed)
	 */
	public void save() {
		if (viewData != null) {
			viewData.save();
		}
	}

	/**
	 * Dispose of resources
	 */
	public void dispose() {
		// nothing here
	}

	/**
	 * Set the view associated with this state object
	 * 
	 * @param view
	 */
	public void setView(View view) {
		if (viewData != null) {
			viewData.setView(view);
		}
	}

	@Override
	public String toString() {
		return (name);
	}

	/**
	 * Set the name for this state object
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the ViewData object.
	 * @return
	 */
	public ViewData getViewData() {
		return(viewData);
	}

}
