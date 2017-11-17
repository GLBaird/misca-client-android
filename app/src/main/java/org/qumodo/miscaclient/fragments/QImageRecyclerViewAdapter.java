package org.qumodo.miscaclient.fragments;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.qumodo.data.MediaLoader;
import org.qumodo.data.models.MiscaImage;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.fragments.QImageListFragment.OnListFragmentInteractionListener;

import java.util.List;


public class QImageRecyclerViewAdapter extends RecyclerView.Adapter<QImageRecyclerViewAdapter.ViewHolder> {

    private final List<MiscaImage> mValues;
    private final OnListFragmentInteractionListener mListener;

    public QImageRecyclerViewAdapter(List<MiscaImage> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_qimage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mImageView.setImageBitmap(null);
        holder.mSpinner.setVisibility(View.VISIBLE);

        Glide.with(holder.mView.getContext())
                .load(MediaLoader.getURLStringForCoreImageCachedThumb(holder.mItem.getPath().substring(1)))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.mSpinner.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.mSpinner.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.mImageView);

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
        public final ImageView mImageView;
        public final ProgressBar mSpinner;
        public MiscaImage mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.image_view);
            mSpinner = view.findViewById(R.id.spinner);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItem.getClassifier() + "'";
        }
    }
}
