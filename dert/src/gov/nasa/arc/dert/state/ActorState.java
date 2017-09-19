package gov.nasa.arc.dert.state;

import java.util.Map;


public class ActorState
	extends State {
	
	public ActorState(String name, ViewData viewData) {
		super(name, StateType.Actor, viewData);
	}
	
	public ActorState(Map<String,Object> map) {
		super(map);
	}
	
	public void initialize() {
		// do nothing
	}

}
