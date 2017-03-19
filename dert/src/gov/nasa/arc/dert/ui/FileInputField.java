package gov.nasa.arc.dert.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class FileInputField
	extends JPanel {
	
	protected JTextField fileText;
	protected JButton browseButton;
	
	public FileInputField(String path, String toolTip) {
		setLayout(new BorderLayout());
		fileText = new JTextField();
		fileText.setText(path);
		fileText.setToolTipText(toolTip);
		add(fileText, BorderLayout.CENTER);
		browseButton = new JButton("Browse");
		browseButton.setToolTipText(toolTip);
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setFile();
			}
		});
		add(browseButton, BorderLayout.EAST);
	}
	
	public String getFilePath() {
		return(fileText.getText().trim());
	}
	
	public void setFilePath(String filePath) {
		fileText.setText(filePath);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		fileText.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}
	
	public abstract void setFile();

}
