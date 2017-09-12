package org.qumodo.data;

import android.graphics.Bitmap;

public interface MediaLoaderListener {

    void imageHasLoaded(String ref, Bitmap image);
    void imageHasFailedToLoad(String ref);

}
