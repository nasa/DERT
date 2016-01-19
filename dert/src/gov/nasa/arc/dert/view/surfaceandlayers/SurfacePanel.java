package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.view.world.WorldScene;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides controls for landscape surface options.
 *
 */
public class SurfacePanel extends GroupPanel {

	// Controls
	private JCheckBox shadingCheckBox, wireframeCheckBox, surfaceNormalsCheckBox;
	private JCheckBox gridButton;
	private DoubleSpinner vertExag;
	private ColorSelectionPanel surfaceColor;
	private JComboBox cellSizeCombo;
	private ColorSelectionPanel gridColor;

	// Grid
	private ReadOnlyColorRGBA gridColorRGBA;
	private DecimalFormat formatter;
	private double[] units;

	/**
	 * Constructor
	 * 
	 * @param s
	 */
	public SurfacePanel(State s) {
		super("Surface");
		formatter = new DecimalFormat("0");

		final Landscape landscape = World.getInstance().getLandscape();
		final WorldScene scene = (WorldScene) Dert.getWorldView().getScenePanel().getScene();

		setLayout(new GridBagLayout());

		wireframeCheckBox = new JCheckBox("Wireframe");
		wireframeCheckBox.setToolTipText("display landscape as wireframe");
		wireframeCheckBox.setSelected(SpatialUtil.isWireFrame(World.getInstance().getLandscape()));
		wireframeCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				WireframeState wfs = new WireframeState();
				wfs.setEnabled(wireframeCheckBox.isSelected());
				World.getInstance().getLandscape().setRenderState(wfs);
			}
		});
		add(wireframeCheckBox, GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));

		shadingCheckBox = new JCheckBox("Surface Shading");
		shadingCheckBox.setToolTipText("shading determined by surface topography");
		shadingCheckBox.setSelected(landscape.isShadingFromSurface());
		shadingCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				World.getInstance().getLandscape().setShadingFromSurface(shadingCheckBox.isSelected());
			}
		});
		add(shadingCheckBox, GBCHelper.getGBC(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));

		surfaceNormalsCheckBox = new JCheckBox("Surface Normals");
		surfaceNormalsCheckBox.setToolTipText("display normal vector for each surface point");
		surfaceNormalsCheckBox.setSelected(scene.isNormalsEnabled());
		surfaceNormalsCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				scene.enableNormals(surfaceNormalsCheckBox.isSelected());
			}
		});
		add(surfaceNormalsCheckBox,
			GBCHelper.getGBC(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));

		JLabel label = new JLabel("Vertical Exaggeration", SwingConstants.RIGHT);
		add(label, GBCHelper.getGBC(1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0));
		vertExag = new DoubleSpinner(World.getInstance().getVerticalExaggeration(), 0.01, 10, 0.05, false, "#0.00") {
			@Override
			public void stateChanged(ChangeEvent event) {
				double val = ((Double) vertExag.getValue());
				World.getInstance().setVerticalExaggeration(val);
			}
		};
		vertExag.setToolTipText("scale elevation");
		add(vertExag, GBCHelper.getGBC(2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0));

		label = new JLabel("Surface Color", SwingConstants.RIGHT);
		add(label, GBCHelper.getGBC(1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0));
		surfaceColor = new ColorSelectionPanel(landscape.getSurfaceColor()) {
			@Override
			public void doColor(Color color) {
				landscape.setSurfaceColor(color);
			}
		};
		add(surfaceColor, GBCHelper.getGBC(2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0));

		gridButton = new JCheckBox("Surface Grid");
		gridButton.setToolTipText("display a grid on the surface");
		gridButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
				layerManager.enableGrid(gridButton.isSelected());
				World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
			}
		});
		add(gridButton, GBCHelper.getGBC(0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));

		label = new JLabel("Grid Cell Size", SwingConstants.RIGHT);
		add(label, GBCHelper.getGBC(1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0));
		String[] item = createUnits();
		cellSizeCombo = new JComboBox(item);
		double gridCellSize = landscape.getLayerManager().getGridCellSize();
		for (int i = 0; i < units.length; ++i) {
			if (gridCellSize <= units[i]) {
				cellSizeCombo.setSelectedIndex(i);
				break;
			}
		}
		cellSizeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = cellSizeCombo.getSelectedIndex();
				LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
				layerManager.setGridCellSize(units[index]);
				World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
			}
		});
		add(cellSizeCombo, GBCHelper.getGBC(2, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0));

		label = new JLabel("Grid Color", SwingConstants.RIGHT);
		add(label, GBCHelper.getGBC(1, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0));
		gridColorRGBA = landscape.getLayerManager().getGridColor();
		final Color gCol = new Color(gridColorRGBA.getRed(), gridColorRGBA.getGreen(), gridColorRGBA.getBlue(),
			gridColorRGBA.getAlpha());
		gridColor = new ColorSelectionPanel(gCol) {
			@Override
			public void doColor(Color color) {
				gridColorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
					color.getAlpha() / 255f);
				LayerManager layerManager = World.getInstance().getLandscape().getLayerManager();
				layerManager.setGridColor(gridColorRGBA);
				World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
			}
		};
		add(gridColor, GBCHelper.getGBC(2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));
	}

	private String[] createUnits() {
		units = new double[10];
		units[0] = 0.1 * Grid.defaultCellSize;
		units[1] = 0.5 * Grid.defaultCellSize;
		units[2] = Grid.defaultCellSize;
		units[3] = 2 * Grid.defaultCellSize;
		units[4] = 2.5 * Grid.defaultCellSize;
		units[5] = 5 * Grid.defaultCellSize;
		units[6] = 10 * Grid.defaultCellSize;
		units[7] = 20 * Grid.defaultCellSize;
		units[8] = 25 * Grid.defaultCellSize;
		units[9] = 50 * Grid.defaultCellSize;
		if (Grid.defaultCellSize < 1) {
			formatter.applyPattern("0.000");
		}
		String[] item = new String[units.length];
		for (int i = 0; i < units.length; ++i) {
			item[i] = formatter.format(units[i]);
		}
		return (item);
	}

}
