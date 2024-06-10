package com.rnposedetection.facedetectorframeprocessor

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.io.Closeable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PoseDetectorProcessor(
    context: Context,
    private val runClassification: Boolean = false,
    isStreamMode: Boolean = false
) : Closeable {

    private val detector: PoseDetector
    private val classificationExecutor: Executor
//    private val poseClassifierProcessor: PoseClassifierProcessor

    init {
        classificationExecutor = Executors.newSingleThreadExecutor()
        val options = PoseDetectorOptions.Builder()
            .setExecutor(classificationExecutor)
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        detector = PoseDetection.getClient(options)
//        poseClassifierProcessor = PoseClassifierProcessor(context, isStreamMode)

    }

    override fun close() {
        detector.close()
    }

    fun isMlImageEnabled(): Boolean {
        return true
    }


}
