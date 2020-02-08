package com.google.mediapipe.components;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;

public interface CameraXPreviewHelperListener {
    public abstract void onAnalyze(FirebaseVisionImage image, int rotation, int width, int height);
}
