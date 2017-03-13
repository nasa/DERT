package gov.nasa.arc.dert.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class IconComboBox
	extends JComboBox {
	
	private String[] label;
	private Icon[] icon;
	
	public IconComboBox(String[] name, Icon[] image) {
		this.label = name;
		this.icon = image;
				
		BasicComboBoxRenderer cbRenderer = new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel comp = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value == null) {
					comp.setIcon(null);
					comp.setText("");
				}
				else {
					Integer ii = (Integer)value;
					comp.setIcon(icon[ii.intValue()]);
					comp.setText(label[ii.intValue()]);
				}
				return(comp);
			}
		};
		setRenderer(cbRenderer);
		
		for (int i=0; i<name.length; ++i)
			addItem(new Integer(i));
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		int h = icon[0].getIconHeight()+4;
		if (dim.height < h)
			dim.height = h;
		return(dim);
	}

}
