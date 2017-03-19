package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerInfo;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides the layer controls for the SurfaceAndLayersView.
 *
 */
public class LayersPanel extends GroupPanel {

	// Controls
	private JCheckBox showLayersCheckBox;
	private JButton configureButton, distributeButton;
	private LayerPanel[] layer;

	// View state
	private State state;

	/**
	 * Constructor
	 * 
	 * @param s
	 */
	public LayersPanel(State s) {
		super("Layers");
		state = s;

		setLayout(new BorderLayout());

		Landscape landscape = Landscape.getInstance();
		Vector<LayerInfo> visibleLayers = landscape.getLayerManager().getVisibleLayers();

		JPanel topPanel = new JPanel(new FlowLayout());
		showLayersCheckBox = new JCheckBox("Show Layers");
		showLayersCheckBox.setToolTipText("display the visible layers");
		showLayersCheckBox.setSelected(landscape.isLayersEnabled());
		showLayersCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Landscape.getInstance().enableLayers(showLayersCheckBox.isSelected());
			}
		});
		topPanel.add(showLayersCheckBox);
		configureButton = new JButton("Configure");
		configureButton.setToolTipText("add and remove visible layers");
		configureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LayerConfigurationDialog dialog = new LayerConfigurationDialog((Dialog) state.getViewData().getViewWindow());
				if (dialog.open()) {
					updateVisibleLayers();
					Landscape.getInstance().resetLayers();
				}
			}
		});
		topPanel.add(configureButton);
		distributeButton = new JButton("Distribute");
		distributeButton.setToolTipText("distribute total color contribution equally among unlocked layers");
		distributeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				adjustBlendFactors(1, -1);
				Landscape.getInstance().markDirty(DirtyType.RenderState);
			}
		});
		topPanel.add(distributeButton);
		add(topPanel, BorderLayout.NORTH);

		// create the layer selections
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(LayerManager.NUM_LAYERS, 1, 0, 0));

		layer = new LayerPanel[LayerManager.NUM_LAYERS];

		for (int i = 0; i < LayerManager.NUM_LAYERS; ++i) {
			layer[i] = new LayerPanel(this, i);
			layer[i].set(visibleLayers.get(i));
			panel.add(layer[i]);
		}
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * An available layer has been added or removed
	 */
	public void updateVisibleLayers() {
		Vector<LayerInfo> visibleLayers = Landscape.getInstance().getLayerManager().getVisibleLayers();
		for (int i = 0; i < visibleLayers.size(); ++i) {
			layer[i].set(visibleLayers.get(i));
		}
		adjustBlendFactors(1, -1);
	}

	public void adjustBlendFactors(int v, int index) {
		// no change in value, return
		if (v == 0)
			return;
		
		// get the layer manager and current blend factors
		LayerManager layerManager = Landscape.getInstance().getLayerManager();
		Vector<LayerInfo> visibleLayers = layerManager.getVisibleLayers();
		
		// single spinner
		if (index >= 0) {
			
			// not automatically blended, return
			if (!visibleLayers.get(index).autoblend) {
				return;
			}
			
			// get number of auto blended layers
			int n = 0;
			for (int i=1; i<visibleLayers.size(); ++i)
				if (visibleLayers.get(i).autoblend && (i != index))
					n ++;
			
			// only one unlocked spinner, return
			if (n == 0)
				return;	
			
			// get the change for each spinner
			float d = -0.01f*v/(float)n;
			
			// set the spinners and update the layer manager
			for (int i=1; i<visibleLayers.size(); ++i)
				if (visibleLayers.get(i).autoblend && (i != index)) {
					float bf = (float)visibleLayers.get(i).opacity;
					bf += d;
					bf = Math.max(0, bf);
					bf = Math.min(1, bf);
					layer[i].setOpacity((int)(bf*100));
					layerManager.setLayerBlendFactor(i, bf);
				}
		}
		
		// distribute throughout all unlocked spinners
		else {
			// set the first layer (never unlocked)
			layer[0].setOpacity((int)(visibleLayers.get(0).opacity*100));
			layerManager.setLayerBlendFactor(0, (float)visibleLayers.get(0).opacity);
			
			int n = 1;
			for (int i = 1; i < visibleLayers.size(); ++i) {
				// set the unlocked spinners
				if (visibleLayers.get(i).autoblend) {
					n ++;
					float bf = 1.0f/n;
					layer[i].setOpacity((int)(bf*100));
					layerManager.setLayerBlendFactor(i, bf);
				}
				// set the locked spinners
				else {
					layer[i].setOpacity((int)(visibleLayers.get(i).opacity*100));
					layerManager.setLayerBlendFactor(i, (float)visibleLayers.get(i).opacity);
				}
			}
		}
	}

}
