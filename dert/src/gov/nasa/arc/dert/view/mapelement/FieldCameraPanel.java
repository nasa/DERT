package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.view.fieldcamera.FieldCameraView;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provides controls for setting options for field cameras.
 *
 */
public class FieldCameraPanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private JCheckBox fovCheckBox, lineCheckBox;
	private JComboBox defCombo;
	private JButton openButton;

	// FieldCamera
	private FieldCamera fieldCamera;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public FieldCameraPanel(MapElementsPanel parent) {
		super(parent);
		icon = FieldCamera.icon;
		type = "Camera";
		build(true, true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		String[] names = FieldCameraInfoManager.getInstance().getFieldCameraNames();
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Definition"));
		defCombo = new JComboBox(names);
		defCombo.setToolTipText("select camera definition file");
		defCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String fieldCameraDef = (String) defCombo.getSelectedItem();
				if (fieldCamera.setFieldCameraDefinition(fieldCameraDef)) {
					FieldCameraView view = (FieldCameraView)fieldCamera.getState().getViewData().getView();
					if (view != null)
						view.setRange(fieldCamera.getFieldCameraInfo());					
				}
			}
		});
		panel.add(defCombo);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(FieldCamera.defaultColor) {
			@Override
			public void doColor(Color color) {
				fieldCamera.setColor(color);
			}
		};
		panel.add(colorList);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		fovCheckBox = new JCheckBox("Show FOV");
		fovCheckBox.setToolTipText("display the field of view frustum");
		fovCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fieldCamera.setFovVisible(fovCheckBox.isSelected());
			}
		});
		panel.add(fovCheckBox);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		lineCheckBox = new JCheckBox("Show Line");
		lineCheckBox.setToolTipText("display a line to the lookat point");
		lineCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fieldCamera.setLookAtLineVisible(lineCheckBox.isSelected());
			}
		});
		panel.add(lineCheckBox);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		openButton = new JButton("Open View");
		openButton.setToolTipText("display this camera's view of the landscape in a separate window");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fieldCamera.getState().getViewData().setVisible(true);
				fieldCamera.getState().open();
			}
		});
		openButton.setEnabled(false);
		panel.add(openButton);
		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		fieldCamera = (FieldCamera) mapElement;
		setLocation(locationText, elevLabel, fieldCamera.getTranslation());
		defCombo.setSelectedItem(fieldCamera.getFieldCameraDefinition());
		pinnedCheckBox.setSelected(fieldCamera.isPinned());
		nameLabel.setText(fieldCamera.getName());
		colorList.setColor(fieldCamera.getColor());
		noteText.setText(fieldCamera.getState().getAnnotation());
		labelCheckBox.setSelected(fieldCamera.isLabelVisible());
		fovCheckBox.setSelected(fieldCamera.isFovVisible());
		lineCheckBox.setSelected(fieldCamera.isLookAtLineVisible());
		openButton.setEnabled(true);
	}

}
