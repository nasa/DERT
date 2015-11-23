package gov.nasa.arc.dert.util;

import gov.nasa.arc.dert.scene.tool.Tool;
import gov.nasa.arc.dert.scenegraph.Marker;

import java.util.HashMap;

import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * Provides helper methods concerned with Ardor3D Spatial classes.
 *
 */
public class SpatialUtil {

	/**
	 * Determine if a spatial is drawn as wireframe.
	 * 
	 * @param spatial
	 * @return
	 */
	public static final boolean isWireFrame(Spatial spatial) {
		if (spatial == null) {
			return (false);
		}
		WireframeState wfs = (WireframeState) spatial.getLocalRenderState(RenderState.StateType.Wireframe);
		if (wfs == null) {
			return (false);
		}
		if (!wfs.isEnabled()) {
			return (false);
		}
		return (true);
	}

	/**
	 * Do a pick operation on a spatial
	 * 
	 * @param root
	 * @param pickRay
	 * @return
	 */
	public static PickResults doPick(Spatial root, final Ray3 pickRay) {
		root.updateWorldBound(true);
		final PrimitivePickResults bpr = new PrimitivePickResults();
		bpr.setCheckDistance(true);
		PickingUtil.findPick(root, pickRay, bpr);
		if (bpr.getNumber() == 0) {
			return (null);
		}
		PickData closest = bpr.getPickData(0);
		for (int i = 1; i < bpr.getNumber(); ++i) {
			PickData pd = bpr.getPickData(i);
			if (closest.getIntersectionRecord().getClosestDistance() > pd.getIntersectionRecord().getClosestDistance()) {
				closest = pd;
			}
		}
		return bpr;
	}

	/**
	 * Do a pick bounds operation on a spatial
	 * 
	 * @param root
	 * @param pickRay
	 * @param pickTop
	 * @return
	 */
	public static SpatialPickResults pickBounds(Node root, Ray3 pickRay, Node pickTop) {
		root.updateWorldBound(true);
		if (pickTop == null) {
			pickTop = root;
		}
		final SpatialPickResults spr = new SpatialPickResults();
		spr.setCheckDistance(true);
		PickingUtil.findPick(pickTop, pickRay, spr);
		if (spr.getMeshList() == null) {
			return (null);
		} else {
			return (spr);
		}
	}

	/**
	 * Set the spatial to return if picked.
	 * 
	 * @param spatial
	 * @param pickHost
	 */
	public static final void setPickHost(Spatial spatial, Spatial pickHost) {
		if (spatial instanceof Node) {
			Node node = (Node) spatial;
			for (int i = 0; i < node.getNumberOfChildren(); ++i) {
				setPickHost(node.getChild(i), pickHost);
			}
		} else if (spatial instanceof Mesh) {
			HashMap<String, Object> map = (HashMap<String, Object>) spatial.getUserData();
			if (map == null) {
				map = new HashMap<String, Object>();
				spatial.setUserData(map);
			}
			map.put("PickHost", pickHost);
		}
	}

	/**
	 * Get the spatial that is returned if picked.
	 * 
	 * @param spatial
	 * @return
	 */
	public static final Spatial getPickHost(Spatial spatial) {
		if (spatial == null) {
			return (null);
		}
		Spatial pickHost = spatial;
		while (!(pickHost instanceof Tool) && (pickHost.getParent() != null)) {
			if (pickHost instanceof Marker) {
				return (pickHost);
			}
			pickHost = pickHost.getParent();
		}
		return (pickHost);
	}
}
