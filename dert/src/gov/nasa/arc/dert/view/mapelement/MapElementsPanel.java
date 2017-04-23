package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.UndoHandler;
import gov.nasa.arc.dert.action.mapelement.NameDialog;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scene.featureset.FeatureSets;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.scene.landmark.Landmark;
import gov.nasa.arc.dert.scene.landmark.Landmarks;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.scene.tool.ScaleBar;
import gov.nasa.arc.dert.scene.tool.Tool;
import gov.nasa.arc.dert.scene.tool.Tools;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementsState;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.view.world.DeleteEditMulti;
import gov.nasa.arc.dert.view.world.GroundEdit;
import gov.nasa.arc.dert.view.world.HideEdit;
import gov.nasa.arc.dert.view.world.HideEditMulti;
import gov.nasa.arc.dert.view.world.SeekEdit;
import gov.nasa.arc.dert.view.world.ShowEdit;
import gov.nasa.arc.dert.view.world.ShowEditMulti;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyEventListener;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.event.SceneGraphManager;

/**
 * Provides the content for the MapElementsView.
 *
 */
public class MapElementsPanel extends JPanel implements DirtyEventListener {

	// Tree displaying list of map elements
	private DefaultTreeModel treeModel;
	private JTree tree;
	private DefaultMutableTreeNode rootNode, landmarksNode, toolsNode, featureSetsNode;

	// Buttons for actions applying to all map elements
	private JButton showButton, deleteButton, seekButton, renameButton, editButton;
	private JButton findButton, hideButton, labelButton, unlabelButton, lockButton, unlockButton;
	private JButton csvButton, groundButton, openButton, noteButton;
	private ColorSelectionPanel colorList;

	// Controls
	private AddElementPanel addElementPanel;

	// Map elements
	private MapElement[] currentMapElements;
	private MapElementsState state;

