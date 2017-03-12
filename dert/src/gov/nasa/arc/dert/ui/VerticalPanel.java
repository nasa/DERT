package gov.nasa.arc.dert.ui;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;

public class VerticalPanel
	extends JPanel {
	
	public VerticalPanel(ArrayList<Component> compList) {
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(Alignment.LEADING);
		for (int i=0; i<compList.size(); ++i)
			hGroup.addComponent(compList.get(i));
		layout.setHorizontalGroup(hGroup);
		
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		for (int i=0; i<compList.size(); ++i)
			vGroup.addComponent(compList.get(i));
		layout.setVerticalGroup(vGroup);
	}
	
	@Override
	public Insets getInsets() {
		return(new Insets(5, 5, 5, 5));
	}

}
