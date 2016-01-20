package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.LineSet;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.LineSetState;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Provides controls for setting options for LineSets.
 *
 */
public class LineSetPanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private JTextField fileText;
	private JButton browseButton;
	private Color currentColor = LineSet.defaultColor;

	// LineSet
	private LineSet lineSet;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public LineSetPanel(MapElementsPanel parent) {
		super(parent);
		icon = LineSet.icon;
		type = "LineSet";
		build(true, false, false);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("File"), BorderLayout.WEST);
		fileText = new JTextField();
		fileText.setToolTipText("path to GeoJSON file");
		fileText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				loadFile();
			}
		});
		panel.add(fileText, BorderLayout.CENTER);
		panel.setMaximumSize(new Dimension(1000, -1));
		browseButton = new JButton("Browse");
		browseButton.setToolTipText("browse to GeoJSON file");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setFile();
			}
		});
		panel.add(browseButton, BorderLayout.EAST);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(LineSet.defaultColor) {
			@Override
			public void doColor(Color color) {
				currentColor = color;
				if (lineSet != null) {
					lineSet.setColor(color);
				}
			}
		};
		panel.add(colorList);
		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		lineSet = (LineSet) mapElement;
		if (lineSet != null) {
			nameLabel.setText(lineSet.getName());
			colorList.setColor(lineSet.getColor());
			fileText.setText(lineSet.getFilePath());
			fileText.setEnabled(false);
			browseButton.setEnabled(false);
			noteText.setText(lineSet.getState().getAnnotation());
			currentColor = lineSet.getColor();
		} else {
			nameLabel.setText("");
			colorList.setColor(LineSet.defaultColor);
			fileText.setEnabled(true);
			browseButton.setEnabled(true);
			noteText.setText("");
		}
	}

	protected void setFile() {
		String path = FileHelper.getFilePathForOpen("Select LineSet File", "GeoJSON Files", "json");
		if (path != null) {
			File file = new File(path);
			fileText.setText(file.getAbsolutePath());
			loadFile();
		}
	}

	private void loadFile() {

		// get the file path
		String filePath = fileText.getText().trim();
		if (filePath.isEmpty()) {
			return;
		}

		String label = StringUtil.getLabelFromFilePath(filePath);
		nameLabel.setText(label);
		LineSetState lsState = new LineSetState(label, filePath, currentColor, noteText.getText());
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(lsState);
	}

}
