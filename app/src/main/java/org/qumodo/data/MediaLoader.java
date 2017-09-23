package org.qumodo.data;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.UUID;


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

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();

        if (source.getWidth() > 1000) {
            float scale = (float) 1000 / (float) source.getWidth();
            matrix.postScale(scale, scale);
            Log.d("@@@@@", "Scale "+scale+"  "+source.getWidth()+"  "+source.getHeight());
        }
        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap fixImageOrientation(String filepath, Bitmap image) {
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
                return rotateBitmap(image, rotate);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix matrix = new Matrix();

        if (image.getWidth() > 1000) {
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

    public static void uploadImageToServer(String imageID) {
        UploadImageToServer uploadTask = new UploadImageToServer(appContext);
        uploadTask.execute(imageID, appContext.getString(R.string.online_image_message_route));
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

        @Override
        protected Bitmap doInBackground(String... params) {
            File imageFile = getImageFile(messageID, IMAGE_STORE_UPLOADS, context);
            Bitmap file = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            if (file != null) {
                file = fixImageOrientation(imageFile.getAbsolutePath(), file);
            }

            return file;
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

        UploadImageToServer(Context context) {
            this.context = context;
        }

        private String getISODateString() {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            df.setTimeZone(tz);
            return df.format(new Date());
        }

        private String loadImageData(String messageID) {
            File imageFile = getImageFile(messageID, IMAGE_STORE_UPLOADS, context);
            Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArrayImage = baos.toByteArray();
            return Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        }

        private String packImageDataIntoUMF(String messageID, String serviceAPI) {
            try {
                JSONObject message = new JSONObject();
                message.put("mid", UUID.randomUUID().toString());
                message.put("to", "[post]"+serviceAPI+"/"+messageID);
                message.put("from", "uid:"+UserSettingsManager.getUserID());
                message.put("version", "UMF/1.3");
                message.put("timestamp", getISODateString());

                JSONObject body = new JSONObject();
                body.put("type", "private:message");
                body.put("contentType", "text/plain");
                body.put("base64", "TEST");

                message.put("body", body);

                return message.toString();
            } catch (JSONException e) {
                Log.e(TAG, "Error forming JSON data for UMF");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(TAG, "Starting image upload");
            String messageID = params[0];
            String apiRoute  = params[1];
            String umf = packImageDataIntoUMF(messageID, apiRoute);
            if (umf != null && messageID != null && apiRoute != null) {
                try {
                    URL imageStoreURL = new URL(
                        appContext.getString(R.string.online_image_hostname)
                            + ":" + appContext.getResources().getInteger(R.integer.online_image_store_port)
                            + apiRoute + messageID
                    );

                    HttpURLConnection connection = (HttpURLConnection) imageStoreURL.openConnection();
                    connection.setRequestProperty("User-Agent", "");
                    connection.setRequestProperty("userID", UserSettingsManager.getUserID());
                    connection.setRequestProperty("User-Certificate", UserSettingsManager.getHashedClientPublicKeyString());
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.connect();

                    OutputStream output = connection.getOutputStream();
                    output.write(umf.getBytes());
                    output.close();

                    Scanner result = new Scanner(connection.getInputStream());
                    String response = result.nextLine();
                    result.close();

                    Log.d("Media Loader", response);

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
            }
        }
    }

}
