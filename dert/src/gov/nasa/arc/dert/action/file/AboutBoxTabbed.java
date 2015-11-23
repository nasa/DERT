package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * Dialog that displays DERT About text.
 *
 */
public class AboutBoxTabbed extends AbstractDialog {

	// About string
	private String version;

	/**
	 * Constructor
	 * 
	 * @param vrsn
	 * @param del
	 */
	public AboutBoxTabbed(String version) {
		super(Dert.getMainWindow(), "About DERT", false, false);
		this.version = version;
		width = 650;
		height = 450;
	}

	@Override
	protected void build() {
		getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getRootPane().setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JTextArea area = new JTextArea();
		area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		area.setEditable(false);
		area.setBackground(getRootPane().getBackground());
		area.setFont(area.getFont().deriveFont(Font.PLAIN, 14));
		area.setText("Desktop Exploration of Remote Terrain (DERT), version "+version+"\nIntelligent Systems Division, NASA Ames Research Center");
		panel.add(area, BorderLayout.CENTER);
		getRootPane().add(panel, BorderLayout.NORTH);
		
		contentArea = new GroupPanel("Licenses");
		contentArea.setLayout(new BorderLayout());
		String str = "DERT employs software developed by other open source projects. "+
			"Find complete third party license listings in DERT_ThirdPartyLicenses.pdf.";
		area = new JTextArea();
		area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		area.setEditable(false);
		area.setText(str);
		area.setBackground(contentArea.getBackground());
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		contentArea.add(area, BorderLayout.NORTH);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("DERT", getLicensePanel(DERT_TEXT));
		tabbedPane.addTab("Ardor3D", getLicensePanel(ARDOR3D_TEXT));
		tabbedPane.addTab("JNISpice", getLicensePanel(JNISPICE_TEXT));
		tabbedPane.addTab("JOGL", getLicensePanel(JOGL_TEXT));
		tabbedPane.addTab("LibTIFF", getLicensePanel(TIFF_TEXT));
		tabbedPane.addTab("Proj.4", getLicensePanel(PROJ4_TEXT));
		tabbedPane.addTab("XStream", getLicensePanel(XSTREAM_TEXT));
		contentArea.add(tabbedPane, BorderLayout.CENTER);
		getRootPane().add(contentArea, BorderLayout.CENTER);
	}
	
	private JPanel getLicensePanel(String text) {
		JPanel panel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		area.setText(text);
		area.setCaretPosition(0);
		scrollPane.getViewport().setView(area);
		panel.add(scrollPane);
		return(panel);
	}

	/**
	 * User made a selection.
	 */
	@Override
	public boolean okPressed() {
		return(true);
	}
	
	private static final String DERT_TEXT =
		"TBD.";
	
	private static final String ARDOR3D_TEXT =
		"Ardor3D graphics engine by Ardor Labs (now JogAmp's Ardor3D Continuation)\n"+
		"\n"+
		"Copyright (c) 2008-2012 Ardor Labs, Inc.\n"+
		"\n"+
		"This software is provided 'as-is', without any express or implied\n"+
		"warranty. In no event will the authors be held liable for any damages\n"+
		"arising from the use of this software.\n"+
		"\n"+
		"Permission is granted to anyone to use this software for any purpose,\n"+
		"including commercial applications, and to alter it and redistribute it\n"+
		"freely, subject to the following restrictions:\n"+
		"\n"+
		" 1. The origin of this software must not be misrepresented; you must not\n"+
		"    claim that you wrote the original software. If you use this software\n"+
		"    in a product, an acknowledgment in the product documentation would be\n"+
		"    appreciated but is not required.\n"+
		"\n"+
		" 2. Altered source versions must be plainly marked as such, and must not be\n"+
		"    misrepresented as being the original software.\n"+
		"\n"+
		" 3. This notice may not be removed or altered from any source\n"+
		"    distribution.";
	
	private static final String JOGL_TEXT = 
		"JOGL OpenGL libary by JogAmp\n"+
		"\n"+
		"Copyright 2010 JogAmp Community. All rights reserved.\n"+
		"\n"+
		"Redistribution and use in source and binary forms, with or without modification, are\n"+
		"permitted provided that the following conditions are met:\n"+
		"\n"+
		"1. Redistributions of source code must retain the above copyright notice, this list of\n"+
		"   conditions and the following disclaimer.\n"+
		"\n"+
		"2. Redistributions in binary form must reproduce the above copyright notice, this list\n"+
		"   of conditions and the following disclaimer in the documentation and/or other materials\n"+
		"   provided with the distribution.\n"+
		"\n"+
		"THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED\n"+
		"WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND\n"+
		"FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR\n"+
		"CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR\n"+
		"CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR\n"+
		"SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON\n"+
		"ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING\n"+
		"NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF\n"+
		"ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n"+
		"\n"+
		"The views and conclusions contained in the software and documentation are those of the\n"+
		"authors and should not be interpreted as representing official policies, either expressed\n"+
		"or implied, of JogAmp Community\n";
	
