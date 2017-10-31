package org.qumodo.miscaclient.fragments;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.qumodo.miscaclient.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ObjectSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ObjectSearchFragment extends Fragment {
    private static final String ARG_IMG_URL = "imageURL";
    private String imageURL;

    public static final String TAG = "org.qumodo.miscaclient.ObjectSearchFragment";

    public ObjectSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageURL Parameter 1.
     * @return A new instance of fragment ObjectSearchFragment.
     */
    public static ObjectSearchFragment newInstance(String imageURL) {
        ObjectSearchFragment fragment = new ObjectSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMG_URL, imageURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageURL = getArguments().getString(ARG_IMG_URL);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onStart() {
        super.onStart();

        Glide.with(getContext())
                .load(imageURL)
                .into(objectImageView);
    }

    ImageView objectImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_object_search, container, false);
        objectImageView = v.findViewById(R.id.object_view);
        return v;
    }

}
