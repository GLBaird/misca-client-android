package org.qumodo.miscaclient.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.qumodo.data.DataManager;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.Message;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;

import java.util.List;


public class MessageListFragment extends Fragment {

    public static final String TAG = "MessageListFragment";

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnMessageListInteractionListener mListener;
    private Group group;
    private List<Message> messages;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageListFragment() {
    }

    public void setGroup(String groupID, Context context) {
        DataManager dm = new DataManager(context);
        group = dm.getGroup(groupID);
        messages = MessageContentProvider.ITEMS;
    }

    @SuppressWarnings("unused")
    public static MessageListFragment newInstance(int columnCount) {
        MessageListFragment fragment = new MessageListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        final ActionBar ab = getActivity().getActionBar();
        if (ab != null && group != null) {
            ab.setTitle(group.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);
        View rView = view.findViewById(R.id.list);

        // Set the adapter
        if (rView instanceof RecyclerView) {
            Context context = rView.getContext();
            RecyclerView recyclerView = (RecyclerView) rView;

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MessageRecyclerViewAdapter(MessageContentProvider.ITEMS, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMessageListInteractionListener) {
            mListener = (OnMessageListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMessageListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMessageListInteractionListener {
        void onSendMessage(String message);
        void onUploadImage(Bitmap image);
    }
}
