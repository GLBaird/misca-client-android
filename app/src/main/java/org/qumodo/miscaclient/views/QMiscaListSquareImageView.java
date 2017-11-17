package org.qumodo.miscaclient.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import org.qumodo.miscaclient.R;

public class QMiscaListSquareImageView extends AppCompatImageView {

    private Boolean flagged = false;
    private Bitmap userImage;
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
        flagPaint.setColor( getResources().getColor(R.color.colorGreen, null));
    }

    public QMiscaListSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        flagPaint.setColor( getResources().getColor(R.color.colorGreen, null));
    }

    public QMiscaListSquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        flagPaint.setColor( getResources().getColor(R.color.colorGreen, null));
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

        if (userImage != null) {

            @SuppressLint("DrawAllocation") Rect src = new Rect(0,0,userImage.getWidth()-1, userImage.getHeight()-1);
            @SuppressLint("DrawAllocation") Rect dest = new Rect(0,0,canvas.getWidth()-1, canvas.getHeight()-1);
            canvas.drawBitmap(userImage, src, dest, null);

        }

        if (flagged) {
            final float flagSize = canvas.getWidth() / 10;
            final float xPos = canvas.getWidth() - flagSize;
            final float yPOS = canvas.getHeight() - flagSize;
            canvas.drawCircle(xPos, yPOS, flagSize, flagPaint);
        }
    }
}
