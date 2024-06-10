package com.rnposedetection.views.graphic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View

class GraphicOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val lock = Any()
    private val graphics: MutableList<Graphic> = ArrayList()

    // Matrix for transforming from image coordinates to overlay view coordinates.
    private val transformationMatrix = Matrix()
    var imageWidth = 0
        private set
    var imageHeight = 0
        private set

    // The factor of overlay View size to image size. Anything in the image coordinates need to be
    // scaled by this amount to fit with the area of overlay View.
    private var scaleFactor = 1.0f

    // The number of horizontal pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private var postScaleWidthOffset = 0f

    // The number of vertical pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private var postScaleHeightOffset = 0f
    var isImageFlipped = false
        private set
    private var needUpdateTransformation = true
    var strokeSizeHint = 0f
        private set

    /**
     * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
     * this and implement the [Graphic.draw] method to define the graphics element. Add
     * instances to the overlay using [GraphicOverlay.add].
     */
    abstract class Graphic(private val overlay: GraphicOverlay) {
        /**
         * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
         * to view coordinates for the graphics that are drawn:
         *
         *
         *  1. [Graphic.scale] adjusts the size of the supplied value from the image
         * scale to the view scale.
         *  1. [Graphic.translateX] and [Graphic.translateY] adjust the
         * coordinate from the image's coordinate system to the view coordinate system.
         *
         *
         * @param canvas drawing canvas
         */
        abstract fun draw(canvas: Canvas)

        /**
         * Adjusts the supplied value from the image scale to the view scale.
         */
        fun scale(imagePixel: Float): Float {
            return imagePixel * overlay.scaleFactor
        }

        val applicationContext: Context
            /**
             * Returns the application context of the app.
             */
            get() = overlay.context.applicationContext

        fun isImageFlipped(): Boolean {
            return overlay.isImageFlipped
        }

        fun setIsImageFlipped(isFlipped: Boolean) {
            overlay.isImageFlipped = isFlipped
        }

        /**
         * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
         */
        fun translateX(x: Float): Float {
            return if (overlay.isImageFlipped) {
                overlay.width - (scale(x) - overlay.postScaleWidthOffset)
            } else {
                scale(x) - overlay.postScaleWidthOffset
            }
        }

        /**
         * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
         */
        fun translateY(y: Float): Float {
            return scale(y) - overlay.postScaleHeightOffset
        }

        val overlayMidWidth: Int
            /**
             * Get width / middle X
             */
            get() = overlay.width / 2

        val overlayMidHeight: Int
            /**
             * Get height / middle Y
             */
            get() = overlay.height / 2

        /**
         * Returns a [Matrix] for transforming from image coordinates to overlay view coordinates.
         */
        fun getTransformationMatrix(): Matrix {
            return overlay.transformationMatrix
        }

        fun postInvalidate() {
            overlay.postInvalidate()
        }

        fun getStrokeSizeHint(): Float {
            return overlay.strokeSizeHint
        }
    }

    init {
        addOnLayoutChangeListener { view: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            needUpdateTransformation = true
        }
        setStrokeSizeHintDp(3)
    }

    fun setStrokeSizeHintDp(sizeHintDp: Int) {
        val context = context
        val sizeHintPx = sizeHintDp * context.resources.displayMetrics.density
        if (strokeSizeHint != sizeHintPx) {
            strokeSizeHint = sizeHintPx
            invalidate()
        }
    }

    /**
     * Removes all graphics from the overlay.
     */
    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    /**
     * Adds a graphic to the overlay.
     */
    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
    }

    /**
     * Removes a graphic from the overlay.
     */
    fun remove(graphic: Graphic) {
        synchronized(lock) {
            graphics.remove(graphic)
        }
        postInvalidate()
    }

    /**
     * Sets the source information of the image being processed by detectors, including size and
     * whether it is flipped, which informs how to transform image coordinates later.
     *
     * @param imageWidth  the width of the image sent to ML Kit detectors
     * @param imageHeight the height of the image sent to ML Kit detectors
     * @param isFlipped   whether the image is flipped. Should set it to true when the image is from the
     * front camera.
     */
    fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
        require(imageWidth > 0) { "image width must be positive" }
        require(imageHeight > 0) { "image height must be positive" }
        synchronized(lock) {
            this.imageWidth = imageWidth
            this.imageHeight = imageHeight
            isImageFlipped = isFlipped
            needUpdateTransformation = true
        }
        postInvalidate()
    }

    private fun updateTransformationIfNeeded() {
        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
            return
        }
        val viewAspectRatio = width.toFloat() / height
        val imageAspectRatio = imageWidth.toFloat() / imageHeight
        postScaleWidthOffset = 0f
        postScaleHeightOffset = 0f
        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = width.toFloat() / imageWidth
            postScaleHeightOffset = (width.toFloat() / imageAspectRatio - height) / 2
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = height.toFloat() / imageHeight
            postScaleWidthOffset = (height.toFloat() * imageAspectRatio - width) / 2
        }
        transformationMatrix.reset()
        transformationMatrix.setScale(scaleFactor, scaleFactor)
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)
        if (isImageFlipped) {
            transformationMatrix.postScale(-1f, 1f, width / 2f, height / 2f)
        }
        needUpdateTransformation = false
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            updateTransformationIfNeeded()
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }
}
