package com.piercelbrooks.mlkit.facedetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.piercelbrooks.mlkit.common.GraphicOverlay;

public class FaceContourCenterGraphic extends GraphicOverlay.Graphic {
    private float x;
    private float y;
    private float radius;

    private Paint paint;

    public FaceContourCenterGraphic(GraphicOverlay overlay, float x, float y, float radius, int color) {
        super(overlay);

        paint = new Paint();
        paint.setColor(color);

        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(translateX(x), translateY(y), radius, paint);
    }

    public static int getColor(int index) {
        switch (index) {
            case FirebaseVisionFaceContour.FACE:
                return Color.RED;
            case FirebaseVisionFaceContour.LEFT_EYE:
                return Color.GREEN;
            case FirebaseVisionFaceContour.RIGHT_EYE:
                return Color.BLUE;
        }
        return Color.BLACK;
    }
}
