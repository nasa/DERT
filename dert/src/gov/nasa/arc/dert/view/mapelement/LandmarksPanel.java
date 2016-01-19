package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FigureState;
import gov.nasa.arc.dert.state.ImageBoardState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.PlacemarkState;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides controls for setting Landmark preferences and adding Landmarks.
 *
 */
public class LandmarksPanel extends JPanel {

	/**
	 * Constructor
	 */
	public LandmarksPanel() {
		super();
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new GridLayout(3, 1));

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Add:"));

		JButton newButton = new JButton(Icons.getImageIcon("placemark.png"));
		newButton.setToolTipText("Placemark");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PlacemarkState pState = new PlacemarkState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(pState);
				newMapElement(MapElementState.Type.Placemark, pState.getMapElement());
			}
		});
		panel.add(newButton);

		newButton = new JButton(Icons.getImageIcon("figure.png"));
		newButton.setToolTipText("3D Figure");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ReadOnlyVector3 normal = World.getInstance().getMarble().getNormal();
				FigureState fState = new FigureState(position, normal);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(fState);
				newMapElement(MapElementState.Type.Figure, fState.getMapElement());
			}
		});
		panel.add(newButton);

		newButton = new JButton(Icons.getImageIcon("billboard.png"));
		newButton.setToolTipText("Image Billboard");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ImageBoardState iState = new ImageBoardState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(iState);
				newMapElement(MapElementState.Type.Billboard, iState.getMapElement());
			}
		});
		panel.add(newButton);
		topPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("All Landmarks:"));
		JButton hideAllButton = new JButton("Hide");
		hideAllButton.setToolTipText("hide all landmarks");
		hideAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setAllVisible(false);
			}
		});
		panel.add(hideAllButton);

		JButton showAllButton = new JButton("Show");
		showAllButton.setToolTipText("show all landmarks");
		showAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setAllVisible(true);
			}
		});
		panel.add(showAllButton);

		JButton saveAsCSVButton = new JButton("Save to File");
		saveAsCSVButton.setToolTipText("save all landmark coordinates to a file");
		saveAsCSVButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				World.getInstance().getLandmarks().saveToFile();
			}
		});
		panel.add(saveAsCSVButton);
		topPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Preferences"));
		topPanel.add(panel);
		add(topPanel, BorderLayout.NORTH);

		JPanel bottomPanel = new JPanel();

		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		// Placemark Preferences
		GroupPanel gPanel = new GroupPanel("Placemark");
		gPanel.setLayout(new GridLayout(4, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		JCheckBox checkBox = new JCheckBox("visible");
		checkBox.setSelected(Placemark.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Placemark.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Icon", SwingConstants.RIGHT));
		JComboBox comboBox = new JComboBox(Placemark.ICON_LABEL);
		comboBox.setSelectedIndex(Placemark.defaultTextureIndex);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Placemark.defaultTextureIndex = ((JComboBox) event.getSource()).getSelectedIndex();
			}
		});
		gPanel.add(comboBox);

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		ColorSelectionPanel colorList = new ColorSelectionPanel(Placemark.defaultColor) {
			@Override
			public void doColor(Color color) {
				Placemark.defaultColor = color;
			}
		};
		gPanel.add(colorList);

		gPanel.add(new JLabel("Size", SwingConstants.RIGHT));
		DoubleTextField sizeText = new DoubleTextField(8, Placemark.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Placemark.defaultSize = value;
			}
		};
		gPanel.add(sizeText);

		bottomPanel.add(gPanel);

		// Figure Preferences
		gPanel = new GroupPanel("3D Figure");
		gPanel.setLayout(new GridLayout(6, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Figure.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Figure.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Normal", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Figure.defaultSurfaceNormalVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Figure.defaultSurfaceNormalVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Shape", SwingConstants.RIGHT));
		comboBox = new JComboBox(ShapeType.values());
		comboBox.setSelectedItem(Figure.defaultShapeType);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Figure.defaultShapeType = (ShapeType) ((JComboBox) event.getSource()).getSelectedItem();
			}
		});
		gPanel.add(comboBox);

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(Figure.defaultColor) {
			@Override
			public void doColor(Color color) {
				Figure.defaultColor = color;
			}
		};
		gPanel.add(colorList);

		gPanel.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Figure.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Figure.defaultSize = value;
			}
		};
		gPanel.add(sizeText);

		gPanel.add(new JLabel("    ", SwingConstants.RIGHT));
		checkBox = new JCheckBox("fixed size");
		checkBox.setSelected(Figure.defaultFixedSize);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Figure.defaultFixedSize = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		bottomPanel.add(gPanel);

		// Image Billboard Preferences
		gPanel = new GroupPanel("Billboard");
		gPanel.setLayout(new GridLayout(2, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(ImageBoard.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ImageBoard.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, ImageBoard.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				ImageBoard.defaultSize = value;
			}
		};
		gPanel.add(sizeText);

		bottomPanel.add(gPanel);

		add(new JScrollPane(bottomPanel), BorderLayout.CENTER);
	}

	/**
	 * Create a new Landmark. Overridden by implementing class.
	 * 
	 * @param type
	 * @param mapElement
	 */
	public void newMapElement(MapElementState.Type type, MapElement mapElement) {
		// nothing here
	}

	/**
	 * Set all Landmarks visibility. Overridden by implementing class.
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		// nothing here
	}
}
