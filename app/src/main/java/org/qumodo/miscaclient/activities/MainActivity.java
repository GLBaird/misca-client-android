package org.qumodo.miscaclient.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.qumodo.data.models.Group;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.fragments.QMiscaGroupsListFragment;

public class MainActivity extends Activity implements QMiscaGroupsListFragment.OnListFragmentInteractionListener {

    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QMiscaGroupsListFragment fragment = new QMiscaGroupsListFragment();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.main_activity_fragment_container, fragment, QMiscaGroupsListFragment.TAG)
                .commit();
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

    @Override
    public void onListFragmentInteraction(Group item) {
        Log.d(TAG, "List item clicked "+item.getName());
    }
}
