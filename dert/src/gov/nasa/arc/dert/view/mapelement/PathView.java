package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ardor3d.math.Vector3;

/**
 * View for Path statistics.
 *
 */
public class PathView extends JPanelView {

	private JTextArea textArea;
	private JLabel messageLabel;
	private JCheckBox volumeCheck;
	private JRadioButton polyMethod, planeMethod;
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
	public PathView(State state) {
		super(state);
		lowerBound = new Vector3();
		upperBound = new Vector3();
		JPanel northPanel = new JPanel(new GridLayout(2, 1));
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		northPanel.setBackground(Color.white);
		topPanel.setBackground(Color.white);
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
		northPanel.add(topPanel);
		
		JPanel volPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		volPanel.setBackground(Color.white);
		volumeCheck = new JCheckBox("Volume");
		volumeCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				enableVolume();
			}
		});
		volPanel.add(volumeCheck);
		ButtonGroup volType = new ButtonGroup();
		polyMethod = new JRadioButton("Above/Below Polygon");
		polyMethod.setSelected(true);
		polyMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				enableVolume();
			}			
		});
		volType.add(polyMethod);
		volPanel.add(polyMethod);
		planeMethod = new JRadioButton("Above/Below Elevation");
		planeMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				enableVolume();
			}			
		});
		volType.add(planeMethod);
		volPanel.add(planeMethod);
		volElev = new DoubleTextField(8, Double.NaN, false, Landscape.format) {
			@Override
			public void handleChange(double value) {
				if (!isCalculating)
					doRefresh();
			}
		};
		volPanel.add(volElev);
		enableVolume();
		northPanel.add(volPanel);
		add(northPanel, BorderLayout.NORTH);
		
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(Color.white);
		textArea = new JTextArea();
		textArea.setEditable(false);
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		Path path = (Path)((MapElementState)state).getMapElement();
		double value = path.getCenterElevation();
		volElev.setValue(value);
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
	
	private void enableVolume() {
		boolean vol = volumeCheck.isSelected();
		polyMethod.setEnabled(vol);
		planeMethod.setEnabled(vol);
		volElev.setEnabled(planeMethod.isSelected() && vol);
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
						if (volumeCheck.isSelected()) {
							messageLabel.setText("Calculating volume ...");
							Thread.yield();
							str = getVolume(getVolElevation(), path);
							if (str == null) {
								doCancel();
								return;
							}
							textArea.append(str);
						}
					}
					else {
						str = "Surface Area: N/A\n";
						str += "Mean Slope: N/A\n";
						if (volumeCheck.isSelected())
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

}
