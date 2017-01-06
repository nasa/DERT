package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.mapelement.NameDialog;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scene.featureset.FeatureSets;
import gov.nasa.arc.dert.scene.landmark.Landmark;
import gov.nasa.arc.dert.scene.landmark.Landmarks;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Tool;
import gov.nasa.arc.dert.scene.tool.Tools;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementsState;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.view.world.DeleteEdit;
import gov.nasa.arc.dert.view.world.DeleteEditMulti;
import gov.nasa.arc.dert.view.world.HideEdit;
import gov.nasa.arc.dert.view.world.HideEditMulti;
import gov.nasa.arc.dert.view.world.SeekEdit;
import gov.nasa.arc.dert.view.world.ShowEdit;
import gov.nasa.arc.dert.view.world.ShowEditMulti;
import gov.nasa.arc.dert.view.world.WorldScene;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ViewpointNode;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
	private JButton showButton, deleteButton, seekButton, renameButton, findButton;

	// MapElement panels
	private PlacemarkPanel placemarkPanel;
	private FigurePanel figurePanel;
	private ImageBoardPanel imageBoardPanel;
	private FeatureSetPanel featureSetPanel;
	private PathPanel pathPanel;
	private PlanePanel planePanel;
	private CartesianGridPanel cartesianGridPanel;
	private RadialGridPanel radialGridPanel;
	private FieldCameraPanel fieldCameraPanel;
	private ProfilePanel profilePanel;
	private ScaleBarPanel scalePanel;

	// MapElement category panels
	private LandmarksPanel landmarksPanel;
	private ToolsPanel toolsPanel;
	private FeatureSetsPanel featureSetsPanel;

	// Controls
	private MapElementBasePanel currentPanel;
	private JPanel emptyPanel;
	private JPanel panelPane;
	private CardLayout panelLayout;
	private JSplitPane splitPane;

	// Map elements
	private MapElement currentMapElement;
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
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setLayout(new BorderLayout());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);

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
		splitPane.setLeftComponent(scrollPane);
		
		// Pane for panels
		panelLayout = new CardLayout();
		panelPane = new JPanel(panelLayout);
		splitPane.setRightComponent(panelPane);
		emptyPanel = new JPanel();
		panelLayout.addLayoutComponent(emptyPanel, "Empty");

		// Common functions
		JPanel buttonBar = new JPanel(new GridBagLayout());
		searchText = new JTextField();
		searchText.setToolTipText("enter text for search");
		buttonBar.add(searchText,
			GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));
		findButton = new JButton("Find");
		findButton.setToolTipText("press to search map element list");
		findButton.addActionListener(new ActionListener() {
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
		});
		buttonBar.add(findButton,
			GBCHelper.getGBC(1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0));
		buttonBar.add(new JLabel("    "),
			GBCHelper.getGBC(2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0));
		showButton = new JButton("Hide");
		showButton.setToolTipText("hide/show selected map element");
		showButton.setEnabled(false);
		showButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JButton button = (JButton) event.getSource();
				if (button.getText().equals("Hide")) {
					if (currentMapElement != null) {
						Dert.getMainWindow().getUndoHandler().addEdit(new HideEdit(currentMapElement));
					} else if (currentMapElements != null) {
						Dert.getMainWindow().getUndoHandler().addEdit(new HideEditMulti(currentMapElements));
					}
					showButton.setText("Show");
				} else {
					if (currentMapElement != null) {
						Dert.getMainWindow().getUndoHandler().addEdit(new ShowEdit(currentMapElement));
					} else if (currentMapElements != null) {
						Dert.getMainWindow().getUndoHandler().addEdit(new ShowEditMulti(currentMapElements));
					}
					showButton.setText("Hide");
				}
			}
		});
		buttonBar.add(showButton,
			GBCHelper.getGBC(3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0));
		deleteButton = new JButton("Delete");
		deleteButton.setToolTipText("remove selected map element");
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String nameStr = "";
				if (currentMapElement != null) {
					nameStr = currentMapElement.getName();
				} else if (currentMapElements != null) {
					nameStr = currentMapElements[0].getName() + "...";
				}
				int answer = JOptionPane.showConfirmDialog(MapElementsPanel.this, "Delete " + nameStr + "?",
					"Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					Icons.getImageIcon("delete.png"));
				if (answer == JOptionPane.OK_OPTION) {
					if (currentMapElement != null) {
						MapElementState state = currentMapElement.getState();
						Dert.getMainWindow().getUndoHandler().addEdit(new DeleteEdit(state));
						currentMapElement = null;
					} else if (currentMapElements != null) {
						MapElementState[] state = new MapElementState[currentMapElements.length];
						for (int i = 0; i < currentMapElements.length; ++i) {
							state[i] = currentMapElements[i].getState();
						}
						Dert.getMainWindow().getUndoHandler().addEdit(new DeleteEditMulti(state));
						currentMapElements = null;
					}
				}
			}
		});
		deleteButton.setEnabled(false);
		buttonBar.add(deleteButton,
			GBCHelper.getGBC(4, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0));
		seekButton = new JButton("Seek");
		seekButton.setToolTipText("move viewpoint close to selected map element");
		seekButton.setEnabled(false);
		seekButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				WorldView wv = Dert.getWorldView();
				ViewpointNode cameraControl = ((WorldScene) wv.getScenePanel().getScene()).getViewpointNode();
				cameraControl.seek(currentMapElement);
				Dert.getMainWindow().getUndoHandler().addEdit(new SeekEdit(currentMapElement));
			}
		});
		buttonBar.add(seekButton,
			GBCHelper.getGBC(5, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0));
		renameButton = new JButton("Rename");
		renameButton.setToolTipText("rename selected map element");
		renameButton.setEnabled(false);
		renameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String nameStr = NameDialog.getName(currentMapElement.getName());
				if (nameStr == null) {
					return;
				}
				if (currentMapElement instanceof Waypoint) {
					currentMapElement.getState().setAnnotation(nameStr);
				} else {
					currentMapElement.setName(nameStr);
				}
			}
		});
		buttonBar.add(renameButton,
			GBCHelper.getGBC(6, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0));
		add(buttonBar, BorderLayout.NORTH);
		SceneGraphManager.getSceneGraphManager().addDirtyEventListener(this);
		tree.expandPath(new TreePath(new Object[] { rootNode, landmarksNode }));
		tree.expandPath(new TreePath(new Object[] { rootNode, toolsNode }));
		tree.expandPath(new TreePath(new Object[] { rootNode, featureSetsNode }));
		selectMapElement(state.getLastMapElement());
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
			if (treeNode == selectedNode) {
				if (mapElement.isVisible()) {
					showButton.setText("Hide");
				} else {
					showButton.setText("Show");
				}
			}
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
		featureSetsNode = new DefaultMutableTreeNode("FeatureSets", true);
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
		currentMapElement = null;
		currentMapElements = null;
		if (treeNode == landmarksNode) {
			showButton.setEnabled(false);
			deleteButton.setEnabled(false);
			seekButton.setEnabled(false);
			renameButton.setEnabled(false);
			tree.clearSelection();
			if (landmarksPanel == null) {
				landmarksPanel = new LandmarksPanel() {
					@Override
					public void newMapElement(MapElementState.Type type, MapElement mapElement) {
						setPanel(type, mapElement);
					}

					@Override
					public void setAllVisible(boolean visible) {
						Landmarks landmarks = World.getInstance().getLandmarks();
						landmarks.setAllVisible(visible);
						treeModel.nodeChanged(rootNode);
					}
				};
				landmarksPanel.setMinimumSize(new Dimension(100, 100));
				panelPane.add(landmarksPanel, "Landmarks");
			}
			panelLayout.show(panelPane, "Landmarks");
		} else if (treeNode == toolsNode) {
			showButton.setEnabled(false);
			deleteButton.setEnabled(false);
			seekButton.setEnabled(false);
			renameButton.setEnabled(false);
			tree.clearSelection();
			if (toolsPanel == null) {
				toolsPanel = new ToolsPanel() {
					@Override
					public void newMapElement(MapElementState.Type type, MapElement mapElement) {
						setPanel(type, mapElement);
					}

					@Override
					public void setAllVisible(boolean visible) {
						Tools tools = World.getInstance().getTools();
						tools.setAllVisible(visible);
						treeModel.nodeChanged(rootNode);
					}
				};
				toolsPanel.setMinimumSize(new Dimension(100, 100));
				panelPane.add(toolsPanel, "Tools");
			}
			panelLayout.show(panelPane, "Tools");
		} else if (treeNode == featureSetsNode) {
			showButton.setEnabled(false);
			deleteButton.setEnabled(false);
			seekButton.setEnabled(false);
			renameButton.setEnabled(false);
			tree.clearSelection();
			if (featureSetsPanel == null) {
				featureSetsPanel = new FeatureSetsPanel() {
					@Override
					public void addFeatureSet(MapElementState.Type type) {
						FeatureSetDialog dialog = new FeatureSetDialog((Dialog)getTopLevelAncestor());
						if (dialog.open())
							setPanel(type, null);
					}

					@Override
					public void setAllVisible(boolean visible) {
						FeatureSets groups = World.getInstance().getFeatureSets();
						groups.setAllVisible(visible);
						treeModel.nodeChanged(rootNode);
					}
				};
				featureSetsPanel.setMinimumSize(new Dimension(100, 100));
				panelPane.add(featureSetsPanel, "FeatureSets");
			}
			panelLayout.show(panelPane, "FeatureSets");
		} else {
			currentMapElement = (MapElement) treeNode.getUserObject();
			showButton.setEnabled(true);
			if (currentMapElement.isVisible()) {
				showButton.setText("Hide");
			} else {
				showButton.setText("Show");
			}
			if (currentMapElement instanceof Feature)
				deleteButton.setEnabled(false);
			else
				deleteButton.setEnabled(true);
			seekButton.setEnabled(true);
			if ((currentMapElement instanceof Feature) || (currentMapElement instanceof Waypoint)) {
				renameButton.setEnabled(false);
			} else {
				renameButton.setEnabled(true);
			}
			setPanel(currentMapElement.getType(), currentMapElement);
		}
		state.setLastMapElement(currentMapElement);
	}

	private void doSelection(DefaultMutableTreeNode[] treeNode) {
		currentMapElement = null;
		currentMapElements = null;
		for (int i = 0; i < treeNode.length; ++i) {
			if ((treeNode[i] == landmarksNode) || (treeNode[i] == toolsNode) || (treeNode[i] == featureSetsNode)) {
				showButton.setEnabled(false);
				deleteButton.setEnabled(false);
				seekButton.setEnabled(false);
				renameButton.setEnabled(false);
				return;
			}
			for (int j = i + 1; j < treeNode.length; ++j) {
				if (treeNode[i].isNodeChild(treeNode[j])) {
					showButton.setEnabled(false);
					deleteButton.setEnabled(false);
					seekButton.setEnabled(false);
					renameButton.setEnabled(false);
					return;
				}
			}
		}
		currentMapElements = new MapElement[treeNode.length];
		for (int i = 0; i < treeNode.length; ++i) {
			currentMapElements[i] = (MapElement) treeNode[i].getUserObject();
		}
		showButton.setEnabled(true);
		if (currentMapElements[0].isVisible()) {
			showButton.setText("Hide");
		} else {
			showButton.setText("Show");
		}
		deleteButton.setEnabled(true);
		seekButton.setEnabled(false);
		renameButton.setEnabled(false);
		currentPanel = null;
	}

	private void setPanel(MapElementState.Type type, MapElement mapElement) {
		currentPanel = null;
		String card = type.toString();
		switch (type) {
		case Placemark:
			if (placemarkPanel == null) {
				placemarkPanel = new PlacemarkPanel(this);
				placemarkPanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(placemarkPanel, "Placemark");
			}
			currentPanel = placemarkPanel;
			break;
		case Figure:
			if (figurePanel == null) {
				figurePanel = new FigurePanel(this);
				figurePanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(figurePanel, "Figure");
			}
			currentPanel = figurePanel;
			break;
		case Billboard:
			if (imageBoardPanel == null) {
				imageBoardPanel = new ImageBoardPanel(this);
				imageBoardPanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(imageBoardPanel, "Billboard");
			}
			currentPanel = imageBoardPanel;
			break;
		case Feature:
			card = "FeatureSet";
		case FeatureSet:
			if (featureSetPanel == null) {
				featureSetPanel = new FeatureSetPanel(this);
				featureSetPanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(featureSetPanel, "FeatureSet");
			}
			currentPanel = featureSetPanel;
			break;
		case Waypoint:
			card = "Path";
		case Path:
			if (pathPanel == null) {
				pathPanel = new PathPanel(this);
				pathPanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(pathPanel, "Path");
			}
			currentPanel = pathPanel;
			break;
		case Plane:
			if (planePanel == null) {
				planePanel = new PlanePanel(this);
				planePanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(planePanel, "Plane");
			}
			currentPanel = planePanel;
			break;
		case RadialGrid:
			if (radialGridPanel == null) {
				radialGridPanel = new RadialGridPanel(this);
				radialGridPanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(radialGridPanel, "RadialGrid");
			}
			currentPanel = radialGridPanel;
			break;
		case CartesianGrid:
			if (cartesianGridPanel == null) {
				cartesianGridPanel = new CartesianGridPanel(this);
				cartesianGridPanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(cartesianGridPanel, "CartesianGrid");
			}
			currentPanel = cartesianGridPanel;
			break;
		case FieldCamera:
			if (fieldCameraPanel == null) {
				fieldCameraPanel = new FieldCameraPanel(this);
				fieldCameraPanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(fieldCameraPanel, "FieldCamera");
			}
			currentPanel = fieldCameraPanel;
			break;
		case Profile:
			if (profilePanel == null) {
				profilePanel = new ProfilePanel(this);
				profilePanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(profilePanel, "Profile");
			}
			currentPanel = profilePanel;
			break;
		case Scale:
			if (scalePanel == null) {
				scalePanel = new ScaleBarPanel(this);
				scalePanel.setMinimumSize(new Dimension(100,100));
				panelPane.add(scalePanel, "Scale");
			}
			currentPanel = scalePanel;
			break;
		default:
			break;
		}
		
		if (currentPanel == null)
			throw new IllegalStateException("No map element panel selected.");
		currentPanel.setMapElement(mapElement);
		panelLayout.show(panelPane, card);
		state.setLastMapElement(mapElement);
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
			if (spatial == currentMapElement) {
				currentPanel.updateData((MapElement) spatial);
			}
			break;
		case Transform:
			if (currentPanel != null) {
				if (spatial == currentMapElement) {
					currentPanel.updateLocation((MapElement) spatial);
				} else if (spatial.getParent() == currentMapElement) {
					currentPanel.updateLocation((MapElement) spatial.getParent());
				}
			}
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
	
	public void dispose() {
		if (placemarkPanel != null) {
			placemarkPanel.dispose();
		}
		if (figurePanel != null) {
			figurePanel.dispose();
		}
		if (imageBoardPanel != null) {
			imageBoardPanel.dispose();
		}
		if (featureSetPanel != null) {
			featureSetPanel.dispose();
		}
		if (pathPanel != null) {
			pathPanel.dispose();
		}
		if (planePanel != null) {
			planePanel.dispose();
		}
		if (radialGridPanel != null) {
			radialGridPanel.dispose();
		}
		if (cartesianGridPanel != null) {
			cartesianGridPanel.dispose();
		}
		if (fieldCameraPanel != null) {
			fieldCameraPanel.dispose();
		}
		if (profilePanel != null) {
			profilePanel.dispose();
		}
		if (scalePanel != null) {
			scalePanel.dispose();
		}
		
	}
}
