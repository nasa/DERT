package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.render.SceneCanvas;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Manages input events for the SceneCanvas.
 *
 */
public abstract class InputManager {

	protected int mouseX, mouseY;
	protected int width = 1, height = 1;
	protected KeyListener keyListener;

	public InputManager(final SceneCanvas canvas) {

		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				mouseX = event.getX();
				mouseY = height - event.getY();
				if (event.getClickCount() == 2) {
					mouseDoubleClick(mouseX, mouseY, event.getButton());
				} else {
					mouseClick(mouseX, mouseY, event.getButton());
				}
			}

			@Override
			public void mouseEntered(MouseEvent event) {
			}

			@Override
			public void mouseExited(MouseEvent event) {
			}

			@Override
			public void mousePressed(MouseEvent event) {
				mouseX = event.getX();
				mouseY = height - event.getY();
				mousePress(mouseX, mouseY, event.getButton(), event.isControlDown(), event.isShiftDown());
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				mouseX = event.getX();
				mouseY = height - event.getY();
				mouseRelease(mouseX, mouseY, event.getButton());
			}
		});

		canvas.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				mouseScroll(event.getWheelRotation());
			}
		});

		canvas.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent event) {
				int x = event.getX();
				int y = height - event.getY();
				int button = event.getButton();
				button |= getButton(event.getModifiers());
				mouseMove(x, y, x - mouseX, y - mouseY, button, event.isControlDown(), event.isShiftDown());
				mouseX = x;
				mouseY = y;
			}

			@Override
			public void mouseMoved(MouseEvent event) {
				int x = event.getX();
				int y = height - event.getY();
				mouseMove(x, y, x - mouseX, y - mouseY, 0, event.isControlDown(), event.isShiftDown());
				mouseX = x;
				mouseY = y;
			}
		});

		keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent event) {
				final int keyCode = event.getKeyCode();
				final boolean shiftDown = event.isShiftDown();
				handleStep(keyCode, shiftDown);
			}

			@Override
			public void keyReleased(KeyEvent event) {
				// nothing here
			}

			@Override
			public void keyTyped(KeyEvent event) {
			}
		};
		canvas.addKeyListener(keyListener);
		
		canvas.getParent().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				setComponentSize(event.getComponent().getWidth(), event.getComponent().getHeight());
			}
		});
	}

	private void handleStep(int keyCode, boolean shiftDown) {
		switch (keyCode) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_KP_UP:
			stepUp(shiftDown);
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN:
			stepDown(shiftDown);
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
			stepLeft(shiftDown);
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
			stepRight(shiftDown);
			break;
		}
	}

	public KeyListener getKeyListener() {
		return (keyListener);
	}

	protected final int getButton(int modifiers) {
		if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
			return (1);
		}
		if ((modifiers & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) {
			return (2);
		}
		if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
			return (3);
		}
		return (0);
	}
	
	public void setComponentSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public abstract void setCanvasScale(double xScale, double yScale);

	// Mouse was scrolled
	protected abstract void mouseScroll(int delta);

	// Mouse button was pressed
	protected abstract void mousePress(int x, int y, int mouseButton, boolean isControlled, boolean shiftDown);

	// Mouse button was released
	protected abstract void mouseRelease(int x, int y, int mouseButton);

	// Mouse was moved
	protected abstract void mouseMove(int x, int y, int dx, int dy, int mouseButton, boolean isControlled, boolean shiftDown);

	// Mouse button was clicked
	protected abstract void mouseClick(int x, int y, int mouseButton);

	// Mouse button was double-clicked
	protected abstract void mouseDoubleClick(int x, int y, int mouseButton);

	// Up arrow was pressed
	protected abstract void stepUp(boolean shiftDown);

	// Down arrow was pressed
	protected abstract void stepDown(boolean shiftDown);

	// Right arrow was pressed
	protected abstract void stepRight(boolean shiftDown);

	// Left arrow was pressed
	protected abstract void stepLeft(boolean shiftDown);
}
