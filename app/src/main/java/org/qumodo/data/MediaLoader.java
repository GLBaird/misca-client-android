package org.qumodo.data;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import org.qumodo.miscaclient.R;

public class MediaLoader {

    private static Context appContext;
    public static void setContext(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static void getUserAvatar(String userID, MediaLoaderListener listener) {
        Bitmap file = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.face);
        listener.imageHasLoaded(userID, file);
    }

    public static void getUserCircularAvatar(String userID, MediaLoaderListener listener) {
        Bitmap file = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.face);
        file = getCircularBitmap(file);
        listener.imageHasLoaded(userID, file);
    }

    public static void getMessageImage(String messageID, MediaLoaderListener listener) {
        Bitmap file = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.face);
        listener.imageHasLoaded(messageID, file);
    }

    public static void getMissingImageIcon(MediaLoaderListener listener) {

    }

}
