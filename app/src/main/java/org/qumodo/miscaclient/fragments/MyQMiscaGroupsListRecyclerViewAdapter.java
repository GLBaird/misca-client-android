package org.qumodo.miscaclient.fragments;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.User;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.fragments.QMiscaGroupsListFragment.OnListFragmentInteractionListener;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.views.QMiscaListSquareImageView;

import java.util.List;


public class MyQMiscaGroupsListRecyclerViewAdapter extends RecyclerView.Adapter<MyQMiscaGroupsListRecyclerViewAdapter.ViewHolder> {

    private final List<Group> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyQMiscaGroupsListRecyclerViewAdapter(List<Group> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_misca_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        Message lastMessage = holder.mItem.getLastMessageInGroup();
        holder.groupName.setText(holder.mItem.getName());
        holder.messagePreview.setText(lastMessage.getText());
        holder.messageTime.setText(lastMessage.getSentAsTime());
        int unreadMessages = MessageContentProvider.unreadMessagesInGroup(holder.mView.getContext(), holder.mItem.getId());
        holder.messageCountBubble.setVisibility(unreadMessages > 0 ? View.VISIBLE : View.GONE);
        holder.messageCountBubble.setText(String.valueOf(unreadMessages));
        holder.refreshIconPreview();


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView groupName;
        public TextView messagePreview;
        public ProgressBar loader;
        public QMiscaListSquareImageView iconView;
        public TextView messageTime;
        public TextView messageCountBubble;
        public Group mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            groupName = (TextView) view.findViewById(R.id.group_title);
            messagePreview = (TextView) view.findViewById(R.id.group_message);
            loader = (ProgressBar) view.findViewById(R.id.user_icon_preloader);
            iconView = (QMiscaListSquareImageView) view.findViewById(R.id.user_icon);
            messageTime = (TextView) view.findViewById(R.id.message_time);
            messageCountBubble = (TextView) view.findViewById(R.id.message_bubble);
        }

        public void refreshIconPreview() {
            final String userID = mItem.getLastMessageInGroup().getFrom().getId();
            iconView.setVisibility(View.INVISIBLE);
            loader.setVisibility(View.VISIBLE);
            MediaLoader.getUserCircularAvatar(userID, new MediaLoaderListener() {
                @Override
                public void imageHasLoaded(String ref, Bitmap image) {
                    if (ref.equals(userID)) {
                        loader.setVisibility(View.INVISIBLE);
                        iconView.setVisibility(View.VISIBLE);
                        iconView.setUserImage(image, true);
                    }
                }

                @Override
                public void imageHasFailedToLoad(String ref) {
                    loader.setVisibility(View.INVISIBLE);
                }
            });
        }



        @Override
        public String toString() {
            return super.toString();
        }
    }
}
