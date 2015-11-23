package gov.nasa.arc.dert.render;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.GLSLShaderDataLogic;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.scenegraph.Mesh;

/**
 * Provides a shader that uses a texture map as a color look up table (LUT).
 * Generates the shader program on the fly.
 *
 */
public class ColorTableEffects extends GLSLShaderObjectsState {

	protected static final String program = "#version 120\n" + "uniform sampler2D texture;\n"
		+ "uniform sampler1D colorTable;\n" + "\n" + "void main() {\n"
		+ "	vec4 col = texture2D(texture, gl_TexCoord[0].xy);\n" + "	if (col.r == 0.0)\n"
		+ "		gl_FragColor = gl_Color;\n" + "	else {\n" + "		float index = ((col.r*255)-1)/254;\n"
		+ "		gl_FragColor = texture1D(colorTable, index);\n" + "	}\n" + "}\n";

	// texture units
	private int textureUnit, colorTableUnit;

	public ColorTableEffects(int tUnit, int cTUnit) {
		this.textureUnit = tUnit;
		this.colorTableUnit = cTUnit;

		// load the shader program and set the texture units
		InputStream iStream = null;
		try {
			iStream = new ByteArrayInputStream(program.getBytes());
			setFragmentShader(iStream, "frag");
			iStream.close();
			setUniform("texture", textureUnit);
			setUniform("colorTable", colorTableUnit);
			setShaderDataLogic(new GLSLShaderDataLogic() {
				@Override
				public void applyData(GLSLShaderObjectsState shader, Mesh mesh, Renderer renderer) {
					setUniform("texture", textureUnit);
					setUniform("colorTable", colorTableUnit);
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