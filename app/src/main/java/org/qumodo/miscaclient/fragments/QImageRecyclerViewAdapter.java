package org.qumodo.miscaclient.fragments;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
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
        holder.mClassifierView.setText(mValues.get(position).getClassifier());
        holder.mImageView.setImageBitmap(null);
        holder.mSpinner.setVisibility(View.VISIBLE);

        MediaLoader.getCoreImage(holder.mItem.getId(), holder.mItem.getPath(), "100", holder.mView.getContext(), new MediaLoaderListener() {
            @Override
            public void imageHasLoaded(String ref, Bitmap image) {
                Log.d("RECY", "REF "+ref+" ::"+holder.mItem.getId());
                if (holder.mItem.getId().equals(ref)) {
                    holder.mImageView.setImageBitmap(image);
                    holder.mSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void imageHasFailedToLoad(String ref) {
                holder.mSpinner.setVisibility(View.GONE);
            }
        });

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
        public final TextView mClassifierView;
        public MiscaImage mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.image_view);
            mSpinner = view.findViewById(R.id.spinner);
            mClassifierView = view.findViewById(R.id.image_classifier);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mClassifierView.getText() + "'";
        }
    }
}
