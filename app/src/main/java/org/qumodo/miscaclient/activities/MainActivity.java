package org.qumodo.miscaclient.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.ContentFrameLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.DataManager;
import org.qumodo.data.MediaLoader;
import org.qumodo.data.models.GroupListItem;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.MiscaImage;
import org.qumodo.miscaclient.BuildConfig;
import org.qumodo.miscaclient.QMiscaClientApplication;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.LocationProvider;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.controllers.MiscaWorkflowManager;
import org.qumodo.miscaclient.dataProviders.ServerDetails;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.miscaclient.fragments.MessageListFragment;
import org.qumodo.miscaclient.fragments.ObjectSearchFragment;
import org.qumodo.miscaclient.fragments.QImageListFragment;
import org.qumodo.miscaclient.fragments.QImageViewFragment;
import org.qumodo.miscaclient.fragments.QMiscaGroupsListFragment;
import org.qumodo.miscaclient.fragments.QMiscaMapView;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;
import org.qumodo.services.QTCPSocketService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class MainActivity extends Activity implements QMiscaGroupsListFragment.OnListFragmentInteractionListener,
        MessageListFragment.OnMessageListInteractionListener, FragmentManager.OnBackStackChangedListener,
        View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        QImageListFragment.OnListFragmentInteractionListener {

    private static final String TAG = "MAIN_ACTIVITY";

    private static final String BUNDLE_KEY_GROUP_ID = "org.qumodo.miscaclient.MainActivity.bundleKey.groupID";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_CHECK_SETTINGS = 3;

    public static final String ACTION_SHOW_IMAGE_GALLERY = "org.qumodo.miscaclient.MainActivity.action.ShowImageGallery";
    public static final String ACTION_SHOW_IMAGE_VIEW = "org.qumodo.miscaclient.MainActivity.action.ShowImageView";
    public static final String ACTION_OBJECT_SEARCH_RESULTS = "org.qumodo.miscaclient.MainActivity.action.ObjectSearchResults";

    private ImageButton chatModeButton;
    private ImageButton objectModeButton;
    private ImageButton mapModeButton;
    private ContentFrameLayout ContentFrameLayout;

    private QMiscaGroupsListFragment mGroupsListFragment;
    private QMiscaMapView mMapView;

    private ActionBar actionBar;
    private String groupID;

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 0;
    private GoogleApiClient googleApiClient;

    private Location userLocation;


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case ACTION_SHOW_IMAGE_GALLERY:
                    onOpenImageListIntent();
                    break;
                case ACTION_SHOW_IMAGE_VIEW:
                    String pathName = intent.getStringExtra(QImageViewFragment.INTENT_IMAGE_PATH);
                    String imageID = intent.getStringExtra(QImageViewFragment.INTENT_IMAGE_ID);
                    int service = intent.getIntExtra(QImageViewFragment.INTENT_SERVICE, 0);
                    if (pathName != null) {
                        onOpenImagePreviewIntent(pathName, service, imageID);
                    }
                    break;
                case QTCPSocketService.DELEGATE_SOCKET_CLOSED:
                    Intent startupActivity = new Intent(MainActivity.this, StartupActivity.class);
                    startupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startupActivity);

                    finish();
                    break;
            }
        }
    };

    public QMiscaGroupsListFragment getGroupsListFragment() {
        if (mGroupsListFragment == null) {
            mGroupsListFragment = new QMiscaGroupsListFragment();
        }

        return mGroupsListFragment;
    }

    public QMiscaMapView getMapView() {
        if (mMapView == null) {
            mMapView = new QMiscaMapView();
        }

        return mMapView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            groupID = savedInstanceState.getString(BUNDLE_KEY_GROUP_ID);

            if (groupID != null) {
                ContentFrameLayout frame = findViewById(R.id.main_activity_fragment_container);
                frame.removeAllViews();
            }
        }

        actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(groupID != null);

        if (UserSettingsManager.getUserID() != null && UserSettingsManager.isUserAuthorised()) {
            loadMainFragment();
        }

        chatModeButton = findViewById(R.id.mode_view_chat_button);
        objectModeButton = findViewById(R.id.mode_view_object_find_button);
        mapModeButton = findViewById(R.id.mode_view_map_button);

        chatModeButton.setOnClickListener(this);
        objectModeButton.setOnClickListener(this);
        mapModeButton.setOnClickListener(this);

        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        LocationProvider
                .getSharedLocationProvider()
                .setApiClient(googleApiClient);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        IntentFilter actions = new IntentFilter();
        actions.addAction(ACTION_SHOW_IMAGE_GALLERY);
        actions.addAction(ACTION_SHOW_IMAGE_VIEW);
        actions.addAction(ACTION_OBJECT_SEARCH_RESULTS);
        actions.addAction(QTCPSocketService.DELEGATE_SOCKET_CLOSED);
        registerReceiver(receiver, actions);
        Log.d(TAG, "Intent receiver registered");
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }

        Log.d(TAG, "receiver unregistered");
        unregisterReceiver(receiver);
        super.onStop();
    }

    private void loadMainFragment() {

        ContentFrameLayout frame = findViewById(R.id.main_activity_fragment_container);
        frame.removeAllViews();

        Fragment fragment = getFragmentManager()
                .findFragmentById(R.id.main_activity_fragment_container);

        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .attach(fragment)
                    .commit();
        } else {
            if (groupID == null && currentMode == R.id.mode_view_chat_button) {
                fragment = getGroupsListFragment();
            } else if (currentMode == R.id.mode_view_object_find_button) {
              fragment = getMapView();
            } else if (currentMode == R.id.mode_view_map_button) {
                fragment = getMapView();
            } else {
                MessageContentProvider.setup(getApplicationContext(), groupID);
                fragment = new MessageListFragment();
            }

            fragment.setRetainInstance(true);

            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_activity_fragment_container, fragment, QMiscaGroupsListFragment.TAG)
                    .commit();
        }

        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEY_GROUP_ID, groupID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resuming");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity Pausing");
    }

    static public int getResourceIdFromCurrentThemeAttribute(Activity activity, int attribute){
        TypedValue a = new TypedValue();
        activity.getTheme().resolveAttribute(attribute, a, false);
        return a.resourceId;
    }

    @Override
    public void onListFragmentInteraction(GroupListItem item) {
        groupID = item.id;
        MessageContentProvider.setup(getApplicationContext(), groupID);

        Intent groupMessageList = new Intent(this, GroupViewActivity.class);
        groupMessageList.putExtra(GroupViewActivity.INTENT_GROUP_ID, groupID);
        startActivity(groupMessageList);
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
        LocationProvider.getSharedLocationProvider().updateLocation(this);
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
        LocationProvider.getSharedLocationProvider().updateLocation(this);
        messageTextForCaption = caption;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_GALLERY);
    }

    private void onOpenImageListIntent() {
        Log.d(TAG, "onOpenImageListIntent");
        Fragment fragment = QImageListFragment.newInstance(3);
        fragment.setRetainInstance(true);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_fragment_container, fragment, QImageListFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(QMiscaGroupsListFragment.TAG)
                .commit();
    }

    private void onOpenImagePreviewIntent(String path, int service, String imageID) {
        Fragment fragment = QImageViewFragment.newInstance(path, service, imageID);
        fragment.setRetainInstance(true);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_fragment_container, fragment, QImageViewFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(QMiscaGroupsListFragment.TAG)
                .commit();
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

    private void searchForObject(Uri imageURI) {
        MediaLoader.imageSearch(imageURI);
        Fragment fragment = ObjectSearchFragment.newInstance(imageURI.toString());
        removeCurrentFragment();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.main_activity_fragment_container, fragment, ObjectSearchFragment.TAG)
                .commit();
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
                searchForObject(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "Error with crop " + error);
            }
        }

    }

    private void storeAndSendImageToMessageList() {
        DataManager dm = new DataManager(getApplicationContext());
        Message newPictureMessage = dm.addNewMessage(messageTextForCaption, QMessageType.PICTURE, groupID, newImageID, null, null);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_activity_fragment_container);
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

    @Override
    public void onBackStackChanged() {
        int backCount = getFragmentManager().getBackStackEntryCount();
        if (backCount == 0)
            groupID = null;

        if (backCount == 0 && actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(getString(R.string.actionbar_title_messages));
            actionBar.setIcon(null);
        }
    }

    @Override
    public boolean onNavigateUp() {
        getFragmentManager().popBackStack();
        return true;
    }

    MenuItem item1, item2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_ids, menu);


        String userID = UserSettingsManager.getUserID();

        Log.d(TAG, "Startig wit "+userID);

        for (int i = 0; i < menu.size(); i++) {
           if (menu.getItem(i).getItemId() == R.id.action_user_a)
            item1 = menu.getItem(i);
           else if (menu.getItem(i).getItemId() == R.id.action_user_b)
            item2 = menu.getItem(i);
        }

        if (userID != null && userID.equals(UserSettingsManager.USER_ID_A)) {
            setMenuForID(R.id.action_user_a);
        } else if (userID != null && userID.equals(UserSettingsManager.USER_ID_B)) {
            setMenuForID(R.id.action_user_b);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return setMenuForID(item.getItemId());
    }

    private boolean setMenuForID(int id) {
        Log.d("MENU", "SET FOR "+id+" with "+R.id.action_user_a);
        switch (id) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;

            case R.id.action_user_a:
                UserSettingsManager.setUserID(UserSettingsManager.USER_ID_A);
                item1.setTitle("USER A **SELECTED");
                item2.setTitle("USER B");
                checkForLoadingFragment();
                return true;

            case R.id.action_user_b:
                UserSettingsManager.setUserID(UserSettingsManager.USER_ID_B);
                item1.setTitle("USER A");
                item2.setTitle("USER B **SELECTED");
                checkForLoadingFragment();
                return true;

            case R.id.action_config_host:
                ServerDetails.showHostNameDialoge(this);
                return true;

            case R.id.action_config_port:
                ServerDetails.showPortNumberDialog(this);
                return true;

            case R.id.action_restart_socket:
                Intent closeSocket = new Intent();
                closeSocket.setAction(QTCPSocketService.ACTION_CLOSE_SOCKET);
                sendBroadcast(closeSocket);
                return true;

            default:
                return false;
        }
    }

    private void requestSocketConnect() {
        Intent openSocket = new Intent();
        openSocket.setAction(QMiscaClientApplication.APPLICATION_CONNECT_SOCKET);
        sendBroadcast(openSocket);
    }

    private void checkForLoadingFragment() {
        if (getFragmentManager().findFragmentById(R.id.main_activity_fragment_container) == null)
            loadMainFragment();
    }

    int currentMode = R.id.mode_view_chat_button;

    @Override
    public void onClick(View button) {
        switch (button.getId()) {
            case R.id.mode_view_chat_button:
                if (currentMode != R.id.mode_view_chat_button) {
                    changeMode(R.id.mode_view_chat_button, chatModeButton, R.drawable.ic_textsms_blue_24dp);
                    switchModeToText();
                }
                break;

            case R.id.mode_view_object_find_button:
                if (currentMode != R.id.mode_view_object_find_button) {
                    changeMode(R.id.mode_view_object_find_button, objectModeButton, R.drawable.ic_camera_alt_blue_24dp);
                    switchModeToObjectFind();
                } else {
                    switchModeToObjectFind();
                }
                break;

            case R.id.mode_view_map_button:
                if (currentMode != R.id.mode_view_map_button) {
                    changeMode(R.id.mode_view_map_button, mapModeButton, R.drawable.ic_place_blue_24dp);
                    switchModeToMap();
                }
                break;
        }
    }

    private void changeMode(int mode, ImageButton button, int resource) {
        currentMode = mode;
        chatModeButton.setImageResource(R.drawable.ic_textsms_grey_24dp);
        mapModeButton.setImageResource(R.drawable.ic_place_grey_24dp);
        objectModeButton.setImageResource(R.drawable.ic_camera_alt_grey_24dp);
        button.setImageResource(resource);
        removeCurrentFragment();
    }

    private void removeCurrentFragment() {
        Fragment current = getFragmentManager().findFragmentById(R.id.main_activity_fragment_container);
        if (current != null) {
            getFragmentManager()
                    .beginTransaction()
                    .remove(current)
                    .commit();
        }
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void addFragment(Fragment fragment, String tag) {
        getFragmentManager()
                .beginTransaction()
                .add(R.id.main_activity_fragment_container, fragment, tag)
                .commitNow();
    }


    private void switchModeToText() {
        addFragment(getGroupsListFragment(), QMiscaGroupsListFragment.TAG);
    }

    private void switchModeToObjectFind() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    private void switchModeToMap() {
        addFragment(getMapView(), "QMiscaMapView");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation();
                } else {
                    Toast.makeText(
                        this,
                        "Without your location you will not be able to find local objects!",
                        Toast.LENGTH_SHORT
                    ).show();
                }

                break;
        }
    }

    private void getUserLocation() {
        Log.d(TAG, "Starting location");
        if (userLocation == null
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        ) {
            userLocation =  LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (mMapView != null) {
                mMapView.updateMapView(userLocation, googleApiClient);
            }
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(500);
            locationRequest.setFastestInterval(1);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
        } else if (userLocation != null && mMapView != null) {
            mMapView.updateMapView(userLocation, googleApiClient);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getUserLocation();
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

    @Override
    public void onLocationChanged(Location location) {
        userLocation = location;
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        if (mMapView != null) {
            mMapView.updateMapView(userLocation, googleApiClient);
        }
    }

    @Override
    public void onListFragmentInteraction(MiscaImage item) {
        Intent openImageView = new Intent(this, ImageViewActivity.class);
        openImageView.putExtra(ImageViewActivity.INTENT_IMAGE_ID, item.getId());
        openImageView.putExtra(ImageViewActivity.INTENT_IMAGE_PATH, item.getPath());
        openImageView.putExtra(ImageViewActivity.INTENT_SERVICE, QImageViewFragment.IMAGE_SERVICE_CORE_IMAGE);
        startActivity(openImageView);
    }

}
