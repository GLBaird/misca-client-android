package org.qumodo.miscaclient.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.ImageListProvider;
import org.qumodo.miscaclient.dataProviders.LocationImageProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ObjectSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ObjectSearchFragment extends Fragment implements LocationImageProvider.LocationImageProviderListener {
    private static final String ARG_IMG_URL = "imageURL";
    private String imageURL;

    public static final String TAG = "ObjectSearchFragment";

    public static final String ACTION_CLASSIFICATION_RESULT = "org.qumodo.miscaclient.ObjectSearchFragment.ActionClassificationResult";
    public static final String INTENT_CLASSIFICATION = "classification";

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_CLASSIFICATION_RESULT:
                    String classification = intent.getStringExtra(INTENT_CLASSIFICATION);
                    if (classification.contains("_")) {
                        Pattern p = Pattern.compile("[a-z]+_[a-z]+");
                        Matcher m = p.matcher(classification);
                        while(m.find()) {
                            classification += " " + m.group().replace("_", " ");
                        }
                    }
                    Toast.makeText(getContext(), classification, Toast.LENGTH_SHORT).show();
                    LocationImageProvider.getLocationObjectImages(null, classification, getContext());
                    break;
            }
        }
    };

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
        LocationImageProvider.addListener(this);
        if (getArguments() != null) {
            imageURL = getArguments().getString(ARG_IMG_URL);
        }
    }

    @Override
    public void onDestroy() {
        LocationImageProvider.removeListener(this);
        super.onDestroy();
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

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CLASSIFICATION_RESULT);

        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        getContext().unregisterReceiver(receiver);
        super.onStop();
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

    @Override
    public void locationImageProviderHasUpdatedWithData() {
        if (LocationImageProvider.ITEMS.size() <= 0) {
            View spinner = getView().findViewById(R.id.progress_indicator);
            if (spinner != null) {
                spinner.setVisibility(View.GONE);
            }
            TextView resultText = getView().findViewById(R.id.classification);
            if (resultText != null) {
                resultText.setText("No results found");
            }
            resultText.setVisibility(View.VISIBLE);
        } else {
            ImageListProvider.setITEMS(LocationImageProvider.ITEMS);
            Fragment listFrag = QImageListFragment.newInstance(3);
            listFrag.setRetainInstance(true);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_fragment_container, listFrag, QImageListFragment.TAG)
                    .commit();
        }
    }
}
