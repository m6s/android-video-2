package info.mschmitt.video;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
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
    private final int stokeHeight;
    private final int minimumFlingVelocity;
    private final int maximumFlingVelocity;
    private final Scroller scroller;
    private long strokeInterval;
    private long position;
    private long duration;
    private float scaleFactor;
    private VelocityTracker velocityTracker;
    private float lastTouchX;
    private float lastTouchY;
    private int activePointerId;
    private long dragStartPosition;
    private float distanceX;
    private float distanceY;
    private ValueAnimator flingAnimator;

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
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        scroller = new Scroller(getContext());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(event);
                // Remember where we started (for dragging)
                lastTouchX = event.getX(index);
                lastTouchY = event.getY(index);
                distanceX = 0;
                distanceY = 0;
                // Save the ID of this pointer (for dragging)
                activePointerId = event.getPointerId(0);
                startDrag();
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                int activePointerIndex = event.findPointerIndex(activePointerId);
                // Calculate the distance moved
                float x = event.getX(activePointerIndex);
                float y = event.getY(activePointerIndex);
                distanceX += x - lastTouchX;
                distanceY += y - lastTouchY;
                // Remember this touch position for the next move event
                lastTouchX = x;
                lastTouchY = y;
                drag(distanceX, distanceY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = velocityTracker.getXVelocity(activePointerId);
                float velocityY = velocityTracker.getYVelocity(activePointerId);
                // Return a VelocityTracker object back to be re-used by others.
                activePointerId = -1;
                if (!fling(velocityX, velocityY)) {
                    endDrag();
                }
                // Return a VelocityTracker object back to be re-used by others.
                velocityTracker.recycle();
                velocityTracker = null;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId == activePointerId) {
                    // This was our active pointer going up. Choose a new active pointer and adjust accordingly.
                    final int newPointerIndex = index == 0 ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                    break;
                }
        }
        return true;
    }

    private void startDrag() {
        scroller.abortAnimation();
        dragStartPosition = position;
    }

    private void endDrag() {
    }

    private boolean fling(float velocityX, float velocityY) {
        if (Math.abs(velocityX) <= minimumFlingVelocity) {
            return false;
        }
        velocityX =
                velocityX > 0 ? Math.min(maximumFlingVelocity, velocityX) : -Math.min(maximumFlingVelocity, -velocityX);
        scroller.fling((int) (position * scaleFactor), 0, (int) -velocityX, 0, 0, (int) (duration * scaleFactor), 0, 0);
        postOnAnimation(this::updateFromScroller);
        return true;
    }

    private void updateFromScroller() {
        if (scroller.computeScrollOffset()) {
            int currX = scroller.getCurrX();
            position = (long) (currX / scaleFactor);
            invalidate();
            postOnAnimation(this::updateFromScroller);
        }
    }

    private void drag(float distanceX, float distanceY) {
        Log.d("X", "drag: " + distanceX);
        position = (long) (dragStartPosition - distanceX / scaleFactor);
        position = Math.max(0, position);
        position = Math.min(duration, position);
        postInvalidateOnAnimation();
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
            if (i * strokeInterval > duration) {
                int x = (int) ((duration - position) * scaleFactor);
                if (x > width) {
                    break;
                }
                canvas.drawLine(x, 0, x, height, strokePaint);
                break;
            }
            int x = (int) ((i * strokeInterval - position) * scaleFactor);
            if (x > width) {
                break;
            }
            int y = height - stokeHeight;
            canvas.drawLine(x, y, x, height, strokePaint);
        }
    }

    public void setScaleFactor(int unit, int dimension, long millis) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.scaleFactor = TypedValue.applyDimension(unit, dimension, displayMetrics) / millis;
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
