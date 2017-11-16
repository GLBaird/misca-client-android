package org.qumodo.miscaclient.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.DataManager;
import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
import org.qumodo.data.models.Message;
import org.qumodo.miscaclient.BuildConfig;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.controllers.MiscaCommandRunner;
import org.qumodo.miscaclient.controllers.MiscaWorkflowManager;
import org.qumodo.miscaclient.dataProviders.LocationProvider;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.miscaclient.fragments.MessageListFragment;
import org.qumodo.miscaclient.fragments.ObjectSearchFragment;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;
import org.qumodo.services.QTCPSocketService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class GroupViewActivity extends Activity implements MessageListFragment.OnMessageListInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String INTENT_GROUP_ID = "group_id";
    public static final String TAG = "GroupViewActivity";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private ActionBar actionBar;
    private String groupID;
    private GoogleApiClient googleApiClient;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            switch (action) {
                case QTCPSocketService.DELEGATE_SOCKET_CLOSED:
                    Intent startupActivity = new Intent(GroupViewActivity.this, StartupActivity.class);
                    startupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startupActivity);
                    finish();
                    break;
                case ObjectSearchFragment.ACTION_CLASSIFICATION_RESULT:
                    String classification = intent.getStringExtra(ObjectSearchFragment.INTENT_CLASSIFICATION);
                    MiscaCommandRunner.runObjectDetection(classification, groupID, GroupViewActivity.this);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        LocationProvider
                .getSharedLocationProvider()
                .setApiClient(googleApiClient);

        groupID = getIntent().getStringExtra(INTENT_GROUP_ID);

        setContentView(R.layout.activity_group_view);

        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            MediaLoader.getUserCircularAvatar(groupID, new MediaLoaderListener() {
                @Override
                public void imageHasLoaded(String ref, Bitmap image, double scale) {
                    DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
                    float size = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 40, displaymetrics );
                    image = Bitmap.createScaledBitmap(image, Math.round(size), Math.round(size), false);
                    Bitmap padded = Bitmap.createBitmap(Math.round((float)((float)image.getWidth() * 1.35)), image.getHeight(), image.getConfig());
                    Canvas c = new Canvas(padded);
                    c.drawBitmap(image, 0, 0, null);
                    BitmapDrawable icon = new BitmapDrawable(getResources(), padded);
                    actionBar.setIcon(icon);
                    actionBar.setDisplayUseLogoEnabled(true);
                    actionBar.setDisplayOptions(
                        ActionBar.DISPLAY_HOME_AS_UP |
                        ActionBar.DISPLAY_USE_LOGO |
                        ActionBar.DISPLAY_SHOW_TITLE |
                        ActionBar.DISPLAY_SHOW_HOME
                    );
                }

                @Override
                public void imageHasFailedToLoad(String ref) {}
            });
        }

        MessageListFragment fragment = new MessageListFragment();
        fragment.setGroup(groupID, getApplicationContext());
        fragment.setRetainInstance(true);
        getFragmentManager()
                .beginTransaction()
                .add(R.id.group_activity_fragment_container, fragment, MessageListFragment.TAG)
                .commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QTCPSocketService.DELEGATE_SOCKET_CLOSED);
        intentFilter.addAction(ObjectSearchFragment.ACTION_CLASSIFICATION_RESULT);
        registerReceiver(receiver, intentFilter);

        if (googleApiClient != null) {
            googleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public QMessage onSendMessage(String message) {
        try {
            JSONObject data = new JSONObject();
            data.put(QMessage.KEY_GROUP_ID, groupID);
            String encodedMessage;
            try {
                encodedMessage = URLEncoder.encode(message, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                encodedMessage = message;
            }
            data.put(QMessage.KEY_MESSAGE, encodedMessage);
            QMessage preparedMessage = new QMessage("*", UserSettingsManager.getUserID(), QMessageType.TEXT, data);

            Log.d(TAG, "Message ready: " + preparedMessage.serialize());
            Intent messageIntent = new Intent();
            messageIntent.setAction(QTCPSocketService.ACTION_SEND_MESSAGE);
            messageIntent.putExtra(QTCPSocketService.INTENT_KEY_MESSAGE, preparedMessage.serialize());
            sendBroadcast(messageIntent);

            return preparedMessage;

        } catch (JSONException e) {
            Log.e(TAG, "Error Parsing JSON for outgoing message");
            e.printStackTrace();
        }

        return null;
    }

    private String messageTextForCaption;
    private String newImageID;

    @Override
    public void onOpenCameraIntent(String message) {
        LocationProvider.getSharedLocationProvider().updateLocation(this, true);
        newImageID = UUID.randomUUID().toString();
        messageTextForCaption = message;
        File imageFile = MediaLoader.getImageFile(
                newImageID,
                MediaLoader.IMAGE_STORE_UPLOADS,
                getApplicationContext()
        );

        Uri imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", imageFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onOpenImageGalleryIntent(String caption) {
        LocationProvider.getSharedLocationProvider().updateLocation(this, true);
        messageTextForCaption = caption;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            storeAndSendImageToMessageList();
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            extractLoadedData(data);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                MiscaCommandRunner.addMiscaMessage("Uploading image for classification...", groupID, this);
                MediaLoader.imageSearch(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "Error with crop " + error);
            }
        }
    }

    private void storeAndSendImageToMessageList() {
        DataManager dm = new DataManager(getApplicationContext());
        Message newPictureMessage = dm.addNewMessage(messageTextForCaption, QMessageType.PICTURE, groupID, newImageID, null, null);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.group_activity_fragment_container);
        if (fragment instanceof MessageListFragment) {
            MessageListFragment messageListFragment = (MessageListFragment) fragment;
            messageListFragment.loadNewMessage(newPictureMessage);
            MiscaWorkflowManager.getManager().startNewImageWorkflow(newPictureMessage.getId(), this, messageListFragment);
        }

        JSONObject data = new JSONObject();
        try {
            data.put(QMessage.KEY_GROUP_ID, groupID);
            data.put(QMessage.KEY_CAPTION, ""); // Ignoring caption for now, as may remove!
        } catch (JSONException e) {
            e.printStackTrace();
        }

        QMessage imageMessage = new QMessage(
                newPictureMessage.getId(),
                new String[] {groupID},
                UserSettingsManager.getUserID(),
                QMessageType.PICTURE,
                data,
                newPictureMessage.getTS()
        );

        MediaLoader.uploadImageToServer(newImageID, imageMessage);

        messageTextForCaption = null;
        newImageID = null;
    }

    private void extractLoadedData(Intent data) {
        try {
            Uri selectedImage = data.getData();
            InputStream in = getContentResolver().openInputStream(selectedImage);
            newImageID = MediaLoader.storeImageInMediaStore(in, MediaLoader.IMAGE_STORE_UPLOADS, this);
            storeAndSendImageToMessageList();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationProvider.getSharedLocationProvider().updateLocation(this, true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(
                this,
                "Cannot connect with Google services, may have problems getting locations and map data.",
                Toast.LENGTH_SHORT
        ).show();
    }
}
