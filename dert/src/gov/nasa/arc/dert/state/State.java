package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;

import java.io.Serializable;
import java.util.HashMap;

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
	
	public State(HashMap<String,Object> map) {
		name = StateUtil.getString(map, "Name", null);
		if (name == null)
			throw new NullPointerException("State has no name.");
		String str = StateUtil.getString(map, "Type", null);
		if (str == null)
			throw new NullPointerException("State has no type.");
		type = StateType.valueOf(str);
		viewData = ViewData.fromArray((int[])map.get("ViewData"));
	}

	/**
	 * Save contents (called before Configuration is closed)
	 */
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("Name", name);
		map.put("Type", type.toString());
		if (viewData != null) {
			viewData.save();
			map.put("ViewData", viewData.toArray());
		}
		return(map);
	}
	
	public boolean isEqualTo(State that) {
		if (!this.name.equals(that.name)) 
			return(false);
		if (this.type != that.type)
			return(false);
		// the same viewdata objects or both are null
		if (this.viewData == that.viewData)
			return(true);
		// this view data is null but the other isn't
		if (this.viewData == null)
			return(false);
		// the other view data is null but this one isn't
		if (that.viewData == null)
			return(false);
		// see if the view datas are equal
		return(this.viewData.isEqualTo(that.viewData));
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
		return (" Name="+name+" Type="+type+" ViewData="+viewData);
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
	
	public void setViewData(ViewData viewData) {
		this.viewData = viewData;
	}

}
