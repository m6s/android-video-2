package info.mschmitt.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.Scroller;
import com.google.android.exoplayer2.ui.TimeBar;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Matthias Schmitt
 */
public class ScaleBar extends View implements TimeBar {
    private static final int DEFAULT_INTERVAL_COLOR = 0xffffffff;
    private static final int DEFAULT_BAR_HEIGHT_DP = 4;
    private static final int DEFAULT_DRAGGED_BAR_HEIGHT_DP = 6;
    private static final String TAG = "X";
    private final Paint baseIntervalPaint;
    private final Paint baseEmptyPaint;
    private final Paint baseDraggedIntervalPaint;
    private final Paint baseDraggedEmptyPaint;
    //    private final int strokeHeight;
    private final int minimumFlingVelocity;
    private final int maximumFlingVelocity;
    private final CopyOnWriteArrayList<OnScrubListener> listeners = new CopyOnWriteArrayList<>();
    private final Paint paint = new Paint();
    private long position;
    private long duration;
    private float scaleFactor;
    private VelocityTracker velocityTracker;
    private float oldMaxX;
    private float oldMinX;
    private int offset;
    private long initialPosition;
    private float positionChange;
    private boolean scrubbing;
    private float minScaleFactor = 1;
    private float maxScaleFactor = 1;

    {
        baseIntervalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baseIntervalPaint.setStyle(Paint.Style.FILL);
        baseIntervalPaint.setStrokeWidth(0);
        baseIntervalPaint.setColor(DEFAULT_INTERVAL_COLOR);
        baseIntervalPaint.setStrokeCap(Paint.Cap.ROUND);
        int emptyColor = getDefaultEmptyColor(DEFAULT_INTERVAL_COLOR);
        baseEmptyPaint = new Paint(baseIntervalPaint);
        baseEmptyPaint.setColor(emptyColor);
    }

    public ScaleBar(Context context) {
        this(context, null, 0);
    }

