package org.qumodo.miscaclient.fragments;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
import org.qumodo.data.models.GroupListItem;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.fragments.QMiscaGroupsListFragment.OnListFragmentInteractionListener;
import org.qumodo.miscaclient.views.QMiscaListSquareImageView;

import java.util.List;


public class QMiscaGroupsListRecyclerViewAdapter extends RecyclerView.Adapter<QMiscaGroupsListRecyclerViewAdapter.ViewHolder> {

    private final List<GroupListItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public QMiscaGroupsListRecyclerViewAdapter(List<GroupListItem> items, OnListFragmentInteractionListener listener) {
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
        holder.groupName.setText(holder.mItem.name);
        holder.messagePreview.setText(holder.mItem.lastMessageText);
        holder.messageTime.setText(holder.mItem.lastMessageTime);
        holder.messageCountBubble.setVisibility(holder.mItem.unreadMessages > 0 ? View.VISIBLE : View.GONE);
        holder.messageCountBubble.setText(String.valueOf(holder.mItem.unreadMessages));
        holder.iconView.setFlagged(holder.mItem.userOnline, false);
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
        public GroupListItem mItem;

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
            iconView.setVisibility(View.INVISIBLE);
            loader.setVisibility(View.VISIBLE);
            MediaLoader.getUserCircularAvatar(mItem.lastMessageFromID, new MediaLoaderListener() {
                @Override
                public void imageHasLoaded(String ref, Bitmap image) {
                    if (ref.equals(mItem.lastMessageFromID)) {
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
