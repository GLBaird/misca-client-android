package org.qumodo.miscaclient.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.fragments.QImageViewFragment;

public class ImageViewActivity extends Activity {

    public static final String INTENT_IMAGE_PATH = "org.qumodo.miscaclient.ImageViewActivity.IntentImagePath";
    public static final String INTENT_IMAGE_ID = "org.qumodo.miscaclient.ImageViewActivity.IntentImageID";
    public static final String INTENT_SERVICE = "org.qumodo.miscaclient.ImageViewActivity.IntentService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        String imageID = getIntent().getStringExtra(INTENT_IMAGE_ID);
        String imagePath = getIntent().getStringExtra(INTENT_IMAGE_PATH);
        int service = getIntent().getIntExtra(INTENT_SERVICE, 0);

        Fragment fragment = QImageViewFragment.newInstance(imagePath, service, imageID);
        fragment.setRetainInstance(true);
        getFragmentManager()
                .beginTransaction()
                .add(R.id.image_view_fragment_container, fragment, QImageViewFragment.TAG)
                .commit();
    }
}
