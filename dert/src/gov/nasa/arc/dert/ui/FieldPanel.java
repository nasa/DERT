package gov.nasa.arc.dert.ui;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;

public class FieldPanel
	extends JPanel {
	
	public FieldPanel(ArrayList<Component> compList) {
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup pGroup = layout.createParallelGroup(Alignment.TRAILING);
		for (int i=0; i<compList.size(); i+=2)
			pGroup.addComponent(compList.get(i));
		hGroup.addGroup(pGroup);
		pGroup = layout.createParallelGroup(Alignment.LEADING);
		for (int i=1; i<compList.size(); i+=2)
			pGroup.addComponent(compList.get(i));
		hGroup.addGroup(pGroup);
		layout.setHorizontalGroup(hGroup);
		
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		for (int i=0; i<compList.size(); i+=2) {
			pGroup = layout.createParallelGroup(Alignment.CENTER);
			pGroup.addComponent(compList.get(i));
			pGroup.addComponent(compList.get(i+1));
			vGroup.addGroup(pGroup);
		}
		layout.setVerticalGroup(vGroup);
	}
	
	@Override
	public Insets getInsets() {
		return(new Insets(5, 5, 5, 5));
	}

}
