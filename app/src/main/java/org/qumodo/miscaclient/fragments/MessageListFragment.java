package org.qumodo.miscaclient.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.DataManager;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.Message;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.network.QMessageType;

import java.util.Date;
import java.util.List;
import java.util.UUID;


public class MessageListFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    public static final String TAG = "MessageListFragment";

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnMessageListInteractionListener mListener;
    private Group group;
    private List<Message> messages;
    private EditText textEntry;
    private Button cameraButton;
    private Button sendButton;
    MessageRecyclerViewAdapter adapter;

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

        textEntry = (EditText) view.findViewById(R.id.text_entry);
        cameraButton = (Button) view.findViewById(R.id.camera);
        sendButton = (Button) view.findViewById(R.id.send_button);

        cameraButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        cameraButton.setOnLongClickListener(this);

        // Set the adapter
        if (rView instanceof RecyclerView) {
            Context context = rView.getContext();
            RecyclerView recyclerView = (RecyclerView) rView;

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new MessageRecyclerViewAdapter(MessageContentProvider.ITEMS, mListener);
            recyclerView.setAdapter(adapter);
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

    private String getTextEntryAndClear() {
        String message = textEntry.getText().toString();
        textEntry.setText("");
        return message;
    }

    private void sendMessage(String message) {
        DataManager dm = new DataManager(getContext());
        MessageContentProvider.addItem(
                dm.addNewMessage(message, QMessageType.TEXT, group.getId(), null)
        );
        adapter.notifyItemInserted(MessageContentProvider.ITEMS.size() - 1);

        mListener.onSendMessage(message);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera:
                mListener.onOpenCameraIntent(getTextEntryAndClear());
                break;
            case R.id.send_button:
                sendMessage(getTextEntryAndClear());
                break;
        }
    }

    public void loadNewMessage(Message newMessage) {
        MessageContentProvider.addItem(newMessage);
        adapter.notifyItemInserted(MessageContentProvider.ITEMS.size() - 1);
    }

    @Override
    public boolean onLongClick(View v) {
        mListener.onOpenImageGalleryIntent(getTextEntryAndClear());
        return true;
    }

    public interface OnMessageListInteractionListener {
        void onSendMessage(String message);
        void onOpenCameraIntent(String caption);
        void onOpenImageGalleryIntent(String caption);
    }
}