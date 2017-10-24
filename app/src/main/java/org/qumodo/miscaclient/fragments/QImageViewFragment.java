package org.qumodo.miscaclient.fragments;


import android.app.ActionBar;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
import org.qumodo.miscaclient.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QImageViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QImageViewFragment extends Fragment implements MediaLoaderListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_IMAGE_PATH = "image_path";
    private static final String ARG_IMAGE_ID = "image_id";
    private static final String ARG_SERVICE = "service";

    public static final int IMAGE_SERVICE_CORE_IMAGE = 0;
    public static final int IMAGE_SERVICE_USER_IMAGE = 1;

    public static final String INTENT_IMAGE_PATH = "IntentImagePath";
    public static final String INTENT_IMAGE_ID = "IntentImageID";
    public static final String INTENT_SERVICE = "IntentService";

    public static final String TAG = "QImageViewFragment";

    private String imagePath;
    private String imageID;
    private int service;

    public QImageViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imagePath Path of image to display
     * @return A new instance of fragment QImageViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QImageViewFragment newInstance(String imagePath, int service, String imageID) {
        QImageViewFragment fragment = new QImageViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        args.putString(ARG_IMAGE_ID, imageID);
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
            imageID = getArguments().getString(ARG_IMAGE_ID);
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

        if (service == IMAGE_SERVICE_CORE_IMAGE)
            MediaLoader.getCoreImage(imageID, imagePath, null, getContext(), this);
        else
            MediaLoader.getMessageImage(imageID, getContext(), this);

        return v;
    }

    @Override
    public void imageHasLoaded(String ref, Bitmap image) {
        spinner.setVisibility(View.GONE);
        imageView.setImageBitmap(image);
    }

    @Override
    public void imageHasFailedToLoad(String ref) {
        Toast.makeText(getContext(), "Image has failed to load!", Toast.LENGTH_SHORT)
             .show();
    }
}
