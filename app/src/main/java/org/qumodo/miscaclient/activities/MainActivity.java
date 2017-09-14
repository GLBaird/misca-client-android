package org.qumodo.miscaclient.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;

import org.qumodo.data.models.GroupListItem;
import org.qumodo.data.models.Message;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.fragments.QMessageViewFragment;
import org.qumodo.miscaclient.fragments.QMiscaGroupsListFragment;

public class MainActivity extends Activity implements QMiscaGroupsListFragment.OnListFragmentInteractionListener,
        QMessageViewFragment.OnMessageFragmentInteractionListener, FragmentManager.OnBackStackChangedListener {

    private static final String TAG = "MAIN_ACTIVITY";

    private static final String BUNDLE_KEY_GROUP_ID = "org.qumodo.misca.MainActivity.bundleKey.groupID";

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
                fragment = new QMessageViewFragment();
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
        QMessageViewFragment fragment = new QMessageViewFragment();
        fragment.setGroup(groupID, getApplicationContext());
        fragment.setRetainInstance(true);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_fragment_container, fragment, QMessageViewFragment.TAG)
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

    @Override
    public void onUploadImage(Bitmap image) {
        Log.d("MAIN ACTIVITY", "Upload Image " + image.toString());
    }

    @Override
    public void onBackStackChanged() {
        int backCount = getFragmentManager().getBackStackEntryCount();
        Log.d("MAIN ACTIVITY", "BACK STACK POP REQ " + backCount);
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
}
