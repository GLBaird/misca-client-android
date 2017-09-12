package org.qumodo.data;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.qumodo.miscaclient.R;

public class MediaLoader {

    static Context appContext;

    public static void getUserAvatar(String userID, MediaLoaderListener listener) {
        Bitmap file = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.face);
        listener.imageHasLoaded(userID, file);
    }

    public static void getMessageImage(String messageID, MediaLoaderListener listener) {
        Bitmap file = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.face);
        listener.imageHasLoaded(messageID, file);
    }

    public static void getMissingImageIcon(MediaLoaderListener listener) {

    }

}
