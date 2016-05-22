package gov.nasa.arc.dert.view.contour;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.render.SceneCanvasPanel;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.state.PlaneState;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.ColorBar;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.InputManager;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.util.ReadOnlyTimer;

/**
 * SceneCanvasPanel for ContourView.
 *
 */
public class ContourScenePanel extends SceneCanvasPanel {

	// The scene
	private ContourScene contourScene;

	// The plane
	private Plane plane;

	// Separate thread for updating the elevation difference map
	private Thread updateThread;

	// Message to notify use that new update is in progress
	private JLabel messageLabel;

	// Current cursor location
	private CoordTextField coordTextField;

	// Handle input
	private ContourInputHandler inputHandler;
	private InputManager inputManager;

	// Display color map
	private ColorBar colorBar;

	// Flag to recalculate diff map
	private boolean drawDiff;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ContourScenePanel(PlaneState state) {
		super(state.getViewData().getWidth(), state.getViewData().getHeight(), new ContourScene(state), false);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		plane = (Plane) state.getMapElement();
		contourScene = (ContourScene) scene;

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		coordTextField = new CoordTextField(20, "current cursor location in landscape", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 coord) {
				// nothing here
			}
		};
		coordTextField.setEditable(false);
		CoordAction.listenerList.add(coordTextField);
		topPanel.add(coordTextField);
		JButton refreshButton = new JButton(Icons.getImageIcon("refresh.png"));
		refreshButton.setToolTipText("refresh");
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				drawDiff = true;
			}
		});
		topPanel.add(refreshButton);
		messageLabel = new JLabel("        ");
		topPanel.add(messageLabel);
		add(topPanel, BorderLayout.NORTH);
		colorBar = new ColorBar(contourScene.getColorMap(), true);
		add(colorBar, BorderLayout.WEST);
		setState(state);
	}

	@Override
	public void setState(State state) {
		super.setState(state);
		canvasRenderer.setCamera(contourScene.getCamera());
		inputHandler = new ContourInputHandler(contourScene.getCamera(), this);
		inputManager = new InputManager(canvas, inputHandler);
		Dimension size = canvas.getSize();
		resizeCanvas(size.width, size.height);
	}

	@Override
	public void resizeCanvas(int width, int height) {
		if (inputManager != null) {
			inputManager.resize(width, height);
		}
		contourScene.resize(width, height);
	}

	@Override
	public void update(ReadOnlyTimer timer) {
		if (drawDiff) {
			messageLabel.setText("Calculating . . .");
			drawDiff = false;
			if (updateThread != null) {
				return;
			}
			updateThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Thread.yield();
					double strike = plane.getStrike();
					double dip = plane.getDip();
					contourScene.updateContour();
					updateThread = null;
					String str = "Strike: ";
					if (Plane.strikeAsCompassBearing) {
						str += StringUtil.azimuthToCompassBearing(strike);
					} else {
						str += StringUtil.format(strike);
					}
					str += StringUtil.DEGREE;
					str += "   Dip:" + StringUtil.format(dip) + StringUtil.DEGREE;
					messageLabel.setText(str);
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							colorBar.buildPalette(contourScene.getColorMap());
						}
					});
				}
			});
			updateThread.start();
		}
	}

	/**
	 * Update the coord text field.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector3 getCoords(int x, int y) {
		Vector3 coord = contourScene.getCoords(x, y);
		if (coord == null) {
			return (null);
		}
		double z = coord.getZ();
		coord.setZ(z);
		coordTextField.setLocalValue(coord);
		return (coord);
	}

	/**
	 * User clicked on contour map. Move marble to that point in landscape.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector3 getPickCoords(int x, int y) {
		Vector3 coord = contourScene.getPickCoords(x, y);
		if (coord == null) {
			return (null);
		}
		World.getInstance().getMarble().update(coord, null, null);
		return (coord);
	}

	/**
	 * The viewpoint changed, redraw the scene.
	 */
	public void viewpointChanged() {
		contourScene.needsRender.set(true);
	}

	/**
	 * Recalculate the diff map
	 * 
	 * @param draw
	 */
	public void setDraw(boolean draw) {
		drawDiff = draw;
	}

	/**
	 * Get the color map
	 * 
	 * @return
	 */
	public ColorMap getColorMap() {
		return (contourScene.getColorMap());
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (coordTextField != null)
			CoordAction.listenerList.remove(coordTextField);
	}

}
