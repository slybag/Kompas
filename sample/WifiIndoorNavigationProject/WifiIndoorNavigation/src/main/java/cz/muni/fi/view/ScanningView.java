package cz.muni.fi.view;

/**
 * Created by Michal on 25.2.14.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import cz.muni.fi.orientation.Compass;
import cz.muni.fi.orientation.HeadingListener;

public class ScanningView extends View implements HeadingListener {

    private static final String TAG = "ScanningView";

    private Compass mCompass;
    private Paint mPaint;

    public void setCompass(Compass compass) {
        mCompass = compass;
        mCompass.register(this);
    }

    public ScanningView(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        //mPaint.setColor(0x7fff00);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mCompass.reset();
                break;
        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int color = paintMe();
        canvas.drawColor(color);
        canvas.drawText("Heading: " + (180 * mCompass.getHeading() / Math.PI), 5, 15, mPaint);
        canvas.drawText("90", 300, 200, mPaint);
        canvas.drawText("180", 150, 400, mPaint);
        canvas.drawText("270", 5, 200, mPaint);
        canvas.translate(getWidth() / 2, getHeight() / 2);
        mCompass.draw(canvas);
    }

    @Override
    public void headingChanged(float heading) {
        //mPaint.setColor(paintMe());
        postInvalidate();
    }

    public int paintMe() {
        double value = mCompass.getHeading() * 180 / Math.PI;
        //Log.d(TAG, "color me: " + value);
        if (( value >= 355 || value <= 5) ||
                (value >= 40 && value <= 50) ||
                (value >= 85 && value <= 95) ||
                (value >= 130 && value <= 140) ||
                (value >= 175 && value <= 185) ||
                (value >= 220 && value <= 230) ||
                (value >= 265 && value <= 275) ||
                (value >= 310 && value <= 320)) return  0x7fff00;
        return 0xFFFFFF;
        //return 0x7fff00;
    }
}
