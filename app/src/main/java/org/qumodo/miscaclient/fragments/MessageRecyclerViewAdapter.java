package org.qumodo.miscaclient.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
import org.qumodo.data.MessageCenter;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.data.models.Message;
import org.qumodo.network.QMessageType;

import java.io.File;
import java.util.HashMap;
import java.util.List;


public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private final List<Message> mValues;
    private final MessageListFragment.OnMessageListInteractionListener mListener;

    private final HashMap<String, String> userIDs = new HashMap<>();

    private static class MessageViewTypes {
        private static final int USER_TEXT      = R.layout.fragment_message_user;
        private static final int USER_IMAGE     = R.layout.fragment_message_user_picture;
        private static final int GROUP_TEXT     = R.layout.fragment_message_group;
        private static final int GROUP_IMAGE    = R.layout.fragment_message_group_picture;
        private static final int MISCA_TEXT     = R.layout.fragment_message_user;
        private static final int MISCA_IMAGE    = R.layout.fragment_message_group_picture;
        private static final int MISCA_QUESTION = 6;
    }

    public MessageRecyclerViewAdapter(List<Message> items, MessageListFragment.OnMessageListInteractionListener listener) {
        mValues = items;
        mListener = listener;
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

    private void bindTextMessageView(final ViewHolder holder) {
        holder.messageText.setText(holder.mItem.getText());
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
    }

    private boolean loading = false;

    private void bindPictureMessage(final ViewHolder holder) {
        loading = true;
        holder.imageView.setVisibility(View.INVISIBLE);
        holder.spinner.setVisibility(View.VISIBLE);
        Log.d("MessageRecycleView", "Loading Message");
        MediaLoader.getMessageImage(holder.mItem.getId(), holder.mView.getContext(), new MediaLoaderListener() {
            @Override
            public void imageHasLoaded(String ref, Bitmap image) {
                Log.d("MessageRecycleView", "Image has loaded");
                holder.imageView.setImageBitmap(image);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.spinner.setVisibility(View.GONE);
                loading = false;
                holder.imageView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
                int index = mValues.indexOf(holder.mItem);
                if (index >= mValues.size() - 2) {
                    Intent updateUI = new Intent();
                    updateUI.setAction(MessageListFragment.ACTION_LAST_IMAGE_LOADED);
                    updateUI.putExtra(MessageListFragment.INTENT_LIST_ITEM_LOADED, index);
                    holder.imageView.getContext().sendBroadcast(updateUI);
                }
            }

            @Override
            public void imageHasFailedToLoad(String ref) {
                Log.d("BINDER", "load failed");
                holder.imageView.setImageResource(R.drawable.sample_image);
                loading = false;
            }
        });
    }

    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.messageTime.setText(holder.mItem.getSentAsTime());
        if (holder.mItem.getType() == QMessageType.TEXT) {
            bindTextMessageView(holder);
        } else if (holder.mItem.getType() == QMessageType.PICTURE) {
            bindPictureMessage(holder);
        }

        Animation animation = AnimationUtils.loadAnimation(
                holder.mView.getContext(),
                (position > lastPosition)
                        ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.imageView != null && !loading) {
            holder.imageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView messageText;
        public final TextView messageTime;
        public final ImageView imageView;
        public final ProgressBar spinner;
        public Message mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            messageText = (TextView) view.findViewById(R.id.message_list_item_text);
            messageTime = (TextView) view.findViewById(R.id.message_list_item_time);
            spinner = (ProgressBar) view.findViewById(R.id.spinner);
            imageView = (ImageView) view.findViewById(R.id.image_view);
            if (imageView != null) {
                imageView.setClipToOutline(true);
            }
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
