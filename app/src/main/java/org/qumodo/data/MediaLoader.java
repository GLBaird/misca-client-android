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
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.ServerDetails;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.miscaclient.fragments.MessageListFragment;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;
import org.qumodo.services.QTCPSocketService;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

    private static double scale;

    public static void imageSearch(Uri imageURI) {
        UploadImageToServer task = new UploadImageToServer(appContext);
        task.imageURI = imageURI;
        task.execute();
    }

    public static void getUserAvatar(String userID, MediaLoaderListener listener) {
        Bitmap file = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.face);
        listener.imageHasLoaded(userID, file, scale);
    }

    public static void getUserCircularAvatar(final String userID, final MediaLoaderListener listener) {
        LoadMessageImage worker = new LoadMessageImage(userID, appContext, new MediaLoaderListener() {
            @Override
            public void imageHasLoaded(String ref, Bitmap image, double scale) {
                image = getCircularBitmap(image);
                listener.imageHasLoaded(ref, image, scale);
            }

            @Override
            public void imageHasFailedToLoad(String ref) {
                Bitmap file = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.face);
                file = getCircularBitmap(file);
                listener.imageHasLoaded(userID, file, scale);
            }
        });
        worker.imageStore = IMAGE_STORE_AVATARS;
        worker.execute();

    }

    public static Bitmap rotateBitmap(Bitmap source, float angle, boolean scaleImage)
    {
        Matrix matrix = new Matrix();

        if (source.getWidth() > 1000 && scaleImage) {
            float scale = (float) 1000 / (float) source.getWidth();
            matrix.postScale(scale, scale);
            MediaLoader.scale = scale;
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

    public static void getCoreImage(String imageID, String imagePath, String thumbSize,
                                    Context context, MediaLoaderListener listener) {
        LoadMessageImage worker = new LoadMessageImage(imageID, imagePath, thumbSize, context, listener);
        worker.execute();
    }

    public static void getMissingImageIcon(MediaLoaderListener listener) {

    }

    public static File getImageFile(String id, String imageStore, Context context) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File dir = cw.getDir(imageStore, Context.MODE_PRIVATE);
        return new File(dir, id + ".jpg");
    }

    public static Uri getImageURI(String id, String imageStore, Context context) {
        return Uri.fromFile(getImageFile(id, imageStore, context));
    }

    public static final String IMAGE_STORE_UPLOADS = "uploads";
    public static final String IMAGE_STORE_AVATARS = "avatars";
    public static final String IMAGE_STORE_CORE_IMAGE = "core_image";
    public static final String IMAGE_STORE_CORE_IMAGE_THUMB = "core_image_thumb";

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
        String imageID;
        String coreImagePath;
        String thumbSize;
        String messageID;
        Context context;
        String imageStore = IMAGE_STORE_UPLOADS;
        MediaLoaderListener listener;

        LoadMessageImage(String messageID, Context context, MediaLoaderListener listener) {
            this.messageID = messageID;
            this.context = context;
            this.listener = listener;
        }

        LoadMessageImage(String imageID, String path, String thumbSize,
                         Context context, MediaLoaderListener listener) {
            this.imageID = imageID;
            this.coreImagePath = path;
            this.thumbSize = thumbSize;
            this.context = context;
            this.listener = listener;
        }

        private Bitmap downloadImageFromServer(String apiRoute, String imageStore) {
            if (messageID != null || coreImagePath != null) {
                try {

                    URL imageStoreURL;
                    if (imageStore.equals(MediaLoader.IMAGE_STORE_AVATARS))
                        imageStoreURL = getMessageImageURL(messageID, appContext.getResources().getString(R.string.online_image_user_avatar_route), null);
                    else
                        imageStoreURL = getMessageImageURL(messageID != null ? messageID : coreImagePath, apiRoute, thumbSize);

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

                        File outputFile = getImageFile(messageID != null ? messageID : imageID, imageStore, appContext);
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
            File imageFile;
            if (messageID != null) {
                imageFile = getImageFile(messageID, imageStore, context);
            } else {
                imageFile = getImageFile(imageID, thumbSize == null ?  IMAGE_STORE_CORE_IMAGE : IMAGE_STORE_CORE_IMAGE_THUMB, context);
            }

            Bitmap file = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            if (file != null && thumbSize == null) {
                file = fixImageOrientation(imageFile.getAbsolutePath(), file, true);
            } else {
                Log.d(TAG, "Downloading from server");
                String apiRoute;
                if (messageID != null && imageStore.equals(MediaLoader.IMAGE_STORE_UPLOADS))
                    apiRoute = appContext.getString(R.string.online_image_message_route);
                else if (messageID != null && imageStore.equals(MediaLoader.IMAGE_STORE_UPLOADS))
                    apiRoute = appContext.getString(R.string.online_image_user_avatar_route);
                else
                    apiRoute = appContext.getString(R.string.online_core_image_route);
                if (thumbSize != null) {
                    apiRoute += "/thumb";
                }
                file = downloadImageFromServer(
                    apiRoute,
                    messageID != null
                        ? imageStore
                        : thumbSize == null
                            ? IMAGE_STORE_CORE_IMAGE
                            : IMAGE_STORE_CORE_IMAGE_THUMB
                );
                if (file != null && thumbSize == null)
                    file = fixImageOrientation(imageFile.getAbsolutePath(), file, true);
            }

            return file != null ? file : BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_image);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                listener.imageHasLoaded(messageID != null ?  messageID : imageID, bitmap, scale);
            } else {
                listener.imageHasFailedToLoad(messageID);
            }
        }
    }

    private static class UploadImageToServer extends AsyncTask<String, Void, Boolean> {

        private Context context;
        String messageToSend;
        Uri imageURI;


        UploadImageToServer(Context context) {
            this.context = context;
        }

        private byte[] loadImageDataAsByteArray(String messageID) {
            File imageFile;
            if (imageURI == null) {
                imageFile = getImageFile(messageID, IMAGE_STORE_UPLOADS, context);
            } else {
                imageFile = new File(imageURI.getPath());
            }
            return getBytes(imageFile);
        }

        private byte[] loadImageDataAsByteArray(Uri imageURI) {
            File imageFile = new File(imageURI.getPath());
            return getBytes(imageFile);
        }

        private byte[] getBytes(File imageFile) {
            Log.d(TAG, "Loading Image Data");
            Log.d(TAG, "PATH - " + imageFile.getAbsolutePath());
            Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            image = fixImageOrientation(imageFile.getAbsolutePath(), image, false);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 30, bos);
            return bos.toByteArray();
        }

        private boolean pushDataToServer(URL uploadURL, byte[] imageBytes) {
            try {
                HttpURLConnection connection = (HttpURLConnection) uploadURL.openConnection();
                connection.setRequestProperty("User-Agent", "MISCA");
                connection.setRequestProperty("userID", UserSettingsManager.getUserID());
                connection.setRequestProperty("User-Certificate", UserSettingsManager.getHashedClientPublicKeyString());
                connection.setRequestProperty("Content-Type", "image/jpeg");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.connect();

                BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
                out.write(imageBytes);
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

        @Override
        protected Boolean doInBackground(String... params) {
            if (imageURI == null) {
                String messageID = params[0];
                String apiRoute = params[1];
                messageToSend = params[2];
                String thumbSize = null;
                if (params.length == 4) {
                    thumbSize = params[3];
                }

                Log.d(TAG, "Starting image upload " + messageID + ": " + apiRoute);

                if (messageID != null && apiRoute != null) {
                    try {
                        URL imageStoreURL = getMessageImageURL(messageID, apiRoute, thumbSize);
                        byte[] imageData = loadImageDataAsByteArray(messageID);
                        return pushDataToServer(imageStoreURL, imageData);
                    } catch (MalformedURLException e) {
                        Log.d(TAG, "Failed to upload image");
                        e.printStackTrace();
                        return false;
                    }

                }
            } else {
                try {
                    URL objectSearchAPI = new URL(getURLStringForObjectSearch());
                    byte[] imageData = loadImageDataAsByteArray(imageURI);
                    return pushDataToServer(objectSearchAPI, imageData);
                } catch (MalformedURLException e) {
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
                if (imageURI == null) {
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
    }

    public static String getURLString(int apiRef, String fileRef, String thumbnailSize) {
        String api = appContext.getResources().getString(apiRef);
        return ServerDetails.getMediaServerHostName()
                + ":" + appContext.getResources().getInteger(R.integer.online_image_store_port)
                + api + (thumbnailSize != null ? "thumb/" : "")
                + fileRef + (thumbnailSize != null ? "?size=" + thumbnailSize : "");
    }

    public static String getURLStringForMessageImage(String messageID) {
        return getURLStringForMessageImage(messageID, null);
    }

    public static String getURLStringForMessageImage(String messageID, String thumbSize) {
        return getURLString(R.string.online_image_message_route, messageID, thumbSize);
    }

    public static String getURLStringForCoreImage(String imagePath) {
        return getURLStringForCoreImage(imagePath, null);
    }

    public static String getURLStringForCoreImageCachedThumb(String imagePath) {
        return getURLString(R.string.online_core_image_thumbs_route, imagePath, null);
    }

    public static String getURLStringForCoreImage(String imagePath, String thumbSize) {
        return getURLString(R.string.online_core_image_route, imagePath, thumbSize);
    }

    public static String getURLStringForObjectSearch() {
        return getURLString(R.string.online_object_search, UserSettingsManager.getUserID(), null);
    }

    public static String getURLStringForAvatar(String userID) {
        return getURLString(R.string.online_image_user_avatar_route, userID, null);
    }

    @NonNull
    private static URL getMessageImageURL(String imagePath, String apiRoute, String thumbSize)
            throws MalformedURLException {
        return new URL(
            ServerDetails.getMediaServerHostName()
                + ":" + appContext.getResources().getInteger(R.integer.online_image_store_port)
                + apiRoute + imagePath
                + (thumbSize != null ? "?size=" + thumbSize : "")
                + (thumbSize != null ? "&" : "?") + "userID=" + UserSettingsManager.getUserID()
        );
    }

}
