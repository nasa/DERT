package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerInfo;
import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.GBCHelper;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
 * Dialog for configuring which layers are visible on the landscape and their
 * order.
 *
 */
public class LayerConfigurationDialog extends AbstractDialog {

	// Controls
	private JList visibleLayerList, availableLayerList;
	private JButton up, down, add, remove;

	// Layers
	private Vector<LayerInfo> visibleLayers, availableLayers;
	private LayerInfo currentSelected, currentLayer;

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
		LayerManager layerManager = Landscape.getInstance().getLayerManager();
		availableLayers = new Vector<LayerInfo>(layerManager.getAvailableLayers());
		visibleLayers = new Vector<LayerInfo>(layerManager.getVisibleLayers());

		contentArea.setLayout(new GridBagLayout());

		// List of displayed layers
		visibleLayerList = new JList(visibleLayers);
		visibleLayerList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		visibleLayerList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				currentSelected = (LayerInfo) visibleLayerList.getSelectedValue();
				if (currentSelected == null) {
					return;
				}
				boolean doUp = currentSelected.layerNumber > 0;
				if ((currentSelected.layerNumber == 1)
					&& !((currentSelected.type == LayerType.colorimage) || (currentSelected.type == LayerType.grayimage))) {
					doUp = false;
				}
				up.setEnabled(doUp);
				down.setEnabled(currentSelected.layerNumber < 7);
				remove.setEnabled(currentSelected.type != LayerType.none);
				boolean doAdd = true;
				if (currentLayer == null) {
					doAdd = false;
				} else if ((currentSelected.layerNumber == 0)
					&& !((currentLayer.type == LayerType.colorimage) || (currentLayer.type == LayerType.grayimage))) {
					doAdd = false;
				}
				add.setEnabled(doAdd);
			}
		});
		JScrollPane scrollPane = new JScrollPane(visibleLayerList);
		scrollPane.setPreferredSize(new Dimension(200, 128));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Visible Layers", SwingConstants.CENTER), BorderLayout.NORTH);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		contentArea.add(listPanel,
			GBCHelper.getGBC(0, 0, 4, 4, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1));

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		// Button to move currently selected layer up in the list
		up = new JButton("Up");
		up.setToolTipText("move selected visible layer up");
		up.setEnabled(false);
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = visibleLayerList.getSelectedIndex();
				LayerInfo li = visibleLayers.remove(selectedIndex);
				visibleLayers.add(selectedIndex - 1, li);
				li.layerNumber = selectedIndex;
				li = visibleLayers.get(selectedIndex);
				if (li != null) {
					li.layerNumber = selectedIndex;
				}
				visibleLayerList.setListData(visibleLayers);
				visibleLayerList.setSelectedIndex(selectedIndex - 1);
			}
		});
		buttonPanel.add(up, GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// Button to move a layer from the available list to the selected list
		add = new JButton(Icons.getImageIcon("prev_16.png"));
		add.setToolTipText("move selected available layer to the selected position in the visible list");
		add.setEnabled(false);
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// get the index of the slot in the visible layers
				int selectedIndex = visibleLayerList.getSelectedIndex();
				// get the index of the layer selected from the available layers
				int index = availableLayerList.getSelectedIndex();
				// remove the selected layer from the available layers
				LayerInfo li = availableLayers.remove(index);
				// get the layer from the slot selected in the visible layers
				LayerInfo sli = visibleLayers.get(selectedIndex);
				// put the newly selected layer into the slot
				visibleLayers.set(selectedIndex, li);
				// set the layer number (0=first layer, 1=second layer ...)
				li.layerNumber = selectedIndex;
				// put the updated visible list in the list display
				visibleLayerList.setListData(visibleLayers);
				// put the old layer back in the available layers list
				if (sli.type != LayerType.none) {
					sli.layerNumber = -1;
					availableLayers.add(sli);
				}
				// update the available list in the list display
				availableLayerList.setListData(availableLayers);
				visibleLayerList.setSelectedIndex(selectedIndex);
			}
		});
		buttonPanel.add(add, GBCHelper.getGBC(0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// Button to move the selected layer from the display list to the
		// available list
		remove = new JButton(Icons.getImageIcon("next_16.png"));
		remove.setToolTipText("move selected visible layer to the available list");
		remove.setEnabled(false);
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = visibleLayerList.getSelectedIndex();
				LayerInfo li = visibleLayers.get(selectedIndex);
				if (li.type != LayerType.none) {
					visibleLayers.set(selectedIndex, new LayerInfo("None", "none", selectedIndex));
					li.layerNumber = -1;
					availableLayers.add(li);
				}
				visibleLayerList.setListData(visibleLayers);
				availableLayerList.setListData(availableLayers);
				visibleLayerList.setSelectedIndex(selectedIndex);
			}
		});
		buttonPanel.add(remove, GBCHelper.getGBC(0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// Button to move the selected layer down in the display list
		down = new JButton("Down");
		down.setToolTipText("move selected visible layer down");
		down.setEnabled(false);
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = visibleLayerList.getSelectedIndex();
				LayerInfo li = visibleLayers.remove(selectedIndex);
				li.layerNumber = selectedIndex + 1;
				visibleLayers.add(selectedIndex, li);
				li = visibleLayers.get(selectedIndex);
				if (li != null) {
					li.layerNumber = selectedIndex;
				}
				visibleLayerList.setListData(visibleLayers);
				visibleLayerList.setSelectedIndex(selectedIndex + 1);
			}
		});
		buttonPanel.add(down, GBCHelper.getGBC(0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));
		contentArea.add(buttonPanel,
			GBCHelper.getGBC(5, 0, 1, 4, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// List of available layers
		availableLayerList = new JList(availableLayers);
		availableLayerList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(availableLayerList);
		scrollPane.setPreferredSize(new Dimension(200, 128));
		listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Available Layers", SwingConstants.CENTER), BorderLayout.NORTH);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		availableLayerList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				currentLayer = (LayerInfo) availableLayerList.getSelectedValue();
				if (currentLayer == null) {
					return;
				}
				boolean doAdd = true;
				if (currentSelected == null) {
					doAdd = false;
				} else if ((currentSelected.layerNumber == 0)
					&& !((currentLayer.type == LayerType.colorimage) || (currentLayer.type == LayerType.grayimage))) {
					doAdd = false;
				}
				add.setEnabled(doAdd);
			}
		});
		contentArea.add(listPanel, GBCHelper.getGBC(6, 0, 4, 4, GridBagConstraints.EAST, GridBagConstraints.BOTH, 1, 1));
	}

	@Override
	public boolean okPressed() {
		LayerManager layerManager = Landscape.getInstance().getLayerManager();
		layerManager.setLayerSelection(visibleLayers, availableLayers);
		result = new Boolean(true);
		return (true);
	}

}
