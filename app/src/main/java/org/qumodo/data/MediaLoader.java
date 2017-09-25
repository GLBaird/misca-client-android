package org.qumodo.data;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.miscaclient.fragments.MessageListFragment;
import org.qumodo.network.QMessage;
import org.qumodo.services.QTCPSocketService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;


public class MediaLoader {

    private static Context appContext;
    public static final String TAG = "MediaLoader";

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

        float r;

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

    public static Bitmap rotateBitmap(Bitmap source, float angle, boolean scaleImage)
    {
        Matrix matrix = new Matrix();

        if (source.getWidth() > 1000 && scaleImage) {
            float scale = (float) 1000 / (float) source.getWidth();
            matrix.postScale(scale, scale);
            Log.d(TAG, "Scale "+scale+"  "+source.getWidth()+"  "+source.getHeight());
        }
        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap fixImageOrientation(String filepath, Bitmap image, boolean scaleImage) {
        try {
            ExifInterface exif = new ExifInterface(filepath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            int rotate = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            if (rotate > 0)
                return rotateBitmap(image, rotate, scaleImage);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix matrix = new Matrix();

        if (image.getWidth() > 1000 && scaleImage) {
            float scale = (float) 1000 / (float) image.getWidth();
            matrix.postScale(scale, scale);
            return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        }

        return image;
    }

    public static void getMessageImage(String messageID, Context context, MediaLoaderListener listener) {
        LoadMessageImage worker = new LoadMessageImage(messageID, context, listener);
        worker.execute();
    }

    public static void getMissingImageIcon(MediaLoaderListener listener) {

    }

    public static File getImageFile(String id, String imageStore, Context context) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File dir = cw.getDir(imageStore, Context.MODE_PRIVATE);
        return new File(dir, id + ".jpg");
    }

    public static final String IMAGE_STORE_UPLOADS = "uploads";
    public static final String IMAGE_STORE_AVATARS = "avatars";

    public static String storeImageInMediaStore(Bitmap image, String imageStore, Context context) {
        FileOutputStream fos = null;
        String imageID = UUID.randomUUID().toString();

        try {
            fos = new FileOutputStream(getImageFile(imageID, imageStore, context));
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return imageID;
    }

    public static String storeImageInMediaStore(InputStream is, String imageStore, Context context) {
        FileOutputStream fos = null;
        String imageID = UUID.randomUUID().toString();

        try {
            fos = new FileOutputStream(getImageFile(imageID, imageStore, context));
            byte[] buffer = new byte[4 * 1024]; // or other buffer size
            int read;

            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return imageID;
    }

    public static void uploadImageToServer(String imageID, QMessage imageMessage) {
        String messageText;
        try {
            messageText = imageMessage.serialize();
        } catch (JSONException e) {
            e.printStackTrace();
            messageText = null;
        }
        if (messageText != null) {
            UploadImageToServer uploadTask = new UploadImageToServer(appContext);
            uploadTask.execute(imageID, appContext.getString(R.string.online_image_message_route), messageText);
        } else {
            Toast.makeText(appContext, "Failed to create image message to upload!", Toast.LENGTH_SHORT)
                 .show();
        }
    }

    private static class LoadMessageImage extends AsyncTask<String, String, Bitmap> {

        String messageID;
        Context context;
        MediaLoaderListener listener;

        LoadMessageImage(String messageID, Context context, MediaLoaderListener listener) {
            this.messageID = messageID;
            this.context = context;
            this.listener = listener;
        }

        private Bitmap downloadImageFromServer(String apiRoute, String imageStore) {
            if (messageID != null) {
                try {
                    URL imageStoreURL = getMessageImageURL(messageID, apiRoute);

                    Log.d(TAG, "Downloading from " + imageStoreURL.toString());

                    HttpURLConnection connection = (HttpURLConnection) imageStoreURL.openConnection();
                    connection.setRequestProperty("User-Agent", "MISCA");
                    connection.setRequestProperty("userID", UserSettingsManager.getUserID());
                    connection.setRequestProperty("User-Certificate", UserSettingsManager.getHashedClientPublicKeyString());

                    int response = connection.getResponseCode();

                    if (response != HttpsURLConnection.HTTP_OK) {
                        return null;
                    }

                    InputStream is = connection.getInputStream();
                    if (is != null) {
                        Bitmap downloadedImage = BitmapFactory.decodeStream(is);

                        File outputFile = getImageFile(messageID, imageStore, appContext);
                        FileOutputStream fos = new FileOutputStream(outputFile);
                        downloadedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();

                        return downloadedImage;
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Failed to download image");
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "Failed returning NULL from downloadImageFromServer");
            return null;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            File imageFile = getImageFile(messageID, IMAGE_STORE_UPLOADS, context);
            Bitmap file = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            if (file != null) {
                file = fixImageOrientation(imageFile.getAbsolutePath(), file, true);
            } else {
                Log.d(TAG, "Downloading from server");
                String apiRoute = appContext.getString(R.string.online_image_message_route);
                file = downloadImageFromServer(apiRoute, IMAGE_STORE_UPLOADS);
                if (file != null)
                    file = fixImageOrientation(imageFile.getAbsolutePath(), file, true);
            }

            return file != null ? file : BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_image);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                listener.imageHasLoaded(messageID, bitmap);
            } else {
                listener.imageHasFailedToLoad(messageID);
            }
        }
    }

    private static class UploadImageToServer extends AsyncTask<String, Void, Boolean> {

        private Context context;
        String messageToSend;

        UploadImageToServer(Context context) {
            this.context = context;
        }

        private byte[] loadImageDataAsByteArray(String messageID) {
            Log.d(TAG, "Loading Image Data");
            File imageFile = getImageFile(messageID, IMAGE_STORE_UPLOADS, context);
            Log.d(TAG, "PATH - " + imageFile.getAbsolutePath());
            Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            image = fixImageOrientation(imageFile.getAbsolutePath(), image, false);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            return bos.toByteArray();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String messageID = params[0];
            String apiRoute  = params[1];
            messageToSend    = params[2];

            Log.d(TAG, "Starting image upload " + messageID + ": " + apiRoute);

            if (messageID != null && apiRoute != null) {
                try {
                    URL imageStoreURL = getMessageImageURL(messageID, apiRoute);

                    HttpURLConnection connection = (HttpURLConnection) imageStoreURL.openConnection();
                    connection.setRequestProperty("User-Agent", "MISCA");
                    connection.setRequestProperty("userID", UserSettingsManager.getUserID());
                    connection.setRequestProperty("User-Certificate", UserSettingsManager.getHashedClientPublicKeyString());
                    connection.setRequestProperty("Content-Type", "image/jpeg");
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.connect();

                    BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    out.write(loadImageDataAsByteArray(messageID));
                    out.flush();
                    out.close();

                    int responseCode = connection.getResponseCode();
                    return responseCode == 200;

                } catch (IOException e) {
                    Log.d(TAG, "Failed to upload image");
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(appContext, "Failed to upload image to server", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(appContext, "Image uploaded", Toast.LENGTH_SHORT)
                     .show();
                Intent updateUI = new Intent();
                updateUI.setAction(MessageListFragment.ACTION_IMAGE_ADDED);
                appContext.sendBroadcast(updateUI);

                if (messageToSend != null) {
                    Intent sendImageMessage = new Intent();
                    sendImageMessage.setAction(QTCPSocketService.ACTION_SEND_MESSAGE);
                    sendImageMessage.putExtra(QTCPSocketService.INTENT_KEY_MESSAGE, messageToSend);
                    appContext.sendBroadcast(sendImageMessage);
                }
            }
        }
    }

    @NonNull
    private static URL getMessageImageURL(String messageID, String apiRoute) throws MalformedURLException {
        return new URL(
            appContext.getString(R.string.online_image_hostname)
                + ":" + appContext.getResources().getInteger(R.integer.online_image_store_port)
                + apiRoute + messageID
        );
    }

}
