package gov.nasa.arc.dert.ui;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;

public class VerticalPanel
	extends JPanel {
	
	public VerticalPanel(ArrayList<Component> compList, int gap) {
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(Alignment.LEADING);
		for (int i=0; i<compList.size(); ++i)
			hGroup.addComponent(compList.get(i));
		layout.setHorizontalGroup(hGroup);
		
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		for (int i=0; i<compList.size(); ++i) {
			if (gap > 0)
				vGroup.addGap(gap);
			vGroup.addComponent(compList.get(i), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		}
		layout.setVerticalGroup(vGroup);
	}

}
