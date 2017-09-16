package org.qumodo.miscaclient.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.qumodo.data.DataManager;
import org.qumodo.data.MediaLoader;
import org.qumodo.data.models.GroupListItem;
import org.qumodo.data.models.Message;
import org.qumodo.miscaclient.BuildConfig;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.miscaclient.fragments.MessageListFragment;
import org.qumodo.miscaclient.fragments.QMiscaGroupsListFragment;
import org.qumodo.network.QMessageType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static android.R.attr.bitmap;
import static android.R.attr.cacheColorHint;

public class MainActivity extends Activity implements QMiscaGroupsListFragment.OnListFragmentInteractionListener,
        MessageListFragment.OnMessageListInteractionListener, FragmentManager.OnBackStackChangedListener {

    private static final String TAG = "MAIN_ACTIVITY";

    private static final String BUNDLE_KEY_GROUP_ID = "org.qumodo.misca.MainActivity.bundleKey.groupID";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    ActionBar actionBar;

    private String groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            groupID = savedInstanceState.getString(BUNDLE_KEY_GROUP_ID);
        }

        actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(groupID != null);

        Fragment fragment = getFragmentManager()
                .findFragmentById(R.id.main_activity_fragment_container);

        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .attach(fragment)
                    .commit();
        } else {
            if (groupID == null) {
                fragment = new QMiscaGroupsListFragment();
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
        MessageListFragment fragment = new MessageListFragment();
        fragment.setGroup(groupID, getApplicationContext());
        fragment.setRetainInstance(true);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_fragment_container, fragment, MessageListFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(QMiscaGroupsListFragment.TAG)
                .commit();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSendMessage(String message) {
        Log.d("MAIN ACTIVITY", "SEND MESSAGE " + message);
    }

    private String messageTextForCaption;
    private String newImageID;

    @Override
    public void onOpenCameraIntent(String message) {
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
        messageTextForCaption = caption;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_GALLERY);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            storeAndSendImageToMessageList();
        } else  if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            extractLoadedData(data);
        }

    }

    private void storeAndSendImageToMessageList() {
        DataManager dm = new DataManager(getApplicationContext());
        Message newPictureMessage = dm.addNewMessage(messageTextForCaption, QMessageType.PICTURE, groupID, newImageID);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_activity_fragment_container);
        if (fragment instanceof MessageListFragment) {
            MessageListFragment messageListFragment = (MessageListFragment) fragment;
            messageListFragment.loadNewMessage(newPictureMessage);
        }

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

        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.action_user_a) {
                item1 = menu.getItem(i);
                menu.getItem(i).setIcon(R.drawable.ic_check_black_24dp);
            } else if (menu.getItem(i).getItemId() == R.id.action_user_b) {
                menu.getItem(i).setIcon(R.drawable.ic_check_black_24dp);
                item2 = menu.getItem(i);
            }
        }
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user_a:
                UserSettingsManager.setUserID(UserSettingsManager.USER_ID_A);
                item1.setTitle("USER A **SELECTED");
                item2.setTitle("USER B");
                break;

            case R.id.action_user_b:
                UserSettingsManager.setUserID(UserSettingsManager.USER_ID_B);
                item1.setTitle("USER A");
                item2.setTitle("USER B **SELECTED");
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