    public ScaleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int barHeight =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BAR_HEIGHT_DP, displayMetrics);
        baseIntervalPaint.setStrokeWidth(barHeight);
        baseEmptyPaint.setStrokeWidth(barHeight);
        int draggedBarHeight =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_DRAGGED_BAR_HEIGHT_DP,
                        displayMetrics);
        baseDraggedIntervalPaint = new Paint(baseIntervalPaint);
        baseDraggedIntervalPaint.setStrokeWidth(draggedBarHeight);
        baseDraggedEmptyPaint = new Paint(baseEmptyPaint);
        baseDraggedEmptyPaint.setStrokeWidth(draggedBarHeight);
        baseEmptyPaint.setStrokeWidth(barHeight);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
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
        float maxX = -Float.MAX_VALUE;
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
                scrubbing = true;
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(event);
                oldMinX = minX;
                oldMaxX = maxX;
                startDragging();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() > 2) {
                    break;
                }
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
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.recycle();
                velocityTracker = null;
                stopDragging(true);
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                float primaryVelocity = velocityTracker.getXVelocity(event.getPointerId(index));
                float primaryX = event.getX(index);
                stopDragging(false);
                velocityTracker.recycle();
                velocityTracker = null;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() > 2) {
                    break;
                }
                velocityTracker.computeCurrentVelocity(1000);
                float secondaryVelocity = velocityTracker.getXVelocity(event.getPointerId(index));
                float secondaryX = event.getX(index);
                break;
        }
        oldMaxX = maxX;
        oldMinX = minX;
        return true;
    }

    private void fling(float x, float velocity, Scroller scroller) {
        if (Math.abs(velocity) <= minimumFlingVelocity) {
            return;
        }
        velocity = velocity > 0 ? Math.min(maximumFlingVelocity, velocity) : -Math.min(maximumFlingVelocity, -velocity);
        scroller.fling((int) x, 0, (int) velocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
    }

    private void startDragging() {
        initialPosition = position;
        positionChange = 0;
        scrubbing = true;
        setPressed(true);
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        notifyScrubStart();
        postInvalidateOnAnimation();
    }

    private void notifyScrubStart() {
        for (OnScrubListener listener : listeners) {
            if (!listeners.contains(listener)) {
                continue;
            }
            listener.onScrubStart(this, position);
        }
    }

    private void dragScale(float distance, float multiplier) {
        scaleFactor = scaleFactor * multiplier;
        scaleFactor = Math.max(minScaleFactor, scaleFactor);
        scaleFactor = Math.min(maxScaleFactor, scaleFactor);
        positionChange += distance / scaleFactor;
        position = (long) (initialPosition + positionChange);
        position = Math.max(0, position);
        position = Math.min(duration, position);
        notifyScrubMove();
        postInvalidateOnAnimation();
    }

    private void notifyScrubMove() {
        for (OnScrubListener listener : listeners) {
            if (!listeners.contains(listener)) {
                continue;
            }
            listener.onScrubMove(this, position);
        }
    }

    private void stopDragging(boolean canceled) {
        scrubbing = false;
        setPressed(false);
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(false);
        }
        notifyScrubStop(canceled);
        postInvalidateOnAnimation();
    }

    private void notifyScrubStop(boolean canceled) {
        for (OnScrubListener listener : listeners) {
            if (!listeners.contains(listener)) {
                continue;
            }
            listener.onScrubStop(this, position, canceled);
        }
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
        paint.set(scrubbing ? baseDraggedEmptyPaint : baseEmptyPaint);
        canvas.drawLine(0, height / 2, width, height / 2, paint);
        long coveredDuration = (long) (width / scaleFactor);
        long interval;
        if (coveredDuration < 1000) {
            interval = 125;
        } else if (coveredDuration > 1000 && coveredDuration < 2000) {
            interval = 250;
        } else if (coveredDuration < 4000) {
            interval = 500;
        } else if (coveredDuration < 8000) {
            interval = 1000;
        } else if (coveredDuration < 16000) {
            interval = 2000;
        } else if (coveredDuration < 32000) {
            interval = 4000;
        } else {
            interval = 8000;
        }
        long indexOffset = (long) (offset / (interval * scaleFactor));
        long startIndex = position / interval;
        long remainder = position % interval;
        if (remainder > 0) {
            startIndex--;
        }
        long first = Math.max(startIndex - indexOffset, 0);
        if (first % 2 == 1) {
            first--;
        }
        paint.set(scrubbing ? baseDraggedIntervalPaint : baseIntervalPaint);
        paint.setAlpha(0xFF);
        for (long i = first; ; i += 2) {
            long j = Math.max(0, Math.min(duration, i * interval));
            long stopJ = Math.min(duration, j + interval);
            int startX = (int) ((j - position) * scaleFactor) + offset;
            int stopX = (int) ((stopJ - position) * scaleFactor) + offset;
            if (startX > width) {
                break;
            }
            canvas.drawLine(startX, height / 2, stopX, height / 2, paint);
            if (j == duration) {
                break;
            }
        }
    }

    @Override
    public void addListener(OnScrubListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(OnScrubListener listener) {
        listeners.remove(listener);
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
        setDragTimeIncrement(500, TypedValue.COMPLEX_UNIT_DIP, 100);
        invalidate();
    }

    public void setDragTimeIncrement(long time, int unit, int dimension) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        scaleFactor = TypedValue.applyDimension(unit, dimension, displayMetrics) / time;
        minScaleFactor = 0.003f;
        maxScaleFactor = 10f;
        invalidate();
    }

    @Override
    public void setAdGroupTimesMs(@Nullable long[] adGroupTimesMs, @Nullable boolean[] playedAdGroups,
                                  int adGroupCount) {
        // Ignored for now
    }

    enum Side {LEFT, RIGHT}
}