	// Search feature
	private JTextField searchText;
	private TreePath[] searchResult;
	private int searchIndex = -1;
	private String lastSearch = "";

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public MapElementsPanel(MapElementsState state) {
		super();
		this.state = state;

		setLayout(new BorderLayout());

		// Map Elements list tree
		loadTreeModel();
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setCellRenderer(new MapElementTreeCellRenderer());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent event) {
				TreePath[] paths = tree.getSelectionPaths();
				if (paths != null) {
					if (paths.length == 1) {
						doSelection((DefaultMutableTreeNode) paths[0].getLastPathComponent());
					} else {
						DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[paths.length];
						for (int i = 0; i < paths.length; ++i) {
							nodes[i] = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
						}
						doSelection(nodes);
					}
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setMinimumSize(new Dimension(128, 128));
		JPanel tPanel = new JPanel(new BorderLayout());
		tPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new GridLayout(15, 1, 0, 0));
		fillButtonPanel(buttonPanel);
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(buttonPanel);
		tPanel.add(panel, BorderLayout.EAST);
		add(tPanel, BorderLayout.CENTER);
		
		// Add buttons
		addElementPanel = new AddElementPanel();
		add(addElementPanel, BorderLayout.SOUTH);

		// Search function
		JPanel sPanel = new JPanel(new BorderLayout(0, 0));
		sPanel.setBorder(BorderFactory.createEmptyBorder());
		searchText = new JTextField();
		searchText.setToolTipText("enter text for search");
		sPanel.add(searchText, BorderLayout.CENTER);
		findButton = new JButton("Find");
		findButton.setToolTipText("press to search map element list");
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String str = searchText.getText();
				if (!str.equals(lastSearch)) {
					lastSearch = str;
					searchResult = findTreePaths(str);
					searchIndex = -1;
				}
				if (searchResult != null) {
					searchIndex++;
					if (searchIndex >= searchResult.length) {
						searchIndex = 0;
					}
					tree.setSelectionPath(searchResult[searchIndex]);
					tree.scrollPathToVisible(searchResult[searchIndex]);
				}
			}
		};
		findButton.addActionListener(actionListener);
		searchText.addActionListener(actionListener);
		sPanel.add(findButton, BorderLayout.EAST);
		add(sPanel, BorderLayout.NORTH);
		SceneGraphManager.getSceneGraphManager().addDirtyEventListener(this);
		tree.expandPath(new TreePath(new Object[] { rootNode, landmarksNode }));
		tree.expandPath(new TreePath(new Object[] { rootNode, toolsNode }));
		tree.expandPath(new TreePath(new Object[] { rootNode, featureSetsNode }));
		selectMapElement(state.getLastMapElement());
	}
	
	public Insets getInsets() {
		return(new Insets(5, 5, 5, 5));
	}

	private void addMapElement(MapElement mapElement) {
		// Add landmark
		if (mapElement instanceof Landmark) {
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(mapElement, false);
			landmarksNode.add(treeNode);
			treeModel.nodeStructureChanged(landmarksNode);
			tree.setSelectionPath(new TreePath(new Object[] { rootNode, landmarksNode, treeNode }));
		}
		// Add tool
		else if (mapElement instanceof Tool) {
			DefaultMutableTreeNode treeNode = null;
			if (mapElement instanceof Path) {
				Path path = (Path) mapElement;
				treeNode = new DefaultMutableTreeNode(mapElement, true);
				for (int j = 0; j < path.getNumberOfPoints(); ++j) {
					treeNode.add(new DefaultMutableTreeNode(path.getWaypoint(j), false));
				}
			} else {
				treeNode = new DefaultMutableTreeNode(mapElement, false);
			}
			toolsNode.add(treeNode);
			treeModel.nodeStructureChanged(toolsNode);
			tree.setSelectionPath(new TreePath(new Object[] { rootNode, toolsNode, treeNode }));
		}
		// Add FeatureSet
		else if (mapElement instanceof FeatureSet) {
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(mapElement, true);
			FeatureSet fSet = (FeatureSet)mapElement;
			for (int j = 0; j < fSet.getNumberOfChildren(); ++j) {
				treeNode.add(new DefaultMutableTreeNode(fSet.getChild(j), false));
			}
			featureSetsNode.add(treeNode);
			treeModel.nodeStructureChanged(featureSetsNode);
			tree.setSelectionPath(new TreePath(new Object[] { rootNode, featureSetsNode, treeNode }));
		}
		// Add waypoint
		else if (mapElement instanceof Waypoint) {
			Waypoint waypoint = (Waypoint) mapElement;
			Path path = waypoint.getPath();
			int n = toolsNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode pathNode = (DefaultMutableTreeNode) toolsNode.getChildAt(i);
				if ((MapElement) pathNode.getUserObject() == path) {
					DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(mapElement, false);
					pathNode.insert(treeNode, path.getWaypointIndex(waypoint));
					treeModel.nodeStructureChanged(pathNode);
					tree.setSelectionPath(new TreePath(new Object[] { rootNode, toolsNode, pathNode, treeNode }));
					break;
				}
			}
		}
	}

	private TreePath[] findTreePaths(String searchStr) {
		ArrayList<TreePath> list = new ArrayList<TreePath>();
		int n = landmarksNode.getChildCount();
		for (int i = 0; i < n; ++i) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) landmarksNode.getChildAt(i);
			if (child.toString().contains(searchStr)) {
				list.add(new TreePath(child.getPath()));
			}
		}
		n = toolsNode.getChildCount();
		for (int i = 0; i < n; ++i) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) toolsNode.getChildAt(i);
			if (child.toString().contains(searchStr)) {
				list.add(new TreePath(child.getPath()));
			}
			int nn = child.getChildCount();
			for (int j = 0; j < nn; ++j) {
				DefaultMutableTreeNode grandChild = (DefaultMutableTreeNode) child.getChildAt(j);
				if (grandChild.toString().contains(searchStr)) {
					list.add(new TreePath(grandChild.getPath()));
				}
			}
		}
		n = featureSetsNode.getChildCount();
		for (int i = 0; i < n; ++i) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) featureSetsNode.getChildAt(i);
			if (child.toString().contains(searchStr)) {
				list.add(new TreePath(child.getPath()));
			}
			int nn = child.getChildCount();
			for (int j = 0; j < nn; ++j) {
				DefaultMutableTreeNode grandChild = (DefaultMutableTreeNode) child.getChildAt(j);
				if (grandChild.toString().contains(searchStr)) {
					list.add(new TreePath(grandChild.getPath()));
				}
			}
		}
		if (list.size() == 0) {
			return (null);
		}
		TreePath[] tPath = new TreePath[list.size()];
		list.toArray(tPath);
		return (tPath);
	}

	/**
	 * Select the map element to display
	 * 
	 * @param mapElement
	 */
	public void selectMapElement(MapElement mapElement) {
		if (mapElement == null) {
			tree.setSelectionPath(null);
			return;
		}
		TreePath treePath = null;
		DefaultMutableTreeNode treeNode = null;
		if (mapElement instanceof Landmark) {
			int n = landmarksNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) landmarksNode.getChildAt(i);
				if ((MapElement) dmtn.getUserObject() == mapElement) {
					treeNode = dmtn;
					break;
				}
			}
			if (treeNode != null)
				treePath = new TreePath(new Object[] { rootNode, landmarksNode, treeNode });
		} else if (mapElement instanceof Waypoint) {
			Path path = ((Waypoint)mapElement).getPath();
			if (path != null) {
				int n = toolsNode.getChildCount();
				DefaultMutableTreeNode ptNode = null;
				for (int i = 0; i < n; ++i) {
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) toolsNode.getChildAt(i);
					if ((MapElement) dmtn.getUserObject() == path) {
						ptNode = dmtn;
						break;
					}
				}
				if (ptNode != null) {
					n = ptNode.getChildCount();
					for (int i=0; i<n; ++i) {
						DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) ptNode.getChildAt(i);
						if ((MapElement) dmtn.getUserObject() == mapElement) {
							treeNode = dmtn;
							break;
						}
					}
					if (treeNode != null)
						treePath = new TreePath(new Object[] { rootNode, toolsNode, ptNode, treeNode });
				}
			}
		} else if (mapElement instanceof Tool) {
			int n = toolsNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) toolsNode.getChildAt(i);
				if ((MapElement) dmtn.getUserObject() == mapElement) {
					treeNode = dmtn;
					break;
				}
			}
			if (treeNode != null)
				treePath = new TreePath(new Object[] { rootNode, toolsNode, treeNode });
		} else if (mapElement instanceof FeatureSet) {
			int n = featureSetsNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) featureSetsNode.getChildAt(i);
				if ((MapElement) dmtn.getUserObject() == mapElement) {
					treeNode = dmtn;
					break;
				}
			}
			if (treeNode != null)
				treePath = new TreePath(new Object[] { rootNode, featureSetsNode, treeNode });
		} else if (mapElement instanceof Feature) {
			FeatureSet fs = (FeatureSet)((Feature)mapElement).getParent();
			int n = featureSetsNode.getChildCount();
			DefaultMutableTreeNode fstnode = null;
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) featureSetsNode.getChildAt(i);
				if ((MapElement) dmtn.getUserObject() == fs) {
					fstnode = dmtn;
					break;
				}
			}
			if (fstnode != null) {
				n = fstnode.getChildCount();
				for (int i=0; i<n; ++i) {
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) fstnode.getChildAt(i);
					if ((MapElement) dmtn.getUserObject() == mapElement) {
						treeNode = dmtn;
						break;
					}
				}
				if (treeNode != null)
					treePath = new TreePath(new Object[] { rootNode, featureSetsNode, fstnode, treeNode });
			}
		}
		if (treePath != null) {
			tree.setSelectionPath(treePath);
			tree.scrollPathToVisible(treePath);
		}
	}

	private void updateMapElement(MapElement mapElement) {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		DefaultMutableTreeNode treeNode = null;
		if (mapElement instanceof Landmark) {
			int n = landmarksNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) landmarksNode.getChildAt(i);
				if ((MapElement) dmtn.getUserObject() == mapElement) {
					treeNode = dmtn;
					break;
				}
			}
		} else if (mapElement instanceof Tool) {
			int n = toolsNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) toolsNode.getChildAt(i);
				if ((MapElement) dmtn.getUserObject() == mapElement) {
					treeNode = dmtn;
					break;
				}
			}
		} else if (mapElement instanceof FeatureSet) {
			int n = featureSetsNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) featureSetsNode.getChildAt(i);
				if ((MapElement) dmtn.getUserObject() == mapElement) {
					treeNode = dmtn;
					break;
				}
			}
		} else if (mapElement instanceof Waypoint) {
			Waypoint waypoint = (Waypoint) mapElement;
			Path path = waypoint.getPath();
			int n = toolsNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode pathNode = (DefaultMutableTreeNode) toolsNode.getChildAt(i);
				if ((MapElement) pathNode.getUserObject() == path) {
					int m = pathNode.getChildCount();
					for (int j = 0; j < m; ++j) {
						DefaultMutableTreeNode wpNode = (DefaultMutableTreeNode) pathNode.getChildAt(j);
						if ((MapElement) wpNode.getUserObject() == mapElement) {
							treeNode = wpNode;
							break;
						}
					}
				}
				if (treeNode != null) {
					break;
				}
			}
		} else if (mapElement instanceof Feature) {
			Feature feature = (Feature) mapElement;
			FeatureSet featureSet = (FeatureSet)feature.getParent();
			int n = featureSetsNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode fsNode = (DefaultMutableTreeNode) featureSetsNode.getChildAt(i);
				if ((MapElement) fsNode.getUserObject() == featureSet) {
					int m = fsNode.getChildCount();
					for (int j = 0; j < m; ++j) {
						DefaultMutableTreeNode fNode = (DefaultMutableTreeNode) fsNode.getChildAt(j);
						if ((MapElement) fNode.getUserObject() == mapElement) {
							treeNode = fNode;
							break;
						}
					}
				}
				if (treeNode != null) {
					break;
				}
			}
		}
		if (treeNode != null) {
			treeModel.nodeChanged(treeNode);
			if (treeNode == selectedNode)
				enableButtons(false, mapElement);
		}
	}

	/**
	 * Remove a map element
	 * 
	 * @param mapElement
	 */
	public void removeMapElement(MapElement mapElement) {
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		boolean selected = false;
		if (mapElement instanceof Landmark) {
			for (int i = 0; i < landmarksNode.getChildCount(); ++i) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) landmarksNode.getChildAt(i);
				MapElement me = (MapElement) treeNode.getUserObject();
				if (me.getName().equals(mapElement.getName())) {
					selected = (treeNode == selectedNode);
					landmarksNode.remove(treeNode);
					break;
				}
			}
			treeModel.nodeStructureChanged(landmarksNode);
			if (selected) {
				tree.setSelectionPath(new TreePath(new Object[] { rootNode, landmarksNode }));
				doSelection(landmarksNode);
			}
		} else if (mapElement instanceof Tool) {
			for (int i = 0; i < toolsNode.getChildCount(); ++i) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) toolsNode.getChildAt(i);
				MapElement me = (MapElement) treeNode.getUserObject();
				if (me.getName().equals(mapElement.getName())) {
					selected = (treeNode == selectedNode);
					toolsNode.remove(treeNode);
					break;
				}
			}
			treeModel.nodeStructureChanged(toolsNode);
			if (selected) {
				tree.setSelectionPath(new TreePath(new Object[] { rootNode, toolsNode }));
				doSelection(toolsNode);
			}
		} else if (mapElement instanceof FeatureSet) {
			for (int i = 0; i < featureSetsNode.getChildCount(); ++i) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) featureSetsNode.getChildAt(i);
				MapElement me = (MapElement) treeNode.getUserObject();
				if (me.getName().equals(mapElement.getName())) {
					selected = (treeNode == selectedNode);
					featureSetsNode.remove(treeNode);
					break;
				}
			}
			treeModel.nodeStructureChanged(featureSetsNode);
			if (selected) {
				tree.setSelectionPath(new TreePath(new Object[] { rootNode, featureSetsNode }));
				doSelection(featureSetsNode);
			}
		} else if (mapElement instanceof Waypoint) {
			Waypoint waypoint = (Waypoint) mapElement;
			String str = waypoint.getPathName();
			int n = toolsNode.getChildCount();
			for (int i = 0; i < n; ++i) {
				DefaultMutableTreeNode pathNode = (DefaultMutableTreeNode) toolsNode.getChildAt(i);
				MapElement parent = (MapElement) pathNode.getUserObject();
				if ((parent instanceof Path) && parent.getName().equals(str)) {
					for (int j = 0; j < pathNode.getChildCount(); ++j) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) pathNode.getChildAt(j);
						if (treeNode.getUserObject() == waypoint) {
							selected = (treeNode == selectedNode);
							pathNode.remove(treeNode);
							treeModel.nodeStructureChanged(pathNode);
							if (selected) {
								tree.setSelectionPath(new TreePath(new Object[] { rootNode, toolsNode, pathNode }));
								doSelection(pathNode);
							}
							break;
						}
					}
					break;
				}
			}
		}

	}

	private void loadTreeModel() {
		rootNode = new DefaultMutableTreeNode("", true);

		Landmarks landmarks = World.getInstance().getLandmarks();
		landmarksNode = new DefaultMutableTreeNode("Landmarks", true);
		rootNode.add(landmarksNode);
		for (int i = 0; i < landmarks.getNumberOfChildren(); ++i) {
			Landmark lm = (Landmark) landmarks.getChild(i);
			landmarksNode.add(new DefaultMutableTreeNode(lm, false));
		}

		Tools tools = World.getInstance().getTools();
		toolsNode = new DefaultMutableTreeNode("Tools", true);
		rootNode.add(toolsNode);
		for (int i = 0; i < tools.getNumberOfChildren(); ++i) {
			Tool t = (Tool) tools.getChild(i);
			if (t instanceof Path) {
				Path path = (Path) t;
				DefaultMutableTreeNode pathNode = new DefaultMutableTreeNode(t, true);
				toolsNode.add(pathNode);
				for (int j = 0; j < path.getNumberOfPoints(); ++j) {
					pathNode.add(new DefaultMutableTreeNode(path.getWaypoint(j), false));
				}
			} else {
				toolsNode.add(new DefaultMutableTreeNode(t, false));
			}
		}

		FeatureSets featureSets = World.getInstance().getFeatureSets();
		featureSetsNode = new DefaultMutableTreeNode("Feature Sets", true);
		rootNode.add(featureSetsNode);
		for (int i = 0; i < featureSets.getNumberOfChildren(); ++i) {
			FeatureSet vg = (FeatureSet) featureSets.getChild(i);
			DefaultMutableTreeNode featureSetNode = new DefaultMutableTreeNode(vg, true);
			featureSetsNode.add(featureSetNode);
			for (int j = 0; j < vg.getNumberOfChildren(); ++j)
				featureSetNode.add(new DefaultMutableTreeNode(vg.getChild(j), false));
		}

		treeModel = new DefaultTreeModel(rootNode);
	}

	private void doSelection(DefaultMutableTreeNode treeNode) {
		currentMapElements = null;
		if ((treeNode == landmarksNode) || (treeNode == toolsNode) || (treeNode == featureSetsNode)) {
			currentMapElements = new MapElement[treeNode.getChildCount()];
			for (int i=0; i<currentMapElements.length; ++i)
				currentMapElements[i] = (MapElement)((DefaultMutableTreeNode)treeNode.getChildAt(i)).getUserObject();			
			enableButtons(treeNode == landmarksNode, null);
			state.setLastMapElement(null);
		}
		else {
			currentMapElements = new MapElement[] {(MapElement)treeNode.getUserObject()};
			enableButtons(treeNode == landmarksNode, currentMapElements[0]);
			state.setLastMapElement(currentMapElements[0]);
			if (currentMapElements[0] instanceof Waypoint) {
				Waypoint wp = (Waypoint)currentMapElements[0];
				wp.getPath().getState().setMapElement(currentMapElements[0]);
			}
			else if (currentMapElements[0] instanceof Path) {
				((Path)currentMapElements[0]).getState().setMapElement(currentMapElements[0]);
			}
			else if (currentMapElements[0] instanceof Feature) {
				Feature f = (Feature)currentMapElements[0];
				f.getFeatureSet().getState().setMapElement(currentMapElements[0]);
			}
			else if (currentMapElements[0] instanceof FeatureSet) {
				((FeatureSet)currentMapElements[0]).getState().setMapElement(currentMapElements[0]);
			}
		}
	}

	private void doSelection(DefaultMutableTreeNode[] treeNode) {
		ArrayList<MapElement> meList = new ArrayList<MapElement>();
		currentMapElements = null;
		for (int i = 0; i < treeNode.length; ++i) {
			if ((treeNode[i] == landmarksNode) || (treeNode[i] == toolsNode) || (treeNode[i] == featureSetsNode)) {
				for (int j=0; j<treeNode[i].getChildCount(); ++j)
					meList.add((MapElement)((DefaultMutableTreeNode)treeNode[i].getChildAt(i)).getUserObject());
			}
			else {
				MapElement me = (MapElement)((DefaultMutableTreeNode)treeNode[i]).getUserObject();
				meList.add(me);
			}
		}
		currentMapElements = new MapElement[meList.size()];
		meList.toArray(currentMapElements);
		enableButtons(false, null);
		
		state.setLastMapElement(null);
	}
	
	private void enableButtons(boolean allLandmark, MapElement currentMapElement) {
		if (currentMapElements == null) {
			editButton.setEnabled(false);
			openButton.setEnabled(false);
			colorList.setColor(Color.white);
			colorList.setEnabled(false);
			showButton.setEnabled(false);
			hideButton.setEnabled(false);
			lockButton.setEnabled(false);
			unlockButton.setEnabled(false);
			labelButton.setEnabled(false);
			unlabelButton.setEnabled(false);
			deleteButton.setEnabled(false);
			groundButton.setEnabled(false);
			seekButton.setEnabled(false);
			renameButton.setEnabled(false);
			csvButton.setEnabled(false);
			noteButton.setEnabled(false);
		}
		else if (currentMapElement == null) {
			editButton.setEnabled(false);
			openButton.setEnabled(false);
			colorList.setColor(Color.white);
			colorList.setEnabled(true);
			showButton.setEnabled(true);
			hideButton.setEnabled(true);
			lockButton.setEnabled(true);
			unlockButton.setEnabled(true);
			labelButton.setEnabled(true);
			unlabelButton.setEnabled(true);
			deleteButton.setEnabled(true);
			groundButton.setEnabled(true);
			seekButton.setEnabled(false);
			renameButton.setEnabled(false);
			csvButton.setEnabled(allLandmark);
			noteButton.setEnabled(false);
		}
		else {
			editButton.setEnabled(!(currentMapElement instanceof Feature));
			openButton.setEnabled(!(currentMapElement instanceof Waypoint) && !(currentMapElement instanceof Grid)
					&& !(currentMapElement instanceof Figure) && !(currentMapElement instanceof Placemark) && !(currentMapElement instanceof ScaleBar));
			colorList.setColor(currentMapElement.getColor());
			colorList.setEnabled(!(currentMapElement instanceof ImageBoard) && !(currentMapElement instanceof Feature) && !(currentMapElement instanceof Waypoint));
			showButton.setEnabled(!currentMapElement.isVisible());
			hideButton.setEnabled(currentMapElement.isVisible());
			lockButton.setEnabled(!currentMapElement.isLocked() && !(currentMapElement instanceof FeatureSet) && !(currentMapElement instanceof Feature));
			unlockButton.setEnabled(currentMapElement.isLocked() && !(currentMapElement instanceof FeatureSet) && !(currentMapElement instanceof Feature));
			labelButton.setEnabled(!currentMapElement.isLabelVisible());
			unlabelButton.setEnabled(currentMapElement.isLabelVisible());
			deleteButton.setEnabled(!(currentMapElement instanceof Feature));
			groundButton.setEnabled(!(currentMapElement instanceof Feature) && !(currentMapElement instanceof FeatureSet));
			seekButton.setEnabled(true);
			renameButton.setEnabled(!(currentMapElement instanceof Feature) && !(currentMapElement instanceof Waypoint));
			csvButton.setEnabled(currentMapElement instanceof Path);
			noteButton.setEnabled(!(currentMapElement instanceof Feature));
		}

	}

	/**
	 * A map element has changed, been added, or removed.
	 */
	@Override
	public boolean spatialDirty(Spatial spatial, DirtyType type) {
		switch (type) {
		case Attached:
			if (spatial instanceof MapElement) {
				// System.err.println("MapElementsPanel.spatialDirty "+spatial.getName()+" "+spatial.getClass()+" "+spatial.getParent());
				addMapElement((MapElement) spatial);
			}
			break;
		case Detached:
			if (spatial instanceof MapElement) {
				removeMapElement((MapElement) spatial);
			}
			break;
		case Bounding:
			break;
		case RenderState:
			if (spatial instanceof MapElement) {
				updateMapElement((MapElement) spatial);
			}
			break;
		case Transform:
			break;
		case Destroyed:
			break;
		}
		return (false);
	}

	@Override
	public boolean spatialClean(Spatial spatial, DirtyType type) {
		return (false);
	}
	
	private void fillButtonPanel(JPanel buttonPanel) {
		
		// edit
		editButton = new JButton("Edit");
		editButton.setToolTipText("edit the selected map element");
		editButton.setEnabled(false);
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				currentMapElements[0].getState().openEditor();
			}
		});
		buttonPanel.add(editButton);
		
		// open
		openButton = new JButton("Open");
		openButton.setToolTipText("open the selected map element");
		openButton.setEnabled(false);
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				currentMapElements[0].getState().open(true);
			}
		});
		buttonPanel.add(openButton);
		
		// Delete
		deleteButton = new JButton("Delete");
		deleteButton.setToolTipText("remove the selected map element");
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String nameStr = currentMapElements[0].getName();
				if (currentMapElements.length > 1)
					nameStr += "...";
				boolean yes = OptionDialog.showDeleteConfirmDialog((Window)MapElementsPanel.this.getTopLevelAncestor(), "Delete " + nameStr + "?");
				if (yes) {
					MapElementState[] state = new MapElementState[currentMapElements.length];
					for (int i = 0; i < currentMapElements.length; ++i) {
						state[i] = currentMapElements[i].getState();
					}
					UndoHandler.getInstance().addEdit(new DeleteEditMulti(state));
					currentMapElements = null;
					enableButtons(false, null);
				}
			}
		});
		buttonPanel.add(deleteButton);
		
		colorList = new ColorSelectionPanel(Path.defaultColor) {
			@Override
			public void doColor(Color color) {
				for (int i=0; i<currentMapElements.length; ++i)
					currentMapElements[i].setColor(color);
			}
		};
		colorList.setToolTipText("set color of selected map element");
		colorList.setEnabled(false);
		buttonPanel.add(colorList);
		
		// Show
		showButton = new JButton("Show");
		showButton.setToolTipText("show selected map element");
		showButton.setEnabled(false);
		showButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (currentMapElements.length == 1)
					UndoHandler.getInstance().addEdit(new ShowEdit(currentMapElements[0]));
				else
					UndoHandler.getInstance().addEdit(new ShowEditMulti(currentMapElements));
				showButton.setEnabled(false);
				hideButton.setEnabled(true);
			}
		});
		buttonPanel.add(showButton);
		
		// Hide
		hideButton = new JButton("Hide");
		hideButton.setToolTipText("hide selected map element");
		hideButton.setEnabled(false);
		hideButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (currentMapElements.length == 1)
					UndoHandler.getInstance().addEdit(new HideEdit(currentMapElements[0]));
				else
					UndoHandler.getInstance().addEdit(new HideEditMulti(currentMapElements));
				hideButton.setEnabled(false);
				showButton.setEnabled(true);
			}
		});
		buttonPanel.add(hideButton);
		
		// Lock
		lockButton = new JButton("Lock");
		lockButton.setToolTipText("make selected map element immovable");
		lockButton.setEnabled(false);
		lockButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				for (int i=0; i<currentMapElements.length; ++i)
					currentMapElements[i].getState().setLocked(true);
				lockButton.setEnabled(false);
				unlockButton.setEnabled(true);
			}
		});
		buttonPanel.add(lockButton);
		
		// Unlock
		unlockButton = new JButton("Unlock");
		unlockButton.setToolTipText("make selected map element movable");
		unlockButton.setEnabled(false);
		unlockButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				for (int i=0; i<currentMapElements.length; ++i)
					currentMapElements[i].getState().setLocked(false);
				unlockButton.setEnabled(false);
				lockButton.setEnabled(true);
			}
		});
		buttonPanel.add(unlockButton);
		
		// Label
		labelButton = new JButton("Label");
		labelButton.setToolTipText("show label for selected map element");
		labelButton.setEnabled(false);
		labelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				for (int i=0; i<currentMapElements.length; ++i) {
					currentMapElements[i].setLabelVisible(true);
					((Spatial) currentMapElements[i]).markDirty(DirtyType.RenderState);
				}
				labelButton.setEnabled(false);
				unlabelButton.setEnabled(true);
			}
		});
		buttonPanel.add(labelButton);
		
		// Unlabel
		unlabelButton = new JButton("No Label");
		unlabelButton.setToolTipText("hide label for selected map element");
		unlabelButton.setEnabled(false);
		unlabelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				for (int i=0; i<currentMapElements.length; ++i) {
					currentMapElements[i].setLabelVisible(false);
					((Spatial) currentMapElements[i]).markDirty(DirtyType.RenderState);
				}
				unlabelButton.setEnabled(false);
				labelButton.setEnabled(true);
			}
		});
		buttonPanel.add(unlabelButton);
		
		// Seek
		seekButton = new JButton("Seek");
		seekButton.setToolTipText("move viewpoint close to selected map element");
		seekButton.setEnabled(false);
		seekButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				WorldView wv = Dert.getWorldView();
				ViewpointController cameraControl = wv.getScenePanel().getViewpointController();
				cameraControl.seek(currentMapElements[0]);
				UndoHandler.getInstance().addEdit(new SeekEdit(currentMapElements[0]));
			}
		});
		buttonPanel.add(seekButton);
		
		// Rename
		renameButton = new JButton("Rename");
		renameButton.setToolTipText("rename selected map element");
		renameButton.setEnabled(false);
		renameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String nameStr = NameDialog.getName((Dialog)getTopLevelAncestor(), currentMapElements[0].getName());
				if (nameStr == null) {
					return;
				}
				if (currentMapElements[0] instanceof Waypoint) {
					currentMapElements[0].getState().setAnnotation(nameStr);
				} else {
					currentMapElements[0].setName(nameStr);
				}
			}
		});
		buttonPanel.add(renameButton);

		// Ground
		groundButton = new JButton("Ground");
		groundButton.setToolTipText("put map element on terrain surface");
		groundButton.setEnabled(false);
		groundButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (currentMapElements.length == 1)
					UndoHandler.getInstance().addEdit(currentMapElements[0].ground());
				else {
					GroundEdit[] ge = new GroundEdit[currentMapElements.length];
					for (int i=0; i<currentMapElements.length; ++i)
						ge[i] = currentMapElements[i].ground();
					UndoHandler.getInstance().addEdit(new GroundEdit(ge));
				}
			}
		});
		buttonPanel.add(groundButton);
		
		// CSV
		csvButton = new JButton("To CSV");
		csvButton.setEnabled(false);
		csvButton.setToolTipText("save coordinates to a CSV formatted file");
		csvButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String fileName = FileHelper.getCSVFile();
				if (fileName == null) {
					return;
				}
				if (currentMapElements[0] instanceof Path) {
					((Path)currentMapElements[0]).saveAsCsv(fileName);
				}
				else if (currentMapElements[0] instanceof Profile) {
					((Profile)currentMapElements[0]).saveAsCsv(fileName);
				}
				else
					World.getInstance().getLandmarks().saveAsCsv(fileName);
			}
		});
		buttonPanel.add(csvButton);		
		
		// Annotations
		noteButton = new JButton("Note");
		noteButton.setEnabled(false);
		noteButton.setToolTipText("view and edit annotation");
		noteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				for (int i=0; i<currentMapElements.length; ++i) {
					currentMapElements[i].getState().save();
					currentMapElements[i].getState().openAnnotation();
				}
			}
		});
		buttonPanel.add(noteButton);		

	}
}
