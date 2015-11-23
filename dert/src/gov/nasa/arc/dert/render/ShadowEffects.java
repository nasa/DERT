package gov.nasa.arc.dert.render;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.GLSLShaderDataLogic;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.scenegraph.Mesh;

/**
 * Fragment shader program for objects other than the landscape. Layers are not
 * involved here.
 *
 */
public class ShadowEffects extends GLSLShaderObjectsState {

	private static String shadowProg =

	"uniform sampler2DShadow shadowUnit;\n" + "uniform sampler2D imageUnit;\n" + "uniform bool hasTexture;\n"
		+ "uniform bool shadowEnabled;\n" +

		"void main() {\n" +

		"	float shadeFactor = 1.0;\n" +

		"    if (shadowEnabled) {\n" + "    	shadeFactor = shadow2DProj(shadowUnit, gl_TexCoord[7]).x;\n"
		+ "    	shadeFactor = (shadeFactor < 1.0) ? 0.5 : 1.0;\n" + "    }\n" +

		"    if (hasTexture) {\n" + "	// Use texture color\n"
		+ "    	vec4 color = texture2D(imageUnit, gl_TexCoord[0].xy);\n"
		+ "    	gl_FragColor = vec4(shadeFactor*color.rgb*gl_Color.rgb, color.a);\n" + "    }\n" + "    else\n"
		+ "	// Use vertex color\n" + "	    gl_FragColor = vec4(shadeFactor*gl_Color.rgb, gl_Color.a);\n" +

		"}\n";

	// shadow is visible
	public boolean shadowEnabled;

	// this fragment has texture
	public boolean hasTexture;

	// texture unit
	public int imageUnit;

	/**
	 * Constructor
	 */
	public ShadowEffects() {

		InputStream iStream = null;
		try {
			iStream = new ByteArrayInputStream(shadowProg.getBytes());
			setFragmentShader(iStream, "frag");
			setShaderDataLogic(new GLSLShaderDataLogic() {
				@Override
				public void applyData(GLSLShaderObjectsState shader, Mesh mesh, Renderer renderer) {
					RenderState rState = mesh.getLocalRenderState(StateType.Texture);
					hasTexture = ((rState != null) && rState.isEnabled());
					setUniform("shadowUnit", ShadowMap.SHADOW_MAP_UNIT);
					setUniform("imageUnit", imageUnit);
					setUniform("hasTexture", hasTexture);
					setUniform("shadowEnabled", shadowEnabled);
				}
			});
		} catch (final IOException ex) {
			ex.printStackTrace();
		} finally {
			if (iStream != null) {
				try {
					iStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}