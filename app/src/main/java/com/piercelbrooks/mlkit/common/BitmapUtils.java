package com.piercelbrooks.mlkit.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.CameraInfo;
import androidx.annotation.Nullable;
import android.util.Log;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/** Utils functions for bitmap conversions. */
public class BitmapUtils {

    // Convert NV21 format byte buffer to bitmap.
    @Nullable
    public static Bitmap getBitmap(ByteBuffer data, FrameMetadata metadata) {
        byte[] bytes = data.array();
        if (bytes.length < 4) {
            return null;
        }
        int alpha = 255;
        int[] colors = new int[bytes.length/4];
        for (int i = 0; i < bytes.length - 3; i += 4) {
            colors[i / 4] = (alpha << 24) | (bytes[i] << 16) | (bytes[i + 1] << 8) | bytes[i + 2];
        }
        return Bitmap.createBitmap(colors, metadata.getWidth(), metadata.getHeight(), Bitmap.Config.ARGB_8888);
    }

    // Rotates a bitmap if it is converted from a bytebuffer.
    private static Bitmap rotateBitmap(Bitmap bitmap, int rotation, int facing) {
        Matrix matrix = new Matrix();
        int rotationDegree = 0;
        switch (rotation) {
            case FirebaseVisionImageMetadata.ROTATION_90:
                rotationDegree = 90;
                break;
            case FirebaseVisionImageMetadata.ROTATION_180:
                rotationDegree = 180;
                break;
            case FirebaseVisionImageMetadata.ROTATION_270:
                rotationDegree = 270;
                break;
            default:
                break;
        }

        // Rotate the image back to straight.}
        matrix.postRotate(rotationDegree);
        if (facing == CameraInfo.CAMERA_FACING_BACK) {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            // Mirror the image along X axis for front-facing camera image.
            matrix.postScale(-1.0f, 1.0f);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
    }
}

