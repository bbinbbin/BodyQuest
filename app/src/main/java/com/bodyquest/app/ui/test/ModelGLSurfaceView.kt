package com.bodyquest.app.ui.test

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class ModelGLSurfaceView(
    context: Context,
    private val renderer: ModelRenderer
) : GLSurfaceView(context) {

    private var lastX = 0f
    private var lastY = 0f

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val dy = event.y - lastY
                renderer.rotY += dx * 0.4f
                renderer.rotX = (renderer.rotX + dy * 0.4f).coerceIn(-89f, 89f)
                lastX = event.x
                lastY = event.y
            }
        }
        return true
    }
}
