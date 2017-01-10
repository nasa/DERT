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
 * Manages input events on the SceneCanvas and redirects them to an
 * InputHandler.
 *
 */
public class InputManager {

	protected InputHandler handler;
	protected int mouseX, mouseY;
	protected int width = 1, height = 1;
	protected KeyListener keyListener;

	public InputManager(final SceneCanvas canvas, InputHandler hndler) {
		handler = hndler;

		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				mouseX = event.getX();
				mouseY = height - event.getY();
				if (event.getClickCount() == 2) {
					handler.mouseDoubleClick(mouseX, mouseY, event.getButton());
				} else {
					handler.mouseClick(mouseX, mouseY, event.getButton());
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
				handler.mousePress(mouseX, mouseY, event.getButton(), event.isControlDown(), event.isShiftDown());
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				mouseX = event.getX();
				mouseY = height - event.getY();
				handler.mouseRelease(mouseX, mouseY, event.getButton());
			}
		});

		canvas.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				handler.mouseScroll(event.getWheelRotation());
			}
		});

		canvas.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent event) {
				int x = event.getX();
				int y = height - event.getY();
				int button = event.getButton();
				button |= getButton(event.getModifiers());
				handler.mouseMove(x, y, x - mouseX, y - mouseY, button, event.isControlDown(), event.isShiftDown());
				mouseX = x;
				mouseY = y;
			}

			@Override
			public void mouseMoved(MouseEvent event) {
				int x = event.getX();
				int y = height - event.getY();
				handler.mouseMove(x, y, x - mouseX, y - mouseY, 0, event.isControlDown(), event.isShiftDown());
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
			handler.stepUp(shiftDown);
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN:
			handler.stepDown(shiftDown);
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
			handler.stepLeft(shiftDown);
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
			handler.stepRight(shiftDown);
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
	
	public void setCanvasScale(double xScale, double yScale) {
		handler.setCanvasScale(xScale, yScale);
	}
	
	public void setComponentSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
