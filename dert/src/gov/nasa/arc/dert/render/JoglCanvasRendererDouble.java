package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.view.Console;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.CapsUtil;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.math.type.ReadOnlyRectangle2;
import com.ardor3d.renderer.Camera;
import com.jogamp.opengl.GLPipelineFactory;

/**
 * Extends the Ardor3D JoglCanvasRenderer to use a JoglRendererDouble and
 * initialize a BasicScene.
 *
 */
public class JoglCanvasRendererDouble extends JoglCanvasRenderer {

    private static final Logger LOGGER = Logger.getLogger(JoglCanvasRendererDouble.class.getName());
	
	// Fields for frame grab
	private ByteBuffer store;
	private int frameCount;
	private boolean frameGrab;
	private int grabX, grabY, grabWidth, grabHeight;
	private String grabFilePath;
	
	// Fields that are not accessible in super class.
	private boolean _contextDropAndReclaimOnDrawEnabled;
	private boolean _useDebug;
    private boolean _debugEnabled;
    
    private ReadOnlyRectangle2 clipRectangle;

	/**
	 * Constructor
	 * 
	 * @param scene
	 * @param useDebug
	 */
	public JoglCanvasRendererDouble(final Scene scene, boolean useDebug) {
		super(scene, useDebug, new CapsUtil(), true);
		_contextDropAndReclaimOnDrawEnabled = true;
		_useDebug = useDebug;
		
	}

	@Override
	public void init(final DisplaySettings settings, final boolean doSwap) {
		super.init(settings, doSwap);
		makeCurrentContext();
		_renderer = new JoglRendererDouble();
		((BasicScene) _scene).init(this);
		releaseCurrentContext();
	}

    @Override
    @MainThread
    public boolean draw() {

        // set up context for rendering this canvas
        if (_contextDropAndReclaimOnDrawEnabled) {
            makeCurrentContext();
        }

        // Enable Debugging if requested.
        if (_useDebug != _debugEnabled) {
            _context.setGL(GLPipelineFactory.create("javax.media.opengl.Debug", null, _context.getGL(), null));
            _debugEnabled = true;

            LOGGER.info("DebugGL Enabled");
        }

        // render stuff, first apply our camera if we have one
        if (_camera != null) {
            if (Camera.getCurrentCamera() != _camera) {
                _camera.update();
            }
            _camera.apply(_renderer);
        }
        if (clipRectangle != null)
        	_renderer.pushClip(clipRectangle);
        _renderer.clearBuffers(_frameClear);

        final boolean drew = _scene.renderUnto(_renderer);
        _renderer.flushFrame(drew && _doSwap);
        
        if (clipRectangle != null)
        	_renderer.popClip();
        
        if (drew && _doSwap && frameGrab)
        	grabFrame();

        // release the context if we're done (swapped and all)
        if (_doSwap) {
            if (_contextDropAndReclaimOnDrawEnabled) {
                releaseCurrentContext();
            }
        }

        return drew;
    }

	public void enableFrameGrab(String grabFilePath, int grabX, int grabY, int grabWidth, int grabHeight) {
		this.grabFilePath = grabFilePath;
		this.frameGrab = (grabFilePath != null);
		this.grabX = grabX;
		this.grabY = grabY;
		this.grabWidth = grabWidth;
		this.grabHeight = grabHeight;
		if (!frameGrab) {
			if (store != null) {
				Console.getInstance().println(frameCount+" frames");
				store = null;
				System.gc();
			}
		}
	}
	
	public void grabFrame() {
		makeCurrentContext();
		if (store == null) {
			Console.getInstance().println("Rendering image sequence to "+grabFilePath);
			frameCount = 0;
			int n = _renderer.getExpectedBufferSizeToGrabScreenContents(ImageDataFormat.RGBA, PixelDataType.Byte, grabWidth, grabHeight);
			store = ByteBuffer.allocateDirect(n);
			store.limit(n);
		}
		
		try {
			store.position(0);
			_renderer.finishGraphics();
			_renderer.grabScreenContents(store, ImageDataFormat.RGBA, PixelDataType.UnsignedByte, grabX, grabY, grabWidth, grabHeight);
			if (Dert.isMac) {
				ImageUtil.doSwap(store);
			}
			ImageUtil.doFlip(store, grabWidth * 4, grabHeight);
			BufferedImage bImage = new BufferedImage(grabWidth, grabHeight, BufferedImage.TYPE_4BYTE_ABGR);
			byte[] iData = ((DataBufferByte) bImage.getRaster().getDataBuffer()).getData();
			store.get(iData, 0, iData.length);
			String filePath = "frame"+String.format("%07d", frameCount)+".png";
			File file = new File(grabFilePath);
			if (!file.exists())
				file.mkdirs();
			file = new File(file, filePath);
			ImageOutputStream oStream = new FileImageOutputStream(file);
			ImageIO.write(bImage, "PNG", oStream);
			oStream.flush();
			oStream.close();
			frameCount ++;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		releaseCurrentContext();
	}
	
	public void setClipRectangle(ReadOnlyRectangle2 clipRect) {
		clipRectangle = clipRect;
	}

}
