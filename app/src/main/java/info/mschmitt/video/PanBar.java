package info.mschmitt.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;
import com.google.android.exoplayer2.ui.TimeBar;

/**
 * @author Matthias Schmitt
 */
public class PanBar extends View implements TimeBar {
    private static final int DEFAULT_INTERVAL_COLOR = 0xffffffff;
    private static final int DEFAULT_BAR_HEIGHT_DP = 4;
    private static final int DEFAULT_DRAGGED_BAR_HEIGHT_DP = 6;
    private final Paint intervalPaint;
    private final Paint emptyPaint;
    private final Paint draggedIntervalPaint;
    private final Paint draggedEmptyPaint;
    //    private final int strokeHeight;
    private final int minimumFlingVelocity;
    private final int maximumFlingVelocity;
    private final Scroller scroller;
    private long interval;
    private long position;
    private long duration;
    private float scaleFactor;
    private VelocityTracker velocityTracker;
    private float oldMaxX;
    private float oldMinX;
    private int offset;
    private long initialPosition;
    private float positionChange;
    private boolean touching;

    {
        intervalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        intervalPaint.setStyle(Paint.Style.FILL);
        intervalPaint.setStrokeWidth(0);
        intervalPaint.setColor(DEFAULT_INTERVAL_COLOR);
        intervalPaint.setStrokeCap(Paint.Cap.ROUND);
        int emptyColor = getDefaultEmptyColor(DEFAULT_INTERVAL_COLOR);
        emptyPaint = new Paint(intervalPaint);
        emptyPaint.setColor(emptyColor);
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
        int barHeight =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BAR_HEIGHT_DP, displayMetrics);
        intervalPaint.setStrokeWidth(barHeight);
        emptyPaint.setStrokeWidth(barHeight);
        int draggedBarHeight =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_DRAGGED_BAR_HEIGHT_DP,
                        displayMetrics);
        draggedIntervalPaint = new Paint(intervalPaint);
        draggedIntervalPaint.setStrokeWidth(draggedBarHeight);
        draggedEmptyPaint = new Paint(emptyPaint);
        draggedEmptyPaint.setStrokeWidth(draggedBarHeight);
        emptyPaint.setStrokeWidth(barHeight);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        scroller = new Scroller(getContext());
    }

    public static int getDefaultEmptyColor(int playedColor) {
        return 0x33000000 | (playedColor & 0x00FFFFFF);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerCount = event.getPointerCount();
        int index = event.getActionIndex();
        float maxX = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        for (int i = 0; i < pointerCount; i++) {
            if (action == MotionEvent.ACTION_POINTER_UP && i == index) {
                continue;
            }
            maxX = Math.max(maxX, event.getX(i));
            minX = Math.min(minX, event.getX(i));
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touching = true;
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(event);
                oldMinX = minX;
                oldMaxX = maxX;
                startDragScale();
                break;
            case MotionEvent.ACTION_MOVE: {
                velocityTracker.addMovement(event);
                float multiplier = 1;
                if (oldMaxX != oldMinX && maxX != minX) {
                    multiplier = (maxX - minX) / (oldMaxX - oldMinX);
                }
                float distance = (oldMinX - offset) * multiplier - (minX - offset);
                dragScale(distance, multiplier);
                oldMinX = minX;
                oldMaxX = maxX;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = velocityTracker.getXVelocity();
                fling(velocityX);
                velocityTracker.recycle();
                velocityTracker = null;
                touching = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        oldMaxX = maxX;
        oldMinX = minX;
        return true;
    }

    private void fling(float velocityX) {
        if (Math.abs(velocityX) <= minimumFlingVelocity) {
            return;
        }
        velocityX =
                velocityX > 0 ? Math.min(maximumFlingVelocity, velocityX) : -Math.min(maximumFlingVelocity, -velocityX);
        scroller.fling((int) (position * scaleFactor), 0, (int) -velocityX, 0, 0, (int) (duration * scaleFactor), 0, 0);
        postOnAnimation(this::updateFromScroller);
    }

    private void updateFromScroller() {
        if (scroller.computeScrollOffset()) {
            int currX = scroller.getCurrX();
            position = (long) (currX / scaleFactor);
            invalidate();
            postOnAnimation(this::updateFromScroller);
        }
    }

    private void startDragScale() {
        initialPosition = position;
        positionChange = 0;
        scroller.abortAnimation();
    }

    private void dragScale(float distance, float multiplier) {
        scaleFactor = scaleFactor * multiplier;
        positionChange += distance / scaleFactor;
        position = (long) (initialPosition + positionChange);
        position = Math.max(0, position);
        position = Math.min(duration, position);
        postInvalidateOnAnimation();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        offset = w / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        canvas.drawLine(0, height / 2, width, height / 2, touching ? draggedEmptyPaint : emptyPaint);
        long indexOffset = offset / (long) (interval * scaleFactor);
        long startIndex = position / interval;
        long remainder = position % interval;
        if (remainder > 0) {
            startIndex--;
        }
        long first = Math.max(startIndex - indexOffset, 0);
        if (first % 2 == 1) {
            first--;
        }
        for (long i = first; ; i += 2) {
            long j = Math.max(0, Math.min(duration, i * interval));
            long stopJ = Math.min(duration, j + interval);
            int startX = (int) ((j - position) * scaleFactor) + offset;
            int stopX = (int) ((stopJ - position) * scaleFactor) + offset;
            if (startX > width) {
                break;
            }
            canvas.drawLine(startX, height / 2, stopX, height / 2, touching ? draggedIntervalPaint : intervalPaint);
            if (j == duration) {
                break;
            }
        }
    }

    public void setDragTimeIncrement(long time, int unit, int dimension) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        scaleFactor = TypedValue.applyDimension(unit, dimension, displayMetrics) / time;
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
        this.interval = 500;
        this.duration = duration;
        invalidate();
    }

    @Override
    public void setAdGroupTimesMs(@Nullable long[] adGroupTimesMs, @Nullable boolean[] playedAdGroups,
                                  int adGroupCount) {
        // Ignored for now
    }
}
