package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.render.SceneCanvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.Timer;

/**
 * Manages input events on the SceneCanvas and redirects them to an
 * InputHandler.
 *
 */
public class InputManager {

	protected InputHandler handler;
	protected int mouseX, mouseY;
	protected int width, height;
	protected KeyListener keyListener;
	protected Timer stepTimer;
	protected int keyCode;

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
				handler.mousePress(mouseX, mouseY, event.getButton(), event.isShiftDown());
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
				handler.mouseScroll(event.getWheelRotation(), event.isControlDown());
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
				// System.err.println("InputManager.keyPressed "+event.getKeyCode()+" "+KeyEvent.VK_ESCAPE);
				keyCode = event.getKeyCode();
				handleStep(keyCode);
				if (stepTimer == null) {
					stepTimer = new Timer(500, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent aE) {
							handleStep(keyCode);
						}
					});
					stepTimer.setInitialDelay(500);
				}
				stepTimer.start();
			}

			@Override
			public void keyReleased(KeyEvent event) {
				if (stepTimer != null) {
					stepTimer.stop();
				}
			}

			@Override
			public void keyTyped(KeyEvent event) {
				// nothing here
			}
		};
		canvas.addKeyListener(keyListener);
		resize(canvas.getWidth(), canvas.getHeight());
	}

	private void handleStep(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_KP_UP:
			handler.stepUp();
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN:
			handler.stepDown();
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
			handler.stepLeft();
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
			handler.stepRight();
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

	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
