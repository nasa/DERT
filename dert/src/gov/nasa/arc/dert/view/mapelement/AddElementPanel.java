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

import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

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
		setLayout(new GridLayout(2, 6));
		
		JButton newButton = new JButton(Icons.getImageIcon("placemark.png"));
		newButton.setToolTipText("add Placemark");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PlacemarkState pState = new PlacemarkState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(pState, null);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("figure.png"));
		newButton.setToolTipText("add 3D Figure");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ReadOnlyVector3 normal = World.getInstance().getMarble().getNormal();
				FigureState fState = new FigureState(position, normal);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(fState, null);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("billboard.png"));
		newButton.setToolTipText("add Image Billboard");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ImageBoardDialog dialog = new ImageBoardDialog((Dialog)getTopLevelAncestor(), position);
				dialog.open();
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("path.png"));
		newButton.setToolTipText("add Path");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PathState state = new PathState(position);
				Path path = (Path) ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
				Dert.getWorldView().getScenePanel().getInputHandler().setPath(path);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("plane.png"));
		newButton.setToolTipText("add Plane");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PlaneState state = new PlaneState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("cartesiangrid.png"));
		newButton.setToolTipText("add Cartesian Grid");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				GridState state = GridState.createCartesianGridState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("radialgrid.png"));
		newButton.setToolTipText("add Radial Grid");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				GridState state = GridState.createRadialGridState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("fieldcamera.png"));
		newButton.setToolTipText("add Camera");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				FieldCameraState state = new FieldCameraState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("profile.png"));
		newButton.setToolTipText("add Profile");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ProfileState state = new ProfileState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("scale.png"));
		newButton.setToolTipText("add Scale");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ScaleBarState state = new ScaleBarState(position);
				ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
			}
		});
		add(newButton);

		newButton = new JButton(Icons.getImageIcon("lineset.png"));
		newButton.setToolTipText("add FeatureSet");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FeatureSetDialog dialog = new FeatureSetDialog((Dialog)getTopLevelAncestor());
				dialog.open();
			}
		});
		add(newButton);
	}
}
