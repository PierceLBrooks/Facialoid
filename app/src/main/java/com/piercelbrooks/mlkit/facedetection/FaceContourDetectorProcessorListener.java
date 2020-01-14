package com.piercelbrooks.mlkit.facedetection;

import android.graphics.Bitmap;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.piercelbrooks.mlkit.common.FrameMetadata;
import com.piercelbrooks.mlkit.common.GraphicOverlay;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface FaceContourDetectorProcessorListener {
    public void onFrame(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);
}
