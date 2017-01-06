package gov.nasa.arc.dert.view;


/**
 * Interface for handling user navigation input events.
 *
 */
public interface InputHandler {
	
	// Set the canvas scale. This is the ratio of openGL size to component size.
	public void setCanvasScale(double xCanvasScale, double yCanvasScale);

	// Mouse was scrolled
	public void mouseScroll(int delta);

	// Mouse button was pressed
	public void mousePress(int x, int y, int mouseButton, boolean isControlled, boolean shiftDown);

	// Mouse button was released
	public void mouseRelease(int x, int y, int mouseButton);

	// Mouse was moved
	public void mouseMove(int x, int y, int dx, int dy, int mouseButton, boolean isControlled, boolean shiftDown);

	// Mouse button was clicked
	public void mouseClick(int x, int y, int mouseButton);

	// Mouse button was double-clicked
	public void mouseDoubleClick(int x, int y, int mouseButton);

	// Up arrow was pressed
	public void stepUp(boolean shiftDown);

	// Down arrow was pressed
	public void stepDown(boolean shiftDown);

	// Right arrow was pressed
	public void stepRight(boolean shiftDown);

	// Left arrow was pressed
	public void stepLeft(boolean shiftDown);

}
