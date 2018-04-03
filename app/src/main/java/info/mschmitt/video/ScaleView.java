package info.mschmitt.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import java.util.concurrent.TimeUnit;

/**
 * @author Matthias Schmitt
 */
public class ScaleView extends View {
    static final int STEP_STROKE_WIDTH = 4;
    private static final String[] TEXTS =
            {"750 ms", "12 sec", "250 ms", "500 ms", "750 ms", "13 sec", "250 ms", "500 ms", "750 ms", "14 sec"};
    private static final int TEXT_COLOR = 0xffffffff;
    private static final int LINE_COLOR = 0xffffffff;
    private final TextPaint textPaint;
    private final Paint linePaint;
    private final int minorStokeHeight;
    private final int majorStrokeHeight;
    private final int textPadding;
    float zeroOffset;
    float scaleFactor = 1;
    long scalePosition = TimeUnit.MINUTES.toMillis(5) + TimeUnit.SECONDS.toMillis(5) + 420;
    long max = TimeUnit.MINUTES.toMillis(7) + TimeUnit.SECONDS.toMillis(44);
    int minorStepWidth = 250;
    private long majorStepInterval = 4;
    private int offset;
    private int textY;

    {
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(TEXT_COLOR);
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(STEP_STROKE_WIDTH);
        linePaint.setColor(LINE_COLOR);
    }

    public ScaleView(Context context) {
        this(context, null, 0);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);
        textPaint.setTextSize(textSize);
        minorStokeHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);
        majorStrokeHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, displayMetrics);
        textPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, displayMetrics);
    }

    protected String getLabel(long scaleX, boolean major) {
        return DateUtils.formatDateRange(getContext(), 0, scaleX, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        textY = h - majorStrokeHeight - textPadding;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        long startIndex = scalePosition / minorStepWidth;
        long scaleOffset = scalePosition % minorStepWidth;
        if (scaleOffset > 0) {
            startIndex++;
        }
        for (long i = startIndex; ; i++) {
            boolean majorStep = i % majorStepInterval == 0;
            long scaleX = i * minorStepWidth;
            int x = (int) ((scaleX - scalePosition) * scaleFactor);
            if (x > width) {
                break;
            }
            int y = majorStep ? height - majorStrokeHeight : height - minorStokeHeight;
            canvas.drawLine(x, y, x, height, linePaint);
            if (majorStep) {
                String label = getLabel(scaleX, majorStep);
                canvas.drawText(label, x, textY, textPaint);
            }
        }
    }
}
