package org.qumodo.miscaclient.fragments;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.data.models.Message;
import org.qumodo.network.QMessageType;

import java.util.HashMap;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Message} and makes a call to the
 * specified {@link MessageListFragment.OnMessageListInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private final List<Message> mValues;
    private final MessageListFragment.OnMessageListInteractionListener mListener;

    private final HashMap<String, String> userIDs = new HashMap<>();

    private static class MessageViewTypes {
        private static final int USER_TEXT      = R.layout.fragment_message_user;
        private static final int USER_IMAGE     = 1;
        private static final int GROUP_TEXT     = R.layout.fragment_message_group;
        private static final int GROUP_IMAGE    = 3;
        private static final int MISCA_TEXT     = 4;
        private static final int MISCA_IMAGE    = 5;
        private static final int MISCA_QUESTION = 6;
    }

    public MessageRecyclerViewAdapter(List<Message> items, MessageListFragment.OnMessageListInteractionListener listener) {
        Log.d("@@ RECUCE VA ", "CONSTURCTOR");
        mValues = items;
        mListener = listener;
        for (Message m : items) {
            Log.d(">>> MESSAGE >>>", m.getText());
        }
    }

    private String getFromID(Message message) {
        String fromID = userIDs.get(message.getId());
        if (fromID == null) {
            fromID = message.getFrom().getId();
            userIDs.put(message.getId(), fromID);
        }
        return fromID;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = mValues.get(position);
        final String userID = UserSettingsManager.getUserID();
        final String miscaID = UserSettingsManager.getMiscaID();
        String fromID = getFromID(message);

        if (fromID.equals(userID)) {
            return message.getType() == QMessageType.TEXT
                    ? MessageViewTypes.USER_TEXT
                    : MessageViewTypes.USER_IMAGE;
        } else if (fromID.equals(miscaID)) {
            if (message.getType() == QMessageType.MISCA_QUESTION)
                return MessageViewTypes.MISCA_QUESTION;
            return message.getType() == QMessageType.TEXT
                    ? MessageViewTypes.MISCA_TEXT
                    : MessageViewTypes.MISCA_IMAGE;
        } else {
            return message.getType() == QMessageType.TEXT
                    ? MessageViewTypes.GROUP_TEXT
                    : MessageViewTypes.GROUP_IMAGE;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.messageText.setText(holder.mItem.getText());
        holder.messageTime.setText(holder.mItem.getSentAsTime());
        if (holder.mItem.getText().length() < 30) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.weight = 0;
            holder.messageText.setLayoutParams(lp);
        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.weight = 3;
            holder.messageText.setLayoutParams(lp);
        }
        // TODO: SET ONCLICK LISTENERS
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView messageText;
        public final TextView messageTime;
        public Message mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            messageText = (TextView) view.findViewById(R.id.message_list_item_text);
            messageTime = (TextView) view.findViewById(R.id.message_list_item_time);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
