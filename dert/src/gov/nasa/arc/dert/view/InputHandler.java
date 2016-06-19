package gov.nasa.arc.dert.view;


/**
 * Interface for handling user navigation input events.
 *
 */
public interface InputHandler {

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
