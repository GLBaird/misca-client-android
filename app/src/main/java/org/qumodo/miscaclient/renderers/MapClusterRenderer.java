package org.qumodo.miscaclient.renderers;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.qumodo.data.MediaLoader;
import org.qumodo.data.models.MiscaImage;


public class MapClusterRenderer extends DefaultClusterRenderer<MiscaImage> {

    Context context;

    RequestOptions requestOptions;

    public MapClusterRenderer(Context context, GoogleMap map, ClusterManager<MiscaImage> clusterManager) {
        super(context, map, clusterManager);
        clusterManager.setRenderer(this);
        this.context = context;
        requestOptions = new RequestOptions()
                                .centerInside()
                                .override(130, 130)
                                .circleCrop();
    }

    @Override
    protected void onBeforeClusterItemRendered(MiscaImage item, final MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.visible(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

    }

    @Override
    protected void onClusterItemRendered(final MiscaImage clusterItem, final Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        Glide.with(context)
                .asBitmap()
                .load(MediaLoader.getURLStringForCoreImageCachedThumb(clusterItem.getPath().substring(1)))
                .apply(requestOptions)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        Marker activeMarker = getMarker(clusterItem);
                        if (activeMarker != null && resource != null) {
                            activeMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resource));
                        }
                    }
                });
    }
}
