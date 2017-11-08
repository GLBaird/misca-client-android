package org.qumodo.miscaclient.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.qumodo.data.DataManager;
import org.qumodo.data.MessageCenter;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.Message;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;


public class MessageListFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    public static final String TAG = "MessageListFragment";
    public static final String ACTION_LAST_IMAGE_LOADED = "org.qumodo.misca.MessageListFragment.ActionLastImageLoaded";
    public static final String ACTION_IMAGE_ADDED = "org.qumodo.misca.MessageListFragment.ActionImageAdded";
    public static final String INTENT_LIST_ITEM_LOADED = "ActionLastImageLoaded.ListItemLoaded";
    private static final String ARG_COLUMN_COUNT = "column-count";

    private int imageLoadCount = 0;
    private int mColumnCount = 1;
    private OnMessageListInteractionListener mListener;
    private Group group;
    private EditText textEntry;
    private MessageRecyclerViewAdapter adapter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int messageCount = MessageContentProvider.ITEMS.size();
            switch (intent.getAction()) {
                case ACTION_LAST_IMAGE_LOADED:
                    int itemLoaded = intent.getIntExtra(INTENT_LIST_ITEM_LOADED, 0);
                    if ((itemLoaded == messageCount - 2 && imageLoadCount <= 1)
                            || (itemLoaded == messageCount -1 && imageLoadCount == 0)) {
                        imageLoadCount++;
                        recyclerView.scrollToPosition(messageCount - 1);
                    }
                    break;
                case MessageCenter.NEW_LIST_ITEM:
                    adapter.notifyItemRangeRemoved(messageCount - 1, 1);
                    adapter.notifyItemInserted(messageCount - 1);
                    recyclerView.scrollToPosition(messageCount - 1);
                    break;
                case MessageCenter.RELOAD_UI:
                    adapter.notifyDataSetChanged();
                    break;
                case MessageCenter.REMOVE_LAST_ITEM:
                    adapter.notifyItemRemoved(messageCount);
                    recyclerView.scrollToPosition(messageCount - 1);
                case ACTION_IMAGE_ADDED:
                    recyclerView.scrollToPosition(messageCount - 1);
                    break;
            }
        }
    };

    public MessageListFragment() {
    }

    public void setGroup(String groupID, Context context) {
        DataManager dm = new DataManager(context);
        group = dm.getGroup(groupID);
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

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);
        View rView = view.findViewById(R.id.list);

        textEntry = (EditText) view.findViewById(R.id.text_entry);
        Button cameraButton = (Button) view.findViewById(R.id.camera);
        Button sendButton = (Button) view.findViewById(R.id.send_button);

        cameraButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        view.getViewTreeObserver().addOnGlobalLayoutListener(this);


        cameraButton.setOnLongClickListener(this);

        // Set the adapter
        if (rView instanceof RecyclerView) {
            Context context = rView.getContext();
            recyclerView = (RecyclerView) rView;

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new MessageRecyclerViewAdapter(MessageContentProvider.ITEMS, mListener);
            recyclerView.setAdapter(adapter);
            recyclerView.scrollToPosition(MessageContentProvider.ITEMS.size() - 1);
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

        IntentFilter receiverIntent = new IntentFilter();
        receiverIntent.addAction(MessageCenter.RELOAD_UI);
        receiverIntent.addAction(MessageCenter.NEW_LIST_ITEM);
        receiverIntent.addAction(ACTION_LAST_IMAGE_LOADED);
        receiverIntent.addAction(ACTION_IMAGE_ADDED);
        receiverIntent.addAction(MessageCenter.REMOVE_LAST_ITEM);
        getContext().registerReceiver(receiver, receiverIntent);
    }

    @Override
    public void onDetach() {
        getContext().unregisterReceiver(receiver);
        super.onDetach();
        mListener = null;
    }

    private String getTextEntryAndClear() {
        String message = textEntry.getText().toString();
        textEntry.setText("");
        return message;
    }

    private void sendMessage(String message) {
        QMessage messageSent = mListener.onSendMessage(message);
        if (messageSent != null) {
            DataManager dm = new DataManager(getContext());
            MessageContentProvider.addItem(
                    dm.addNewMessage(message, QMessageType.TEXT, group.getId(), messageSent.id, messageSent.from, new Date(messageSent.ts))
            );
            adapter.notifyItemInserted(MessageContentProvider.ITEMS.size() - 1);
        } else {
            Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT)
                 .show();
        }
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

    private boolean layoutForKeyboard = false;

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        //r will be populated with the coordinates of your view that area still visible.
        View view = getView();
        if (view != null) {
            view.getWindowVisibleDisplayFrame(r);

            int heightDiff = view.getRootView().getHeight() - (r.bottom - r.top);
            if (heightDiff > 500 && !layoutForKeyboard) { // if more than 100 pixels, its probably a keyboard...
                recyclerView.scrollToPosition(MessageContentProvider.ITEMS.size() - 1);
                layoutForKeyboard = true;
            } else if (layoutForKeyboard) {
                layoutForKeyboard =false;
            }
        }
    }

    public interface OnMessageListInteractionListener {
        QMessage onSendMessage(String message);
        void onOpenCameraIntent(String caption);
        void onOpenImageGalleryIntent(String caption);
    }
}
