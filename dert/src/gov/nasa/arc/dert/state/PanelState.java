package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.view.ColorBarView;
import gov.nasa.arc.dert.view.ConsoleView;
import gov.nasa.arc.dert.view.HelpView;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.lighting.LightPositionView;
import gov.nasa.arc.dert.view.lighting.LightingView;
import gov.nasa.arc.dert.view.mapelement.MapElementsView;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;
import gov.nasa.arc.dert.view.viewpoint.ViewpointView;

import java.awt.Window;

/**
 * Base class for state objects based on AWT or Swing panels.
 *
 */
public class PanelState extends State {

	// Types of views
	public static enum PanelType {
		Console, Help, ColorBars, SurfaceAndLayers, Lighting, LightPosition, MapElements, Viewpoint
	}

	public PanelType type;
	public String title;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param title
	 * @param viewData
	 */
	public PanelState(PanelType type, String title, ViewData viewData) {
		super(type.toString(), StateType.Panel, viewData);
		this.type = type;
		this.title = title;
	}

	/**
	 * Open the view
	 * 
	 * @return
	 */
	public View open() {
		Window window = viewData.getViewWindow();
		View view = viewData.getView();
		if (window != null) {
			window.setVisible(true);
			return (view);
		}
		int xOffset = 20;
		int yOffset = 20;
		if (view == null) {
			switch (type) {
			case Help:
				view = new HelpView(this);
				break;
			case ColorBars:
				view = new ColorBarView(this);
				break;
			case Console:
				view = new ConsoleView(this);
				window = Dert.getConsoleWindow();
				xOffset = 0;
				yOffset = 600;
				break;
			case MapElements:
				view = new MapElementsView((MapElementsState) this);
				break;
			case Lighting:
				view = new LightingView(this);
				break;
			case LightPosition:
				view = new LightPositionView(this);
				break;
			case SurfaceAndLayers:
				view = new SurfaceAndLayersView(this);
				break;
			case Viewpoint:
				view = new ViewpointView((ViewpointState) this);
				break;
			default:
				throw new IllegalArgumentException("Unknown panel type " + type);
			}
			setView(view);
		}
		if (window == null) {
			window = viewData.createWindow(Dert.getMainWindow(), title, xOffset, yOffset);
		} else {
			viewData.setViewWindow(window, true, xOffset, yOffset);
		}
		window.setVisible(true);
		return (view);
	}

}
