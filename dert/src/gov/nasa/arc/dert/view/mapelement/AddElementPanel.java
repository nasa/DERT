package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.state.FigureState;
import gov.nasa.arc.dert.state.GridState;
import gov.nasa.arc.dert.state.PathState;
import gov.nasa.arc.dert.state.PlacemarkState;
import gov.nasa.arc.dert.state.PlaneState;
import gov.nasa.arc.dert.state.ProfileState;
import gov.nasa.arc.dert.state.ScaleBarState;
import gov.nasa.arc.dert.ui.VerticalPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides controls for setting Landmark preferences and adding Landmarks.
 *
 */
public class AddElementPanel extends JPanel {

	/**
	 * Constructor
	 */
	public AddElementPanel() {
		super();
		setLayout(new BorderLayout());
		add(new JLabel("Add a Map Element"), BorderLayout.NORTH);
		ArrayList<Component> compList = new ArrayList<Component>();

		
		compList.add(new JLabel("Landmarks"));
		JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton newButton = new JButton(Icons.getImageIcon("placemark.png"));
		newButton.setToolTipText("Placemark");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PlacemarkState pState = new PlacemarkState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(pState, null);
			}
		});
		buttonRow.add(newButton);

		newButton = new JButton(Icons.getImageIcon("figure.png"));
		newButton.setToolTipText("3D Figure");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ReadOnlyVector3 normal = World.getInstance().getMarble().getNormal();
				FigureState fState = new FigureState(position, normal);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(fState, null);
			}
		});
		buttonRow.add(newButton);

		newButton = new JButton(Icons.getImageIcon("billboard.png"));
		newButton.setToolTipText("Image Billboard");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ImageBoardDialog dialog = new ImageBoardDialog((Dialog)getTopLevelAncestor(), position);
				dialog.open();
			}
		});
		buttonRow.add(newButton);
		
		compList.add(buttonRow);

		compList.add(new JLabel("Tools"));
		buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		newButton = new JButton(Icons.getImageIcon("path.png"));
		newButton.setToolTipText("Path");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PathState state = new PathState(position);
				Path path = (Path) ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
				Dert.getWorldView().getScenePanel().getInputHandler().setPath(path);
			}
		});
		buttonRow.add(newButton);

		newButton = new JButton(Icons.getImageIcon("plane.png"));
		newButton.setToolTipText("Plane");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PlaneState state = new PlaneState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		buttonRow.add(newButton);

		newButton = new JButton(Icons.getImageIcon("cartesiangrid.png"));
		newButton.setToolTipText("Cartesian Grid");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				GridState state = GridState.createCartesianGridState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		buttonRow.add(newButton);

		newButton = new JButton(Icons.getImageIcon("radialgrid.png"));
		newButton.setToolTipText("Radial Grid");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				GridState state = GridState.createRadialGridState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		buttonRow.add(newButton);

		newButton = new JButton(Icons.getImageIcon("fieldcamera.png"));
		newButton.setToolTipText("Camera");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				FieldCameraState state = new FieldCameraState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		buttonRow.add(newButton);

		newButton = new JButton(Icons.getImageIcon("profile.png"));
		newButton.setToolTipText("Profile");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ProfileState state = new ProfileState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		buttonRow.add(newButton);

		newButton = new JButton(Icons.getImageIcon("scale.png"));
		newButton.setToolTipText("Scale");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ScaleBarState state = new ScaleBarState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		buttonRow.add(newButton);
		
		compList.add(buttonRow);

		compList.add(new JLabel("Feature Sets"));
		buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		newButton = new JButton(Icons.getImageIcon("lineset.png"));
		newButton.setToolTipText("FeatureSet");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FeatureSetDialog dialog = new FeatureSetDialog((Dialog)getTopLevelAncestor());
				dialog.open();
			}
		});
		buttonRow.add(newButton);
		
		compList.add(buttonRow);

		VerticalPanel vertPanel = new VerticalPanel(compList);
		add(new JScrollPane(vertPanel), BorderLayout.CENTER);
	}
	
	@Override
	public Insets getInsets() {
		return(new Insets(5, 5, 5, 5));
	}
}
