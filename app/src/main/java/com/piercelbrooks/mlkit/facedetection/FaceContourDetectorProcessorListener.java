package com.piercelbrooks.mlkit.facedetection;

import android.graphics.Bitmap;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.piercelbrooks.mlkit.common.FrameMetadata;
import com.piercelbrooks.mlkit.common.GraphicOverlay;

import java.util.List;

public interface FaceContourDetectorProcessorListener {
    public void onFrame(
            Bitmap originalCameraImage,
            List<FirebaseVisionFace> faces,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay);
}
