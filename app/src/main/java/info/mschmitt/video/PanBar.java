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
    private static final int STEP_STROKE_WIDTH = 4;
    private static final int LINE_COLOR = 0xffffffff;
    private final Paint strokePaint;
    private final int strokeHeight;
    private final int minimumFlingVelocity;
    private final int maximumFlingVelocity;
    private final Scroller scroller;
    private long strokeInterval;
    private long position;
    private long duration;
    private float scaleFactor;
    private VelocityTracker velocityTracker;
    private float oldMaxX;
    private float oldMinX;
    private int offset;
    private long initialPosition;
    private float positionChange;

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
        strokeHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);
        strokeInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, displayMetrics);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        scroller = new Scroller(getContext());
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
        int startY = (height - strokeHeight) / 2;
        int stopY = startY + strokeHeight;
        long indexOffset = offset / (long) (strokeInterval * scaleFactor);
        long startIndex = position / strokeInterval;
        long remainder = position % strokeInterval;
        if (remainder > 0) {
            startIndex--;
        }
        for (long i = Math.max(startIndex - indexOffset, 0); ; i++) {
            if (i == 0) {
                int x = (int) (-position * scaleFactor) + offset;
                canvas.drawLine(x, 0, x, height, strokePaint);
            } else if (i * strokeInterval > duration) {
                int x = (int) ((duration - position) * scaleFactor) + offset;
                if (x > width) {
                    break;
                }
                canvas.drawLine(x, 0, x, height, strokePaint);
                break;
            } else {
                int x = (int) ((i * strokeInterval - position) * scaleFactor) + offset;
                if (x > width) {
                    break;
                }
                canvas.drawLine(x, startY, x, stopY, strokePaint);
            }
        }
    }

    public void setDragTimeIncrement(long time, int unit, int dimension) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        scaleFactor = TypedValue.applyDimension(unit, dimension, displayMetrics) / time;
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