	private static final String JNISPICE_TEXT = 
		"JNISpice by NASA's Navigation and Ancillary Information Facility at JPL\n"+
		"\n"+
		"    Copyright (2003), California Institute of Technology.\n"+
		"    U.S. Government sponsorship acknowledged.\n"+
		"\n"+
		"THIS SOFTWARE AND ANY RELATED MATERIALS WERE CREATED BY THE CALIFORNIA INSTITUTE OF\n"+
		"TECHNOLOGY (CALTECH) UNDER A U.S. GOVERNMENT CONTRACT WITH THE NATIONAL AERONAUTICS\n"+
		"AND SPACE ADMINISTRATION (NASA). THE SOFTWARE IS TECHNOLOGY AND SOFTWARE PUBLICLY\n"+
		"AVAILABLE UNDER U.S. EXPORT LAWS AND IS PROVIDED \"AS-IS\" TO THE RECIPIENT WITHOUT WARRANTY\n"+
		"OF ANY KIND, INCLUDING ANY WARRANTIES OF PERFORMANCE OR MERCHANTABILITY OR FITNESS FOR A\n"+
		"PARTICULAR USE OR PURPOSE (AS SET FORTH IN UNITED STATES UCC§2312-§2313) OR FOR ANY\n"+
		"PURPOSE WHATSOEVER, FOR THE SOFTWARE AND RELATED MATERIALS, HOWEVER USED.\n"+
		"IN NO EVENT SHALL CALTECH, ITS JET PROPULSION LABORATORY, OR NASA BE LIABLE FOR ANY\n"+
		"DAMAGES AND/OR COSTS, INCLUDING, BUT NOT LIMITED TO, INCIDENTAL OR CONSEQUENTIAL DAMAGES\n"+
		"OF ANY KIND, INCLUDING ECONOMIC DAMAGE OR INJURY TO PROPERTY AND LOST PROFITS,\n"+
		"REGARDLESS OF WHETHER CALTECH, JPL, OR NASA BE ADVISED, HAVE REASON TO KNOW, OR, IN FACT,\n"+
		"SHALL KNOW OF THE POSSIBILITY.\n"+
		"RECIPIENT BEARS ALL RISK RELATING TO QUALITY AND PERFORMANCE OF THE SOFTWARE AND ANY\n"+
		"RELATED MATERIALS, AND AGREES TO INDEMNIFY CALTECH AND NASA FOR ALL THIRD-PARTY CLAIMS\n"+
		"RESULTING FROM THE ACTIONS OF RECIPIENT IN THE USE OF THE SOFTWARE\n";
	
	private static final String TIFF_TEXT =
		"LibTIFF by Sam Leffler and Silicon Graphics, Inc.\n"+
		"\n"+
		"    Copyright (c) 1988-1997 Sam Leffler\n"+
		"    Copyright (c) 1991-1997 Silicon Graphics, Inc.\n"+
		"\n"+
		"Permission to use, copy, modify, distribute, and sell this software and\n"+
		"its documentation for any purpose is hereby granted without fee, provided\n"+
		"that (i) the above copyright notices and this permission notice appear in\n"+
		"all copies of the software and related documentation, and (ii) the names of\n"+
		"Sam Leffler and Silicon Graphics may not be used in any advertising or\n"+
		"publicity relating to the software without the specific, prior written\n"+
		"permission of Sam Leffler and Silicon Graphics.\n"+
		"\n"+
		"THE SOFTWARE IS PROVIDED \"AS-IS\" AND WITHOUT WARRANTY OF ANY KIND,\n"+
		"EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY\n"+
		"WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.\n"+
		"\n"+
		"IN NO EVENT SHALL SAM LEFFLER OR SILICON GRAPHICS BE LIABLE FOR\n"+
		"ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,\n"+
		"OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,\n"+
		"WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY THEORY OF\n"+
		"LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE\n"+
		"OF THIS SOFTWARE.\n";
	
	private static final String PROJ4_TEXT = 
		"Proj.4 cartographic projection library by Gerald Evenden and Frank Warmerdam\n"+
		"\n"+
		"    Copyright (c) 2000, Frank Warmerdam\n"+
		"\n"+
		"Permission is hereby granted, free of charge, to any person obtaining a\n"+
		"copy of this software and associated documentation files (the \"Software\"),\n"+
		"to deal in the Software without restriction, including without limitation\n"+
		"the rights to use, copy, modify, merge, publish, distribute, sublicense,\n"+
		"and/or sell copies of the Software, and to permit persons to whom the\n"+
		"Software is furnished to do so, subject to the following conditions:\n"+
		"\n"+
		"The above copyright notice and this permission notice shall be included\n"+
		"in all copies or substantial portions of the Software.\n"+
		"\n"+
		"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS\n"+
		"OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n"+
		"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL\n"+
		"THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n"+
		"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING\n"+
		"FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER\n"+
		"DEALINGS IN THE SOFTWARE.\n";
		
	private static final String XSTREAM_TEXT = 
		"XStream object serialization library by Joe Walnes and the XStream Committers\n"+
		"\n"+
		"    Copyright (c) 2003-2006, Joe Walnes\n"+
		"    Copyright (c) 2006-2009, 2011 XStream Committers\n"+
		"    All rights reserved.\n"+
		"\n"+
		"Redistribution and use in source and binary forms, with or without modification,\n"+
		"are permitted provided that the following conditions are met:\n"+
		"\n"+
		"1. Redistributions of source code must retain the above copyright notice, this list of\n"+
		"   conditions and the following disclaimer.\n"+
		"\n"+
		"2. Redistributions in binary form must reproduce the above copyright notice, this list of\n"+
		"   conditions and the following disclaimer in the documentation and/or other materials provided\n"+
		"   with the distribution.\n"+
		"\n"+
		"3. Neither the name of XStream nor the names of its contributors may be used to endorse\n"+
		"   or promote products derived from this software without specific prior written\n"+
		"   permission.\n"+
		"\n"+
		"THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY\n"+
		"EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"+
		"OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT\n"+
		"SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,\n"+
		"INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED\n"+
		"TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR\n"+
		"BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN\n"+
		"CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY\n"+
		"WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH\n"+
		"DAMAGE.\n";
}
