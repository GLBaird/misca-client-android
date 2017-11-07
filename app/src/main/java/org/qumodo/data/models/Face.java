package org.qumodo.data.models;

import android.graphics.Rect;

public class Face {

    Rect position;
    Double confidence;

    Face (double confidence, int x, int y, int w, int h) {
        this.confidence = confidence;
        position = new Rect(x, y, x + w, y + h);
    }

    Face (double confidence, Rect position) {
        this.confidence = confidence;
        this.position = position;
    }

}
