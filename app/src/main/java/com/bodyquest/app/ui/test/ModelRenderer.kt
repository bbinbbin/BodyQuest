package com.bodyquest.app.ui.test

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ModelRenderer(private val model: ObjModel) : GLSurfaceView.Renderer {

    @Volatile var rotX = 0f
    @Volatile var rotY = 0f

    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotMatrix  = FloatArray(16)
    private val normMatrix = FloatArray(16)
    private val mvMatrix   = FloatArray(16)
    private val mvpMatrix  = FloatArray(16)
    private val tmp        = FloatArray(16)

    private var program = 0
    private lateinit var vertexBuffer: FloatBuffer

    private val VS = """
        uniform mat4 uMVP;
        uniform mat4 uRot;
        attribute vec4 aPos;
        attribute vec3 aNorm;
        varying vec3 vNorm;
        void main() {
            vNorm = normalize(mat3(uRot) * aNorm);
            gl_Position = uMVP * aPos;
        }
    """.trimIndent()

    // Two-sided diffuse + soft ambient + subtle rim
    private val FS = """
        precision mediump float;
        varying vec3 vNorm;
        uniform vec3 uColor;
        void main() {
            vec3 L = normalize(vec3(0.5, 1.0, 0.8));
            float front = max(dot(vNorm, L), 0.0);
            float back  = max(dot(-vNorm, L), 0.0) * 0.25;
            float lum   = 0.2 + front * 0.75 + back;
            gl_FragColor = vec4(uColor * lum, 1.0);
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.08f, 0.08f, 0.14f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        program = buildProgram(VS, FS)

        val bb = ByteBuffer
            .allocateDirect(model.vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer().apply {
            put(model.vertexData)
            position(0)
        }

        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 4f,   // eye
            0f, 0f, 0f,   // center
            0f, 1f, 0f    // up
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projMatrix, 0, 50f, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Rotation matrix (applied to both vertices and normals)
        Matrix.setIdentityM(rotMatrix, 0)
        Matrix.rotateM(rotMatrix, 0, rotY, 0f, 1f, 0f)
        Matrix.rotateM(rotMatrix, 0, rotX, 1f, 0f, 0f)

        // Normalization: scale(S) then translate(-center*S)  →  scale * (v - center)
        val s = model.scale
        Matrix.setIdentityM(normMatrix, 0)
        Matrix.translateM(normMatrix, 0,
            -model.center[0] * s, -model.center[1] * s, -model.center[2] * s)
        Matrix.scaleM(normMatrix, 0, s, s, s)

        // Final model matrix: rot * norm
        Matrix.multiplyMM(tmp, 0, rotMatrix, 0, normMatrix, 0)

        // MVP = proj * view * model
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, tmp, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvMatrix, 0)

        GLES20.glUseProgram(program)

        GLES20.glUniformMatrix4fv(loc("uMVP"), 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(loc("uRot"), 1, false, rotMatrix, 0)
        GLES20.glUniform3f(loc("uColor"), 0.78f, 0.58f, 0.40f)  // warm brown

        val stride = 6 * 4  // 6 floats × 4 bytes
        val aPos  = GLES20.glGetAttribLocation(program, "aPos")
        val aNorm = GLES20.glGetAttribLocation(program, "aNorm")

        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glEnableVertexAttribArray(aNorm)

        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer)
        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(aNorm, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, model.vertexCount)

        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aNorm)
    }

    private fun loc(name: String) = GLES20.glGetUniformLocation(program, name)

    private fun buildProgram(vs: String, fs: String): Int {
        val v = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also {
            GLES20.glShaderSource(it, vs)
            GLES20.glCompileShader(it)
        }
        val f = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also {
            GLES20.glShaderSource(it, fs)
            GLES20.glCompileShader(it)
        }
        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, v)
            GLES20.glAttachShader(it, f)
            GLES20.glLinkProgram(it)
        }
    }
}
