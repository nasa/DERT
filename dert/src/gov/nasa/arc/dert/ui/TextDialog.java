/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brian Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.icon.Icons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A dialog that displays text in a JTextArea.
 *
 */
public class TextDialog extends AbstractDialog {

	protected JTextArea textArea;
	protected String theText, theMessage;
	protected Color theColor;
	protected boolean scrolled;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param width
	 * @param height
	 * @param addMessage
	 * @param addRefresh
	 */
	public TextDialog(Frame parent, String title, int width, int height, boolean addMessage, boolean addRefresh, boolean scrolled) {
		super(parent, title, false, addRefresh, addMessage);
		this.width = width;
		this.height = height;
		this.scrolled = scrolled;
	}

	@Override
	protected void build() {
		setBackground(Color.white);
		getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getRootPane().setLayout(new BorderLayout());
		if (boolArg) {
			JPanel panel = new JPanel(new BorderLayout());
			JButton refreshButton = new JButton(Icons.getImageIcon("refresh.png"));
			refreshButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					refresh();
				}
			});
			panel.add(refreshButton, BorderLayout.WEST);
			messageText = new JTextField();
			messageText.setEditable(false);
			messageText.setBackground(getBackground());
			messageText.setForeground(Color.blue);
			messageText.setBorder(null);
			panel.add(messageText, BorderLayout.CENTER);
			getRootPane().add(panel, BorderLayout.NORTH);

		} else if (addMessage) {
			messageText = new JTextField();
			messageText.setEditable(false);
			messageText.setBackground(getBackground());
			messageText.setForeground(Color.blue);
			messageText.setBorder(null);
			getRootPane().add(messageText, BorderLayout.NORTH);
		}
		
		contentArea = new JPanel(new BorderLayout());
		textArea = new JTextArea();
		textArea.setEditable(false);
		if (scrolled) {
			textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.getViewport().setView(textArea);
			contentArea.add(scrollPane, BorderLayout.CENTER);
		}
		else
			contentArea.add(textArea, BorderLayout.CENTER);
		getRootPane().add(contentArea, BorderLayout.CENTER);
		if (theMessage != null) {
			messageText.setText(theMessage);
		}
		if (theColor != null) {
			textArea.setForeground(theColor);
		}
		if (theText != null) {
			textArea.setText(theText);
		}
	}

	/**
	 * Set the text to display
	 * 
	 * @param text
	 */
	public void setText(String text) {
		theText = text;
		if (textArea != null) {
			textArea.setText(theText);
			textArea.setCaretPosition(0);
		}
	}

	/**
	 * Append the text to the display
	 * 
	 * @param text
	 */
	public void appendText(String text) {
		if (theText == null)
			theText = "";
		theText += text;
		setText(theText);
	}

	/**
	 * Set the message at the top of the display
	 * 
	 * @param msg
	 */
	public void setMessage(String msg) {
		theMessage = msg;
		if (messageText != null) {
			messageText.setText(theMessage);
		}
	}

	/**
	 * Set the text color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		theColor = color;
		if (textArea != null) {
			textArea.setForeground(color);
		}
	}

	/**
	 * Close the dialog
	 */
	@Override
	public boolean okPressed() {
		return (true);
	}

	/**
	 * Refresh the display (implemented by subclass)
	 */
	public void refresh() {
		if (textArea != null)
			textArea.setCaretPosition(0);
	}
}
