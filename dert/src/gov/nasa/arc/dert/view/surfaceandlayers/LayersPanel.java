package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerInfo;
import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.ui.IntSpinner;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;

import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides the layer controls for the SurfaceAndLayersView.
 *
 */
public class LayersPanel extends GroupPanel {

	// Controls
	private JLabel[] layer;
	private JCheckBox showLayersCheckBox;
	private IntSpinner[] blendFactorSpinner;
	private JCheckBox[] lockBox;
	private JButton configureButton, distributeButton;

	// View state
	private State state;
	
	// Lock icon
	private ImageIcon lockedIcon = Icons.getImageIcon("locked.png");
	private ImageIcon unlockedIcon = Icons.getImageIcon("unlocked.png");

	/**
	 * Constructor
	 * 
	 * @param s
	 */
	public LayersPanel(State s) {
		super("Layers");
		state = s;

		setLayout(new BorderLayout());

		Landscape landscape = World.getInstance().getLandscape();
		LayerInfo[] currentSelection = landscape.getLayerManager().getLayerSelection();

		JPanel topPanel = new JPanel(new FlowLayout());
		showLayersCheckBox = new JCheckBox("Show Image Layers");
		showLayersCheckBox.setToolTipText("display the visible image layers");
		showLayersCheckBox.setSelected(landscape.isLayersEnabled());
		showLayersCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				World.getInstance().getLandscape().enableLayers(showLayersCheckBox.isSelected());
			}
		});
		topPanel.add(showLayersCheckBox);
		configureButton = new JButton("Change Configuration");
		configureButton.setToolTipText("add and remove visible layers");
		configureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LayerConfigurationDialog dialog = new LayerConfigurationDialog((Dialog) state.getViewData().getViewWindow());
				dialog.open();
				LayerInfo[] current = (LayerInfo[]) dialog.getResult();
				if (current != null) {
					World.getInstance().getLandscape().getLayerManager().setLayerSelection(current);
					adjustBlendFactors(1, -1);
					for (int i = 0; i < current.length; ++i) {
						fillLayerSelection(i, current[i]);
					}
					World.getInstance().getLandscape().resetLayers();
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
				World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
			}
		});
		topPanel.add(distributeButton);
		add(topPanel, BorderLayout.NORTH);

		// create the layer selections
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(LayerManager.NUM_LAYERS, 1, 0, 0));

		layer = new JLabel[LayerManager.NUM_LAYERS];
		blendFactorSpinner = new IntSpinner[LayerManager.NUM_LAYERS];
		lockBox = new JCheckBox[LayerManager.NUM_LAYERS];

		for (int i = 0; i < LayerManager.NUM_LAYERS; ++i) {
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel(" " + i + " "));
			addLockBox(p, i);
			addBlendFactorSpinner(p, i);
			layer[i] = new JLabel();
			p.add(layer[i]);
			panel.add(p);
			fillLayerSelection(i, currentSelection[i]);
		}
		add(new JScrollPane(panel), BorderLayout.CENTER);
	}

	/**
	 * An available layer has been added or removed
	 */
	public void updateSelectedLayers() {
		LayerInfo[] currentSelection = World.getInstance().getLandscape().getLayerManager().getLayerSelection();
		for (int i = 0; i < LayerManager.NUM_LAYERS; ++i) {
			fillLayerSelection(i, currentSelection[i]);
		}
		adjustBlendFactors(1, -1);
	}

	private void fillLayerSelection(final int index, LayerInfo current) {
		String str = current.toString();
		if (current.type == LayerType.elevation)
			str = "Surface";
		else if ((current.type == LayerType.floatfield) && (current.colorMap != null)) {
			str += ", colormap=" + current.colorMap.getName();
		}
		if (current.isOverlay) {
			str += ", overlay";
		}
		layer[index].setText(str);
		lockBox[index].setSelected(!current.autoBlend);
		blendFactorSpinner[index].setValueNoChange((int)(current.blendFactor*100));
		blendFactorSpinner[index].setEnabled(current.type != LayerType.none);
	}

	private void adjustBlendFactors(int v, int index) {
		// no change in value, return
		if (v == 0)
			return;
		
		// get the layer manager and current blend factors
		LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
		LayerInfo[] current = layerManager.getLayerSelection();
		
		// single spinner
		if (index >= 0) {
			
			// not automatically blended, return
			if (!current[index].autoBlend) {
				return;
			}
			
			// get number of auto blended layers
			int n = 0;
			for (int i=0; i<current.length; ++i)
				if (current[i].autoBlend && (i != index))
					n ++;
			
			// only one unlocked spinner, return
			if (n == 0)
				return;	
			
			// get the change for each spinner
			float d = -0.01f*v/(float)n;
			
			// set the spinners and update the layer manager
			for (int i=0; i<current.length; ++i)
				if (current[i].autoBlend && (i != index)) {
					float bf = (float)current[i].blendFactor;
					bf += d;
					blendFactorSpinner[i].setValueNoChange((int)(bf*100));
					layerManager.setLayerBlendFactor(i, bf);
				}
		}
		
		// distribute throughout all unlocked spinners
		else {
			// count the number of unlocked spinners
			int n = 0;
			for (int i=0; i<current.length; ++i) {
				if (current[i].autoBlend) {
					n ++;
				}
			}
			// get the blend factor value (totals 1)
			float bf = 1.0f/n;
			
			// set the unlocked spinners
			for (int i = 0; i < current.length; ++i) {
				if (current[i].autoBlend) {
					blendFactorSpinner[i].setValueNoChange((int)(bf*100));
					layerManager.setLayerBlendFactor(i, bf);
				}
				else {
					blendFactorSpinner[i].setValueNoChange((int)(current[i].blendFactor*100));
					layerManager.setLayerBlendFactor(i, (float)current[i].blendFactor);
				}
			}
		}
	}
	
	private void addLockBox(JPanel panel, final int index) {
		lockBox[index] = new JCheckBox(unlockedIcon);
		lockBox[index].setToolTipText("unlock for automatic color contribution adjustment for this layer");
		lockBox[index].setSelectedIcon(lockedIcon);
		lockBox[index].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
				LayerInfo[] current = layerManager.getLayerSelection();
				current[index].autoBlend = !lockBox[index].isSelected();
			}
		});
		panel.add(lockBox[index]);
		
	}

	private void addBlendFactorSpinner(JPanel panel, final int index) {
		blendFactorSpinner[index] = new IntSpinner(0, 0, 100, 1, false, "###") {
			@Override
			public void stateChanged(ChangeEvent event) {
				int value = (Integer) getValue();
				double blendValue = value/100.0;
				LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
				layerManager.setLayerBlendFactor(index, (float) blendValue);
				adjustBlendFactors(value-lastValue, index);
				World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
				lastValue = value;
			}
		};
		blendFactorSpinner[index].setToolTipText("percent contribution of this layer to total color");
		panel.add(blendFactorSpinner[index]);
	}

}
