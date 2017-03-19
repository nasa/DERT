package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.VerticalPanel;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.ardor3d.math.Vector3;

/**
 * View for Path statistics.
 *
 */
public class PathView extends JPanelView {

	private JTextArea textArea;
	private JLabel messageLabel;
	private JRadioButton polyMethod, planeMethod, noMethod;
	private DoubleTextField volElev;
	private JButton refreshButton;
	protected boolean isCalculating;

	private Thread statThread;
	private Vector3 lowerBound, upperBound;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public PathView(State state, double elev, int volMethod) {
		super(state);
		lowerBound = new Vector3();
		upperBound = new Vector3();
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		refreshButton = new JButton(Icons.getImageIcon("refresh.png"));
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (isCalculating) {
					doCancel();
				}
				else {
					doRefresh();
				}
			}
		});
		topPanel.add(refreshButton);
		messageLabel = new JLabel("        ");
		messageLabel.setBackground(Color.white);
		topPanel.add(messageLabel);
		add(topPanel, BorderLayout.NORTH);
		
		ArrayList<Component> vCompList = new ArrayList<Component>();
		vCompList.add(createVolumePanel(volMethod));
		
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		textArea = new JTextArea();
		textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		textArea.setRows(12);
		textArea.setEditable(false);
		vCompList.add(textArea);
		add(new VerticalPanel(vCompList, 0), BorderLayout.CENTER);
		if (!Double.isNaN(elev))
			volElev.setValue(elev);
		else {
			Path path = (Path)((MapElementState)state).getMapElement();
			double value = path.getCenterElevation();
			volElev.setValue(value);
		}
	}
	
	private JPanel createVolumePanel(int volMethod) {
		
		ArrayList<Component> compList = new ArrayList<Component>();
		
		ButtonGroup volType = new ButtonGroup();
		noMethod = new JRadioButton("No Volume");
		noMethod.setSelected(volMethod == 0);
		volType.add(noMethod);
		compList.add(noMethod);
		polyMethod = new JRadioButton("Volume Above/Below Polygon");
		polyMethod.setSelected(volMethod == 1);
		volType.add(polyMethod);
		compList.add(polyMethod);
		planeMethod = new JRadioButton("Volume Above/Below Reference Elevation");
		planeMethod.setSelected(volMethod == 2);
		volType.add(planeMethod);
		compList.add(planeMethod);
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.add(new JLabel("Reference Elevation", SwingConstants.RIGHT));
		volElev = new DoubleTextField(8, Double.NaN, false, Landscape.format) {
			@Override
			public void handleChange(double value) {
				if (!isCalculating)
					doRefresh();
			}
		};
		panel.add(volElev);
		compList.add(panel);
		
		JPanel volPanel = new VerticalPanel(compList, 0);
		
		return(volPanel);

	}
	
	public void doRefresh() {
		if (statThread != null) {
			return;
		}
		isCalculating = true;
		refreshButton.setIcon(Icons.getImageIcon("cancel.png"));
		updateStatistics();
	}
	
	public void doCancel() {
		if (statThread != null)
			statThread.interrupt();
		statThread = null;
		isCalculating = false;
		messageLabel.setText("Cancelled");
	}
	
	public double getVolElevation() {
		if (!planeMethod.isSelected())
			return(Double.NaN);
		return(volElev.getValue());
	}

	public void updateStatistics() {
		statThread = new Thread(new Runnable() {
			@Override
			public void run() {
				messageLabel.setText("Calculating dimensions ...");
				Thread.yield();
				Path path = (Path)((MapElementState)state).getMapElement();
				textArea.setText(getDimensions(path));
				
				Vector3[] vertex = path.getPolygonVertices();
				if (vertex != null) {
					messageLabel.setText("Calculating mean elevation ...");
					Thread.yield();
					String str = getMeanElevation(vertex);
					if (str == null) {
						doCancel();
						return;
					}
					textArea.append(str);
					if (vertex.length > 3) {
						messageLabel.setText("Calculating surface area ...");
						Thread.yield();
						str = getSurfaceArea(vertex);
						if (str == null) {
							doCancel();
							return;
						}
						textArea.append(str);
						messageLabel.setText("Calculating mean slope ...");
						Thread.yield();
						str = getMeanSlope(vertex);
						if (str == null) {
							doCancel();
							return;
						}
						textArea.append(str);
						if (polyMethod.isSelected() || planeMethod.isSelected()) {
							messageLabel.setText("Calculating volume ...");
							Thread.yield();
							str = getVolume(getVolElevation(), path);
							if (str == null) {
								doCancel();
								return;
							}
							textArea.append(str);
						}
						else {
							str = "Volume Above: N/A\nVolume Below: N/A";
							textArea.append(str);
						}
					}
					else {
						str = "Surface Area: N/A\n";
						str += "Mean Slope: N/A\n";
						if (noMethod.isSelected())
							str += "Volume: N/A\n";
						textArea.append(str);
					}
				}
				isCalculating = false;
				messageLabel.setText("");
				refreshButton.setIcon(Icons.getImageIcon("refresh.png"));
				statThread = null;
			}
		});
		statThread.start();
	}
	
	private String getDimensions(Path path) {
		Vector3 centroid = new Vector3();
		Vector3 tmpVec = new Vector3();
		double[] distArea = new double[2];
		int numPts = path.getDimensions(lowerBound, upperBound, centroid, distArea);
		String str = "";
		str += "Number of Waypoints: " + numPts + "\n";
		if (numPts == 0) {
			str += "Lower Bounds: N/A\n";
			str += "Upper Bounds: N/A\n";
			str += "Centroid: N/A\n";
			str += "Total Path Distance: N/A\n";
		}
		else {
			tmpVec.set(lowerBound);
			Landscape landscape = Landscape.getInstance();
			landscape.localToWorldCoordinate(tmpVec);
			str += "Lower Bounds: " + String.format(Landscape.stringFormat, tmpVec.getX()) + "," + String.format(Landscape.stringFormat, tmpVec.getY()) + "," + String.format(Landscape.stringFormat, tmpVec.getZ()) + "\n";
			tmpVec.set(upperBound);
			landscape.localToWorldCoordinate(tmpVec);
			str += "Upper Bounds: " + String.format(Landscape.stringFormat, tmpVec.getX()) + "," + String.format(Landscape.stringFormat, tmpVec.getY()) + "," + String.format(Landscape.stringFormat, tmpVec.getZ()) + "\n";
			landscape.localToWorldCoordinate(centroid);
			str += "Centroid: " + String.format(Landscape.stringFormat, centroid.getX()) + "," + String.format(Landscape.stringFormat, centroid.getY()) + "," + String.format(Landscape.stringFormat, centroid.getZ()) + "\n";
			str += "Total Path Distance: " + String.format(Landscape.stringFormat, distArea[0]) + "\n";
		}
		if (numPts < 3) {
			str += "Planimetric Area: N/A\n";
		}
		else {
			str += "Planimetric Area: " + String.format(Landscape.stringFormat, distArea[1]) + "\n";
		}
		
		return(str);
	}
	
	private String getSurfaceArea(Vector3[] vertex) {
		double sampledVal = Landscape.getInstance().getSampledSurfaceAreaOfRegion(vertex, lowerBound, upperBound);
		if (Double.isNaN(sampledVal))
			return(null);
		return("Surface Area: "+String.format(Landscape.stringFormat, sampledVal)+"\n");
	}
	
	private String getMeanElevation(Vector3[] vertex) {
		String str = "";
		if (vertex.length < 3) {
			float mElev = 0;
			for (int i = 0; i < vertex.length; ++i) {
				mElev += vertex[i].getZf();
			}
			mElev /= vertex.length;
			str = "Mean Elevation: " + mElev + "\n";
		}
		else {
			double sampledVal = Landscape.getInstance().getSampledMeanElevationOfRegion(vertex, lowerBound, upperBound);
			if (Double.isNaN(sampledVal))
				return(null);
			str = "Mean Elevation: "+String.format(Landscape.stringFormat, sampledVal)+"\n";
		}
		return(str);
	}
	
	private String getMeanSlope(Vector3[] vertex) {
		double sampledVal = Landscape.getInstance().getSampledMeanSlopeOfRegion(vertex, lowerBound, upperBound);
		if (Double.isNaN(sampledVal))
			return(null);
		return("Mean Slope: "+String.format(Landscape.stringFormat, sampledVal)+"\n");
	}
	
	private String getVolume(double volElev, Path path) {
		String str = null;
		double[] vol = path.getVolume(volElev);
		if (vol == null)
			return(str);
		if (!Double.isNaN(volElev)) {
			str = "Volume Above "+"Elevation "+volElev+": " + String.format(Landscape.stringFormat, vol[0]) + "\n";
			str += "Volume Below "+"Elevation "+volElev+": " + String.format(Landscape.stringFormat, vol[1]) + "\n";
		}
		else {
			str = "Volume Above Polygon: " + String.format(Landscape.stringFormat, vol[0]) + "\n";
			str += "Volume Below Polygon: " + String.format(Landscape.stringFormat, vol[1]) + "\n";
		}
		return(str);
	}
		
	/**
	 * Notify user that the currently displayed statistics is old. We don't
	 * automatically update the window for performance reasons.
	 */
	public void pathDirty() {
		messageLabel.setText("Press refresh to recalculate.");
	}
	
	public int getVolumeMethod() {
		if (noMethod.isSelected())
			return(0);
		else if (polyMethod.isSelected())
			return(1);
		else
			return(2);
	}

}
