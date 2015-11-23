package gov.nasa.arc.dert.view.graph;

import gov.nasa.arc.dert.state.ProfileState;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Presents the Profile tool data as a graph.
 *
 */
public class GraphView extends JPanelView {

	// The graph
	private Graph graph;

	// Field showing last picked point in the graph
	private JTextField lastPick;

	// Java2D drawing surface
	private Canvas canvas;

	/**
	 * Constructor
	 * 
	 * @param viewState
	 */
	public GraphView(ProfileState viewState) {
		super(viewState);
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		controlPanel.setBackground(Color.white);
		controlPanel.add(new JLabel("Distance, Elevation: "),
			GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		lastPick = new JTextField();
		lastPick.setBorder(null);
		controlPanel.add(lastPick,
			GBCHelper.getGBC(1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));
		add(controlPanel, BorderLayout.NORTH);

		canvas = new Canvas() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				doPaint((Graphics2D) g);
			}
		};
		canvas.setBackground(Color.white);
		canvas.setSize(viewState.getViewData().getWidth(), viewState.getViewData().getHeight());

		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
			}

			@Override
			public void mouseEntered(MouseEvent event) {
			}

			@Override
			public void mouseExited(MouseEvent event) {
			}

			@Override
			public void mousePressed(MouseEvent event) {
				int mouseX = event.getX();
				int mouseY = canvas.getHeight() - event.getY();
				if (event.getButton() == 1) {
					float[] point = graph.getValueAt(mouseX, mouseY);
					if (point == null) {
						lastPick.setText("");
					} else {
						lastPick.setText(StringUtil.format(point[0]) + ", " + StringUtil.format(point[1]) + " meters");
					}
				}
				canvas.requestFocus();
				canvas.repaint();
			}

			@Override
			public void mouseReleased(MouseEvent event) {
			}
		});
		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				graph.resize(event.getComponent().getWidth(), event.getComponent().getHeight());
			}
		});
		add(canvas, BorderLayout.CENTER);
		graph = viewState.getGraph();
	}

	/**
	 * Set the graph data
	 * 
	 * @param vertex
	 * @param vertexCount
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 */
	public void setData(float[] vertex, int vertexCount, float xMin, float xMax, float yMin, float yMax) {
		graph.setData(vertex, vertexCount, xMin, xMax, yMin, yMax);
		lastPick.setText("");
		canvas.repaint();
	}

	/**
	 * Set the graph color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		graph.setColor(color);
		canvas.repaint();
	}

	protected void doPaint(Graphics2D g2d) {
		graph.render(g2d);
	}

}
