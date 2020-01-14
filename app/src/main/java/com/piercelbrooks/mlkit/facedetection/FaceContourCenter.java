package com.piercelbrooks.mlkit.facedetection;

import android.graphics.Point;

public class FaceContourCenter {
    public int type;
    public int points;
    public Point point;

    public FaceContourCenter(int type) {
        this.type = type;
        this.points = 0;
        this.point = new Point(0, 0);
    }
}
