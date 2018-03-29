package info.mschmitt.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author Matthias Schmitt
 */
public class ScaleView extends View {
    static final int STEP_STROKE_WIDTH = 4;
    private static final String TEXT_750_MS = "750 ms";
    private static final int TEXT_COLOR = 0xffffffff;
    private static final int LINE_COLOR = 0xffffffff;
    private final TextPaint textPaint;
    private final Paint stepPaint;
    int stepWidthPx = 60;
    int firstStepPx = 20;
    int firstBigStepIndex = 3;
    int bigStepIncrement = 4;
    int smallStepHeightPx = 60;
    int bigStepHeightPx = 100;
    private int textWidth;
    private int textHeight;
    private int textY;

    {
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(TEXT_COLOR);
        stepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stepPaint.setStyle(Paint.Style.STROKE);
        stepPaint.setStrokeWidth(STEP_STROKE_WIDTH);
        stepPaint.setColor(LINE_COLOR);
    }

    public ScaleView(Context context) {
        this(context, null, 0);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        textPaint.setTextSize(textSize);
        textWidth = (int) textPaint.measureText(TEXT_750_MS) + 1;
        Rect bounds = new Rect();
        textPaint.getTextBounds(TEXT_750_MS, 0, TEXT_750_MS.length(), bounds);
        textHeight = bounds.bottom - bounds.top;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        textY = (h - bigStepHeightPx + textHeight) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int nextBigStepIndex = firstBigStepIndex;
        for (int i = 0; i < width / stepWidthPx; i++) {
            int stepX = firstStepPx + i * stepWidthPx;
            int stepStartY;
            if (i == nextBigStepIndex) {
                stepStartY = height - bigStepHeightPx;
                nextBigStepIndex += bigStepIncrement;
                canvas.drawText(TEXT_750_MS, stepX - textWidth / 2, textY, textPaint);
            } else {
                stepStartY = height - smallStepHeightPx;
            }
            canvas.drawLine(stepX, stepStartY, stepX, height, stepPaint);
        }
    }
}
