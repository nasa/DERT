package gov.nasa.arc.dert.scene.landmark;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.io.CsvWriter;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scene.Marble;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.FigureState;
import gov.nasa.arc.dert.state.ImageBoardState;
import gov.nasa.arc.dert.state.LandmarkState;
import gov.nasa.arc.dert.state.PlacemarkState;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.view.Console;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;

/**
 * Group of Landmarks
 *
 */
public class Landmarks extends GroupNode {

	// Landmark state list
	private ArrayList<LandmarkState> landmarkList;

	/**
	 * Constructor
	 * 
	 * @param landmarkList
	 */
	public Landmarks(ArrayList<LandmarkState> landmarkList) {
		super("Landmarks");
		this.landmarkList = landmarkList;
	}

	/**
	 * Initialize this object Create landmark objects
	 */
	public void initialize() {
		// Turn off any parent textures
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);

		// create landmark objects
		for (int i = 0; i < landmarkList.size(); ++i) {
			LandmarkState state = landmarkList.get(i);
			addLandmark(state, false);
		}
	}

	/**
	 * Landscape changed, update the Z coordinate of the landmarks.
	 * 
	 * @param quadTree
	 */
	public void landscapeChanged(final QuadTree quadTree) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			final Spatial child = getChild(i);
			if (child instanceof Landmark) {
				((Landmark) child).updateElevation(quadTree);
			}
		}
	}

	/**
	 * Create and add a landmark object.
	 * 
	 * @param state
	 * @param update
	 * @return
	 */
	public Landmark addLandmark(LandmarkState state, boolean update) {
		Landmark landmark = null;
		switch (state.mapElementType) {
		case Figure:
			FigureState fState = (FigureState) state;
			landmark = new Figure(fState);
			break;
		case Placemark:
			PlacemarkState pState = (PlacemarkState) state;
			landmark = new Placemark(pState);
			break;
		case Billboard:
			ImageBoardState iState = (ImageBoardState) state;
			landmark = new ImageBoard(iState);
			break;
		case Path:
		case Plane:
		case Profile:
		case FieldCamera:
		case LineSet:
		case CartesianGrid:
		case RadialGrid:
		case Waypoint:
		case Marble:
			break;
		}
		if (landmark != null) {
			Spatial spatial = (Spatial) landmark;
			attachChild(spatial);
			if (update) {
				spatial.updateGeometricState(0, true);
				landmark.update(Dert.getWorldView().getViewpointNode().getBasicCamera());
			}
		}
		return (landmark);
	}

	/**
	 * Show all the landmarks
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		Marble marble = World.getInstance().getMarble();
		marble.setVisible(visible);
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			((Landmark) getChild(i)).setVisible(visible);
		}
	}

	/**
	 * Save landmark data to a file
	 */
	public void saveToFile() {
		String filePath = FileHelper.getCSVFile();
		saveAsCsv(filePath);
	}

	private void saveAsCsv(String filename) {
		CsvWriter csvWriter = null;
		DecimalFormat formatter = new DecimalFormat(Landscape.format);
		try {
			int n = getNumberOfChildren();
			String[] column = { "Name", "X", "Y", "Z", "Annotation" };
			csvWriter = new CsvWriter(filename, column);
			csvWriter.open();
			String[] value = new String[column.length];
			for (int i = 0; i < n; ++i) {
				Landmark ldmk = (Landmark) getChild(i);
				ReadOnlyVector3 loc = ((Spatial) ldmk).getTranslation();
				value[0] = ldmk.getName();
				value[1] = formatter.format(loc.getX());
				value[2] = formatter.format(loc.getY());
				value[3] = formatter.format(loc.getZ());
				value[4] = ldmk.getState().getAnnotation();
				csvWriter.writeLine(value);
			}
			csvWriter.close();
			Console.getInstance().println(n + " records saved to " + filename);
		} catch (Exception e) {
			e.printStackTrace();
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (Exception e2) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		Placemark.saveDefaultsToProperties(properties);
		Figure.saveDefaultsToProperties(properties);
		ImageBoard.saveDefaultsToProperties(properties);
	}

}
