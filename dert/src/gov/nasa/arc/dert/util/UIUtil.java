package gov.nasa.arc.dert.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

/**
 * Provides some helper methods for user interface tasks.
 *
 */
public class UIUtil {

	public static void setEnabled(Component comp, boolean enabled) {
		comp.setEnabled(enabled);
		if (comp instanceof Container) {
			Container cont = (Container) comp;
			Component[] child = cont.getComponents();
			for (int i = 0; i < child.length; ++i) {
				setEnabled(child[i], enabled);
			}
		}
	}

	public static Color colorRGBAToColor(ReadOnlyColorRGBA rgba) {
		return (new Color(rgba.getRed(), rgba.getGreen(), rgba.getBlue(), rgba.getAlpha()));
	}

	public static ColorRGBA colorToColorRGBA(Color color) {
		return (new ColorRGBA(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
			color.getAlpha() / 255.0f));
	}

	public static float[] colorToFloatArray(Color col) {
		float[] fArray = new float[4];
		fArray[0] = col.getRed() / 255f;
		fArray[1] = col.getGreen() / 255f;
		fArray[2] = col.getBlue() / 255f;
		fArray[3] = col.getAlpha() / 255f;
		return (fArray);
	}

}
