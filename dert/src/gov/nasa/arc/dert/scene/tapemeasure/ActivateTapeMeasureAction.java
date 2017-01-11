package gov.nasa.arc.dert.scene.tapemeasure;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.TextDialog;
import gov.nasa.arc.dert.view.world.WorldScenePanel;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Activate the tape measure
 *
 */
public class ActivateTapeMeasureAction extends ButtonAction {

	// Measurement information dialog
	private TextDialog dialog;

	/**
	 * Constructor
	 */
	public ActivateTapeMeasureAction() {
		super("activate tape measure", null, "tape.png", false);
	}

	@Override
	protected void run() {
		if (dialog == null) {
			dialog = new TextDialog((Frame)getTopLevelAncestor(), "DERT Tape Measure", 340, 160, false, false);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					deactivate();
				}
			});
		}
		TapeMeasure tape = World.getInstance().getTapeMeasure();
		WorldScenePanel worldScenePanel = Dert.getWorldView().getScenePanel();
		checked = !checked;
		if (checked) {
			setIcon(Icons.getImageIcon("tapecheck.png"));
			worldScenePanel.getInputHandler().setTapeMeasure(tape);
			tape.setDialog(dialog);
			dialog.open();
			Dert.getMainWindow().toFront();
		} else {
			setIcon(Icons.getImageIcon("tape.png"));
			worldScenePanel.getInputHandler().setTapeMeasure(null);
			tape.setDialog(null);
			dialog.close();
		}
	}

	/**
	 * Deactivate the tape measure
	 */
	public void deactivate() {
		checked = false;
		setIcon(Icons.getImageIcon("tape.png"));
		WorldScenePanel worldScenePanel = Dert.getWorldView().getScenePanel();
		worldScenePanel.getInputHandler().setTapeMeasure(null);
		TapeMeasure tape = World.getInstance().getTapeMeasure();
		tape.setDialog(null);
		dialog.close();
	}

}
