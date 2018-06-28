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
 
Tile Rendering Library - Brain Paul 
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

package gov.nasa.arc.dert.scenegraph.text;

import gov.nasa.arc.dert.camera.BasicCamera;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.MeshData;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;

/**
 * A class for drawing text strings that uses GLUT bitmap string function.
 *
 */
public class BitmapText extends Text {

	public static BitmapFont DEFAULT_FONT;
	
//	public static int font = GLUT.BITMAP_HELVETICA_18;
//	public static int fontHeight = 18;

	// A glut instance for rendering
//	protected static GLUT glut;

	private final Vector3 look = new Vector3();
	private final Vector3 left = new Vector3();
	private final Matrix3 rot = new Matrix3();
//	private boolean autoHide = false, doHide;
	private Vector3 location = new Vector3();
//	private double oldScale;
	
	private boolean scalable = true;
	
	protected double glutWidth;
	
	protected BitmapFont font;
	
//	static {
//		glut = new GLUT();		
//	}

	/**
	 * Constructor that defaults to left alignment
	 * 
	 * @param name
	 * @param textString
	 */
	public BitmapText(String name, BitmapFont font, String textString) {
		this(name, font, textString, AlignType.Left, true);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param textString
	 * @param alignment
	 */
	public BitmapText(String name, BitmapFont font, String textString, AlignType alignment, boolean scalable) {
		super(name, textString, alignment);
		this.font = font;
		this.scalable = scalable;
		initialize();
	}

	/**
	 * Set the font size
	 * 
	 * @param size
	 */
//	public static void setFont(int size) {
//		switch (size) {
//		case 8:
//			font = GLUT.BITMAP_8_BY_13;
//			fontHeight = 8;
//			break;
//		case 9:
//			font = GLUT.BITMAP_9_BY_15;
//			fontHeight = 9;
//			break;
//		case 10:
//			font = GLUT.BITMAP_HELVETICA_10;
//			fontHeight = 10;
//			break;
//		case 12:
//			font = GLUT.BITMAP_HELVETICA_12;
//			fontHeight = 12;
//			break;
//		case 18:
//			font = GLUT.BITMAP_HELVETICA_18;
//			fontHeight = 18;
//			break;
//		case 24:
//			font = GLUT.BITMAP_TIMES_ROMAN_24;
//			fontHeight = 24;
//			break;
//		default:
//			font = GLUT.BITMAP_HELVETICA_18;
//			fontHeight = 18;
//			break;
//		}
//	}
	
	public double getHeight() {
		return(font.getHeight());
	}

	@Override
	protected double getTextWidth() {
//		System.err.println("RasterText.getWidth "+str+" "+scaleFactor+" "+glut.glutBitmapLength(font, str)+" "+(scaleFactor * glut.glutBitmapLength(font, str)));
//		return (glut.glutBitmapLength(font, textString));
		return (font.stringLength(textString));
	}

	@Override
	protected double getTextHeight() {
//		return (fontHeight);
		return (font.getHeight());
	}

	@Override
	protected void renderArrays(final Renderer renderer, final MeshData meshData, final int primcount,
		final ContextCapabilities caps) {
		if (textString.isEmpty()) {
			return;
		}

		// Use arrays
		if (caps.isVBOSupported()) {
			renderer.unbindVBO();
		}

		final GL2 gl2 = GLContext.getCurrentGL().getGL2();
		gl2.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		gl2.glRasterPos3d(position.getX(), position.getY(), position.getZ());
//		glut.glutBitmapString(font, textString);
		font.drawString(textString);
	}

	@Override
	protected void renderVBO(final Renderer r, final MeshData meshData, final int primcount) {
		if (!textString.isEmpty()) {
			final GL2 gl2 = GLContext.getCurrentGL().getGL2();
			gl2.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
			gl2.glRasterPos3d(position.getX(), position.getY(), position.getZ());
//			glut.glutBitmapString(font, textString);
			font.drawString(textString);
		}
	}

	@Override
	public void draw(final Renderer r) {
//		if (doHide)
//			return;
		update((BasicCamera)ContextManager.getCurrentContext().getCurrentCamera());
		if (scalable)
			_worldTransform.setRotation(rot);
		_worldTransform.setScale(_localTransform.getScale());
		super.draw(r);
	}

	/**
	 * Update position according to camera location.
	 * 
	 * @param camera
	 */
	private void update(BasicCamera camera) {
		left.set(camera.getLeft()).negateLocal();
		look.set(camera.getDirection()).negateLocal();
		rot.fromAxes(left, camera.getUp(), look);

		location.set(camera.getLocation());
		location.negateLocal().addLocal(_worldTransform.getTranslation());
		double z = camera.getDirection().dot(location);
		if ((z < camera.getFrustumNear()) || (z > camera.getFrustumFar())) {
			return;
		}

		double hZ; // height of the screen in world coordinates at Z
		double ft = camera.getFrustumTop()/camera.getMagnification();
		if (camera.getProjectionMode() == ProjectionMode.Parallel) {
			hZ = ft;
		} else {
			hZ = z * ft / camera.getFrustumNear();
		}

		double screenScale = 2 * hZ / camera.getHeight(); // maintain uniform
															// size in screen
															// coords

//		if (autoHide) {
//			doHide = (1 / screenScale < 0.5);
//		}
//		double scale = screenScale * scaleFactor;
//		if (scalable && (Math.abs(scale - oldScale) > 0.00001)) {
		if (scalable) {
			setScaleFactor(screenScale);
//			oldScale = scale;
		}
	}
}
