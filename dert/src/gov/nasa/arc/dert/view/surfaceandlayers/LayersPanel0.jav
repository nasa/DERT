package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerInfo;
import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.GroupPanel;

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
	private JCheckBox showLayersCheckBox, autoAdjustCheckBox;
	private DoubleSpinner[] blendFactorSpinner;
	private JCheckBox[] lockBox;
	private JButton configureButton;

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

		// add the shading, show layers, and auto-adjust checkboxes
		JPanel topPanel = new JPanel(new FlowLayout());
		showLayersCheckBox = new JCheckBox("Show");
		showLayersCheckBox.setSelected(landscape.isLayersEnabled());
		showLayersCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				World.getInstance().getLandscape().enableLayers(showLayersCheckBox.isSelected());
			}
		});
		topPanel.add(showLayersCheckBox);
		autoAdjustCheckBox = new JCheckBox("Auto-adjust Blend Factor");
		autoAdjustCheckBox.setSelected(landscape.isAutoAdjustBlendFactor());
		autoAdjustCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				adjustBlendFactors(autoAdjustCheckBox.isSelected(), 1, -1);
				World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
			}
		});
		topPanel.add(autoAdjustCheckBox);
		configureButton = new JButton("Change");
		configureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LayerConfigurationDialog dialog = new LayerConfigurationDialog((Dialog) state.getViewData().getViewWindow());
				dialog.open();
				LayerInfo[] current = (LayerInfo[]) dialog.getResult();
				if (current != null) {
					World.getInstance().getLandscape().getLayerManager().setLayerSelection(current);
					adjustBlendFactors(autoAdjustCheckBox.isSelected(), 1, -1);
					for (int i = 0; i < current.length; ++i) {
						fillLayerSelection(i, current[i]);
					}
					World.getInstance().getLandscape().resetLayers();
				}
			}
		});
		topPanel.add(configureButton);
		add(topPanel, BorderLayout.NORTH);

		// create the layer selections
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(LayerManager.NUM_LAYERS, 1, 0, 0));

		layer = new JLabel[LayerManager.NUM_LAYERS];
		blendFactorSpinner = new DoubleSpinner[LayerManager.NUM_LAYERS];
		lockBox = new JCheckBox[LayerManager.NUM_LAYERS];

		for (int i = 0; i < LayerManager.NUM_LAYERS; ++i) {
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel(" " + (i + 1) + " "));
			lockBox[i] = new JCheckBox(unlockedIcon);
			lockBox[i].setSelectedIcon(lockedIcon);
			p.add(lockBox[i]);
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
		adjustBlendFactors(autoAdjustCheckBox.isSelected(), 1, -1);
	}

	private void fillLayerSelection(final int index, LayerInfo current) {
		if (current == null) {
			layer[index].setText("None");
			blendFactorSpinner[index].setValueNoChange(0);
			blendFactorSpinner[index].setEnabled(false);
		} else {
			String str = current.toString();
			if ((current.type == LayerType.floatfield) && (current.colorMap != null)) {
				str += ", colormap=" + current.colorMap.getName();
			}
			layer[index].setText(str);
			blendFactorSpinner[index].setValueNoChange(current.blendFactor);
			blendFactorSpinner[index].setEnabled(true);
		}
	}

	private void adjustBlendFactors(boolean adjust, double v, int index) {
		if (!adjust) {
			return;
		}
		LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
		LayerInfo[] current = layerManager.getLayerSelection();
		if (index >= 0) {
			if ((current[index].type == LayerType.none) || current[index].isOverlay) {
				return;
			}
		}
		int n = 0;
		for (int i = 0; i < current.length; ++i) {
			if ((i != index) && (current[i].type != LayerType.none) && !current[i].isOverlay) {
				if ((v < 0) || (current[i].blendFactor > 0) || (index < 0))
					n++;
			}
		}
		if (n == 0)
			return;
		double bf = v / n;
		for (int i = 0; i < current.length; ++i) {
			if ((i != index) && (current[i].type != LayerType.none) && !current[i].isOverlay) {
				double factor = bf;
				if (index >= 0) {
					factor = current[i].blendFactor - bf;
				}
				factor = Math.max(factor, 0);
				blendFactorSpinner[i].setValueNoChange(factor);
				layerManager.setLayerBlendFactor(i, (float) factor);
			}
		}
	}

	private void addBlendFactorSpinner(JPanel panel, final int index) {
		blendFactorSpinner[index] = new DoubleSpinner(0, 0, 1, 0.05, false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
				layerManager.setLayerBlendFactor(index, (float) value);
				adjustBlendFactors(autoAdjustCheckBox.isSelected(), value - lastValue, index);
				World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
				lastValue = value;
			}
		};
		panel.add(blendFactorSpinner[index]);
	}

}
