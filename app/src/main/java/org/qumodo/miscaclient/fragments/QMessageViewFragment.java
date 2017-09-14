package org.qumodo.miscaclient.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.qumodo.data.DataManager;
import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.User;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMessageFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class QMessageViewFragment extends Fragment {

    public static final String TAG = "QMessageViewFragment";

    private OnMessageFragmentInteractionListener mListener;

    private Group group;
    private List<Message> messages;

    public QMessageViewFragment() {
        // Required empty public constructor
    }

    public void setGroup(String groupID, Context context) {
        DataManager dm = new DataManager(context);
        group = dm.getGroup(groupID);
        messages = MessageContentProvider.ITEMS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar ab = getActivity().getActionBar();
        if (ab != null && group != null) {
            ab.setTitle(group.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_qmessage_view, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMessageFragmentInteractionListener) {
            mListener = (OnMessageFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMessageFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMessageFragmentInteractionListener {
        void onSendMessage(String message);
        void onUploadImage(Bitmap image);
    }
}
