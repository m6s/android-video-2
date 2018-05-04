package info.mschmitt.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import com.google.android.exoplayer2.ui.TimeBar;

/**
 * @author Matthias Schmitt
 */
public class PanBar extends View implements TimeBar {
    private static final int STEP_STROKE_WIDTH = 4;
    private static final int LINE_COLOR = 0xffffffff;
    private final Paint strokePaint;
    private final int stokeHeight;
    private long strokeInterval;
    private long position;
    private long duration;
    private float scaleFactor;

    {
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(STEP_STROKE_WIDTH);
        strokePaint.setColor(LINE_COLOR);
    }

    public PanBar(Context context) {
        this(context, null, 0);
    }

    public PanBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        stokeHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);
        strokeInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        long startIndex = position / strokeInterval;
        long remainder = position % strokeInterval;
        if (remainder > 0) {
            startIndex++;
        }
        for (long i = startIndex; ; i++) {
            int x = (int) ((i * strokeInterval - position) * scaleFactor);
            if (x > width) {
                break;
            }
            int y = height - stokeHeight;
            canvas.drawLine(x, y, x, height, strokePaint);
        }
    }

    public void setScaleFactor(int unit, float scaleFactor) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.scaleFactor = (long) TypedValue.applyDimension(unit, scaleFactor, displayMetrics);
        invalidate();
    }

    public void setStrokeInterval(long strokeInterval) {
        this.strokeInterval = strokeInterval;
        invalidate();
    }

    @Override
    public void addListener(OnScrubListener listener) {
    }

    @Override
    public void removeListener(OnScrubListener listener) {
    }

    @Override
    public void setKeyTimeIncrement(long time) {
        // Ignored for now
    }

    @Override
    public void setKeyCountIncrement(int count) {
        // Ignored for now
    }

    @Override
    public void setPosition(long position) {
        this.position = position;
        invalidate();
    }

    @Override
    public void setBufferedPosition(long bufferedPosition) {
        // Ignored for now
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
        invalidate();
    }

    @Override
    public void setAdGroupTimesMs(@Nullable long[] adGroupTimesMs, @Nullable boolean[] playedAdGroups,
                                  int adGroupCount) {
        // Ignored for now
    }
}