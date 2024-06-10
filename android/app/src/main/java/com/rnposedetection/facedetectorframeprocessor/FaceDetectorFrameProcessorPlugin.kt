package com.rnposedetection.facedetectorframeprocessor

import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessors.VisionCameraProxy
import com.rnposedetection.views.PoseOverlayManager
import com.rnposedetection.views.graphic.GraphicOverlay
import com.rnposedetection.views.graphic.PoseGraphic
import java.util.concurrent.Executors

data class FrameInfo(
    val rotationDegree: Int,
    val width: Int,
    val height: Int,
    val frameTime: Long,
) {
    val correctedWidth = if (rotationDegree == 0 || rotationDegree == 180) width else height
    val correctedHeight = if (rotationDegree == 0 || rotationDegree == 180) height else width
    val isFlipped = rotationDegree == 180 || rotationDegree == 270
}

class FaceDetectorFrameProcessorPlugin(proxy: VisionCameraProxy, options: Map<String, Any>?) :
    FrameProcessorPlugin() {
    private var poseDetector: PoseDetector
    private var classificationExecutor = Executors.newSingleThreadExecutor()

    init {
        val poseDetectorOptions = PoseDetectorOptions.Builder()
            .setExecutor(classificationExecutor)
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .setPreferredHardwareConfigs(PoseDetectorOptions.CPU)
            .build()
         poseDetector = PoseDetection.getClient(poseDetectorOptions)
    }

    override fun callback(frame: Frame, arguments: Map<String, Any>?): Any? {
        try {
            val mediaImage = frame.image
            val image = InputImage.fromMediaImage(mediaImage, 270)
            val task = poseDetector.process(image)
            val pose = Tasks.await(task)
            val frameInfo = FrameInfo(
                rotationDegree = 270,
                width = frame.width,
                height = frame.height,
                frameTime = frame.image.timestamp
            )
            if (pose.allPoseLandmarks.isNotEmpty()) {
                PoseOverlayManager.current()?.apply {
                    setImageSourceInfoIfNeed(frameInfo)
                    clear()
                    val graphic = PoseGraphic(
                        overlay = this,
                        pose = pose,
                        visualizeZ = true,
                        rescaleZForVisualization = false,
                    )
                    add(graphic)
                    postInvalidate()
                }
            }
            return null
        } catch (e: Exception) {
            throw Exception("Error processing pose detection: $e")
        }
    }

    private fun GraphicOverlay.setImageSourceInfoIfNeed(info: FrameInfo) {
        val newWidth = info.correctedWidth
        val newHeight = info.correctedHeight
        if (imageWidth != newWidth || imageHeight != newHeight || isImageFlipped != info.isFlipped) {
            setImageSourceInfo(
                newWidth,
                newHeight,
                info.isFlipped
            )
        }
    }
}