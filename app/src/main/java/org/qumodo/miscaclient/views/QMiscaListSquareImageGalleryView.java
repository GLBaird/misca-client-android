package org.qumodo.miscaclient.views;

import android.content.Context;
import android.util.AttributeSet;

public class QMiscaListSquareImageGalleryView extends android.support.v7.widget.AppCompatImageView {

    public QMiscaListSquareImageGalleryView(final Context context) {
        super(context);
    }

    public QMiscaListSquareImageGalleryView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public QMiscaListSquareImageGalleryView(final Context context, final AttributeSet attrs,
                final int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, measuredWidth);
    }
}
