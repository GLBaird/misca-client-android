package org.qumodo.miscaclient.fragments;


import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
import org.qumodo.miscaclient.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QImageViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QImageViewFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_IMAGE_PATH = "image_path";
    private static final String ARG_IMAGE_ID = "image_id";
    private static final String ARG_SERVICE = "service";

    public static final int IMAGE_SERVICE_CORE_IMAGE = R.string.online_core_image_route;
    public static final int IMAGE_SERVICE_USER_IMAGE = R.string.online_image_message_route;

    public static final String INTENT_IMAGE_PATH = "IntentImagePath";
    public static final String INTENT_IMAGE_ID = "IntentImageID";
    public static final String INTENT_SERVICE = "IntentService";

    public static final String TAG = "QImageViewFragment";

    private String imagePath;
    private int service;

    public QImageViewFragment() {
        // Required empty public constructor
    }

    public static QImageViewFragment newInstance(String imagePath, int service, String imageID) {
        QImageViewFragment fragment = new QImageViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        args.putInt(ARG_SERVICE, service);

        fragment.setArguments(args);
        return fragment;
    }

    ImageView imageView;
    ProgressBar spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagePath = getArguments().getString(ARG_IMAGE_PATH);
            service = getArguments().getInt(ARG_SERVICE);
        }

        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Image view");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_qimage_view, container, false);

        imageView = v.findViewById(R.id.image_view);
        spinner = v.findViewById(R.id.spinner);

        Log.d(TAG, MediaLoader.getURLString(service, imagePath.substring(1), null));
        Glide.with(getContext())
                .load(MediaLoader.getURLString(service, imagePath.substring(1), null))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(getContext(), "Image has failed to load!", Toast.LENGTH_SHORT)
                                .show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        spinner.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);

        return v;
    }
}
