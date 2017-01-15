package gov.nasa.arc.dert.icon;

import java.awt.Image;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Handles icon loading.
 *
 */
public class Icons {
	
	public final static ImageIcon DERT_ICON_24 = getImageIcon("dert_24.png");

	/**
	 * Get the URL for a specific icon.
	 * 
	 * @param iconName
	 *            the icon name
	 * @return the URL
	 */
	public static URL getIconURL(String iconName) {
		return (Icons.class.getResource(iconName));
	}

	/**
	 * Get the icon as an ImageIcon object.
	 * 
	 * @param iconName
	 *            the icon name
	 * @return the ImageData object
	 */
	public static ImageIcon getImageIcon(String iconName) {
		if (iconName == null) {
			return (null);
		}
		try {
			InputStream is = getIconURL(iconName).openStream();
			ImageIcon imageIcon = new ImageIcon(ImageIO.read(is));
			is.close();
			return (imageIcon);
		} catch (Exception e) {
			e.printStackTrace();
			return (null);
		}
	}

	/**
	 * Get the icon as an Image object.
	 * 
	 * @param iconName
	 *            the icon name
	 * @return the ImageData object
	 */
	public static Image getImage(String iconName) {
		try {
			InputStream is = getIconURL(iconName).openStream();
			Image image = ImageIO.read(is);
			is.close();
			return (image);
		} catch (Exception e) {
			return (null);
		}
	}
}
