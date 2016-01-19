package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.LayerInfo;
import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.GBCHelper;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Dialog for configuring which layers are displayed on landscape and their
 * order.
 *
 */
public class LayerConfigurationDialog extends AbstractDialog {

	// Controls
	private JList selectedLayerList, layerList;
	private JButton up, down, add, remove;

	// Layers
	private Vector<LayerInfo> selectedLayers, layers;
	private LayerInfo currentSelected, currentLayer;
	private LayerInfo[] layerInfo;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public LayerConfigurationDialog(Dialog parent) {
		super(parent, "Layer Configuration", true, false);
	}

	@Override
	protected void build() {
		super.build();
		LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
		layerInfo = layerManager.getImageLayerInfoList();
		LayerInfo[] currentSelection = layerManager.getLayerSelection();
		layers = new Vector<LayerInfo>();
		for (int i = 0; i < layerInfo.length; ++i) {
			boolean found = false;
			for (int j = 1; j < currentSelection.length; ++j) {
				if (currentSelection[j] == null) {
					continue;
				}
				if ((layerInfo[i].type == currentSelection[j].type)
					&& layerInfo[i].name.equals(currentSelection[j].name)) {
					found = true;
					break;
				}
			}
			if (!found) {
				layers.add(layerInfo[i]);
			}
		}
		selectedLayers = new Vector<LayerInfo>();
		for (int i = 1; i < currentSelection.length; ++i) {
			selectedLayers.add(currentSelection[i]);
		}

		contentArea.setLayout(new GridBagLayout());

		// List of displayed layers
		selectedLayerList = new JList(selectedLayers);
		selectedLayerList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedLayerList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				currentSelected = (LayerInfo) selectedLayerList.getSelectedValue();
				if (currentSelected == null) {
					return;
				}
				boolean doUp = currentSelected.layerNumber > 1;
				if ((currentSelected.layerNumber == 2)
					&& !((currentSelected.type == LayerType.colorimage) || (currentSelected.type == LayerType.grayimage))) {
					doUp = false;
				}
				up.setEnabled(doUp);
				down.setEnabled(currentSelected.layerNumber < 7);
				remove.setEnabled(currentSelected.type != LayerType.none);
				boolean doAdd = true;
				if (currentLayer == null) {
					doAdd = false;
				} else if ((currentSelected.layerNumber == 1)
					&& !((currentLayer.type == LayerType.colorimage) || (currentLayer.type == LayerType.grayimage))) {
					doAdd = false;
				}
				add.setEnabled(doAdd);
			}
		});
		JScrollPane scrollPane = new JScrollPane(selectedLayerList);
		scrollPane.setPreferredSize(new Dimension(200, 128));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Visible Layers", SwingConstants.CENTER), BorderLayout.NORTH);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		contentArea.add(listPanel,
			GBCHelper.getGBC(0, 0, 4, 4, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1));

		JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
		// Button to move currently selected layer up in the list
		up = new JButton("Up");
		up.setToolTipText("move selected visible layer up");
		up.setEnabled(false);
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = selectedLayerList.getSelectedIndex();
				LayerInfo li = selectedLayers.remove(selectedIndex);
				selectedLayers.add(selectedIndex - 1, li);
				li.layerNumber = selectedIndex;
				li = selectedLayers.get(selectedIndex);
				if (li != null) {
					li.layerNumber = selectedIndex + 1;
				}
				selectedLayerList.setListData(selectedLayers);
				selectedLayerList.setSelectedIndex(selectedIndex - 1);
			}
		});
		buttonPanel.add(up);

		// Button to move a layer from the available list to the display list
		add = new JButton(Icons.getImageIcon("left.png"));
		add.setToolTipText("move selected available layer to the selected position in the visible list");
		add.setEnabled(false);
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = selectedLayerList.getSelectedIndex();
				int index = layerList.getSelectedIndex();
				LayerInfo li = layers.remove(index);
				LayerInfo sli = selectedLayers.get(selectedIndex);
				selectedLayers.set(selectedIndex, li);
				li.layerNumber = selectedIndex + 1;
				selectedLayerList.setListData(selectedLayers);
				if (sli.type != LayerType.none) {
					sli.layerNumber = -1;
					layers.add(sli);
				}
				layerList.setListData(layers);
				selectedLayerList.setSelectedIndex(selectedIndex);
			}
		});
		buttonPanel.add(add);

		// Button to move the selected layer from the display list to the
		// available list
		remove = new JButton(Icons.getImageIcon("right.png"));
		remove.setToolTipText("move selected visible layer to the available list");
		remove.setEnabled(false);
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = selectedLayerList.getSelectedIndex();
				LayerInfo li = selectedLayers.get(selectedIndex);
				if (li.type != LayerType.none) {
					li = selectedLayers.remove(selectedIndex);
					selectedLayers.add(selectedIndex, new LayerInfo("None", "none", 0, selectedIndex + 1));
					li.layerNumber = -1;
					layers.add(li);
				}
				selectedLayerList.setListData(selectedLayers);
				layerList.setListData(layers);
				selectedLayerList.setSelectedIndex(selectedIndex);
			}
		});
		buttonPanel.add(remove);

		// Button to move the selected layer down in the display list
		down = new JButton("Down");
		down.setToolTipText("move selected visible layer down");
		down.setEnabled(false);
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = selectedLayerList.getSelectedIndex();
				LayerInfo li = selectedLayers.remove(selectedIndex);
				li.layerNumber = selectedIndex + 2;
				selectedLayers.add(selectedIndex + 1, li);
				li = selectedLayers.get(selectedIndex);
				if (li != null) {
					li.layerNumber = selectedIndex + 1;
				}
				selectedLayerList.setListData(selectedLayers);
				selectedLayerList.setSelectedIndex(selectedIndex + 1);
			}
		});
		buttonPanel.add(down);
		contentArea.add(buttonPanel,
			GBCHelper.getGBC(5, 0, 1, 4, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// List of available layers
		layerList = new JList(layers);
		layerList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(layerList);
		scrollPane.setPreferredSize(new Dimension(200, 128));
		listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Available Layers", SwingConstants.CENTER), BorderLayout.NORTH);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		layerList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				currentLayer = (LayerInfo) layerList.getSelectedValue();
				if (currentLayer == null) {
					return;
				}
				boolean doAdd = true;
				if (currentSelected == null) {
					doAdd = false;
				} else if ((currentSelected.layerNumber == 1)
					&& !((currentLayer.type == LayerType.colorimage) || (currentLayer.type == LayerType.grayimage))) {
					doAdd = false;
				}
				add.setEnabled(doAdd);
			}
		});
		contentArea.add(listPanel, GBCHelper.getGBC(6, 0, 4, 4, GridBagConstraints.EAST, GridBagConstraints.BOTH, 1, 1));
	}

	private LayerInfo[] getCurrentSelection() {
		LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
		LayerInfo[] currentSelection = layerManager.getLayerSelection();
		LayerInfo[] newSelection = new LayerInfo[LayerManager.NUM_LAYERS];
		newSelection[0] = currentSelection[0];
		int n = 0;
		for (int i=0; i<selectedLayers.size(); ++i) {
			newSelection[i+1] = selectedLayers.get(i);
			if (newSelection[i+1].type != LayerType.none)
				n ++;
		}
		if (n == 0)
			newSelection[0].blendFactor = 1;
		return (newSelection);
	}

	@Override
	public boolean okPressed() {
		LayerInfo[] current = getCurrentSelection();
		result = current;
		setVisible(false);
		return (true);
	}

}
