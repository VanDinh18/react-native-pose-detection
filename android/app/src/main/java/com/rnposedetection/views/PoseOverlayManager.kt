package com.rnposedetection.views

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.rnposedetection.views.graphic.GraphicOverlay

class PoseOverlayManager : SimpleViewManager<GraphicOverlay>() {
    companion object {
        const val REACT_CLASS = "PoseGraphicOverlay"
        private var graphicOverlay: GraphicOverlay? = null

        fun current(): GraphicOverlay? {
            return graphicOverlay
        }
    }

    override fun getName(): String {
        return REACT_CLASS
    }

    override fun createViewInstance(ctx: ThemedReactContext): GraphicOverlay {
        val overlay = GraphicOverlay(ctx, null)
        graphicOverlay = overlay
        return overlay
    }

    override fun onDropViewInstance(view: GraphicOverlay) {
        if (graphicOverlay == view) {
            graphicOverlay = null
        }
        super.onDropViewInstance(view)
    }

    @ReactProp(name = "strokeSize")
    fun setStrokeSize(view: GraphicOverlay, strokeSize: Int) {
        view.setStrokeSizeHintDp(strokeSize)
    }
}
