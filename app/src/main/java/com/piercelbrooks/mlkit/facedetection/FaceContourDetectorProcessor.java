package com.piercelbrooks.mlkit.facedetection;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.piercelbrooks.mlkit.common.CameraImageGraphic;
import com.piercelbrooks.mlkit.common.FrameMetadata;
import com.piercelbrooks.mlkit.common.GraphicOverlay;
import com.piercelbrooks.mlkit.VisionProcessorBase;

import java.io.IOException;
import java.util.List;

/**
 * Face Contour Demo.
 */
public class FaceContourDetectorProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceContourDetectorProc";

    private final FirebaseVisionFaceDetector detector;
    private final FaceContourDetectorProcessorListener listener;

    public FaceContourDetectorProcessor(FaceContourDetectorProcessorListener listener) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();

        this.detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        this.listener = listener;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Contour Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            Bitmap originalCameraImage,
            List<FirebaseVisionFace> faces,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay) {
        if (graphicOverlay != null) {
            graphicOverlay.clear();
            /*if (originalCameraImage != null) {
                CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
                graphicOverlay.add(imageGraphic);
            }*/
            for (int i = 0; i < faces.size(); ++i) {
                FirebaseVisionFace face = faces.get(i);
                FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face);
                graphicOverlay.add(faceGraphic);
            }
            //graphicOverlay.postInvalidate();
        }
        if (listener != null) {
            listener.onFrame(originalCameraImage, faces, frameMetadata, graphicOverlay);
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}
