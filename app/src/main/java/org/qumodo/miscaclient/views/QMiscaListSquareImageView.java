package org.qumodo.miscaclient.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;

import org.qumodo.data.MediaLoader;
import org.qumodo.miscaclient.R;

public class QMiscaListSquareImageView extends AppCompatImageView {

    private Boolean flagged = false;
    private Bitmap userImage;
//    final private Path path = new Path();
//    final private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint flagPaint = new Paint();

    public void setUserImage(Bitmap userImage, boolean updateUI) {
        if (this.userImage != null) {
            this.userImage.recycle();
        }
        this.userImage = userImage;
        if (updateUI) {
            invalidate();
        }
    }

    public void setFlagged(Boolean flagged, boolean updateUI) {
        this.flagged = flagged;
        if (updateUI) {
            invalidate();
        }
    }

    public void setFlagColor(int flagColor, boolean updateUI) {
        this.flagPaint.setColor(flagColor);
        if (updateUI) {
            invalidate();
        }
    }

    public QMiscaListSquareImageView(Context context) {
        super(context);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        paint.setAntiAlias(true);
        flagPaint.setColor( getResources().getColor(R.color.colorGreen, getContext().getTheme()));
    }

    public QMiscaListSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMiscaListSquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        setMeasuredDimension(height, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d("@@", "Image is " + userImage);

        if (userImage != null) {

            @SuppressLint("DrawAllocation") Rect src = new Rect(0,0,userImage.getWidth()-1, userImage.getHeight()-1);
            @SuppressLint("DrawAllocation") Rect dest = new Rect(0,0,canvas.getWidth()-1, canvas.getHeight()-1);
            canvas.drawBitmap(userImage, src, dest, null);

//            // Create a circular path.
//            final float halfWidth = canvas.getWidth()/2;
//            final float halfHeight = canvas.getHeight()/2;
//            final float radius = Math.max(halfWidth, halfHeight);
//            path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CCW);
//            canvas.drawPath(path, paint);
        }

        if (flagged) {
            final float flagSize = canvas.getWidth() / 10;
            final float xPos = canvas.getWidth() - flagSize / 2;
            final float yPOS = canvas.getHeight() - flagSize / 2;
            canvas.drawCircle(xPos, yPOS, flagSize, flagPaint);
        }
    }
}
