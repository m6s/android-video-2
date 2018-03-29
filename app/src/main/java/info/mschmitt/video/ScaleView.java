package info.mschmitt.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
    private static final String[] TEXTS =
            {"750 ms", "12 sec", "250 ms", "500 ms", "750 ms", "13 sec", "250 ms", "500 ms", "750 ms", "14 sec"};
    private static final int TEXT_COLOR = 0xffffffff;
    private static final int LINE_COLOR = 0xffffffff;
    private final TextPaint textPaint;
    private final Paint stepPaint;
    int stepWidthPx = 60;
    int firstStepPx = 20;
    int firstBigStepIndex = 3;
    int bigStepIncrement = 4;
    int smallStepHeightPx = 30;
    int bigStepHeightPx = 80;
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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int textHeight = (int) (textPaint.descent() - textPaint.ascent());
        textY = (h - bigStepHeightPx + textHeight) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int bigIndex = 0;
        for (int smallIndex = 0; smallIndex < width / stepWidthPx; smallIndex++) {
            int stepX = firstStepPx + smallIndex * stepWidthPx;
            int stepStartY;
            if (smallIndex == firstBigStepIndex + bigIndex * bigStepIncrement) {
                String text = TEXTS[bigIndex];
                int textWidth = (int) textPaint.measureText(text) + 1;
                stepStartY = height - bigStepHeightPx;
                canvas.drawText(text, stepX - textWidth / 2, textY, textPaint);
                bigIndex++;
            } else {
                stepStartY = height - smallStepHeightPx;
            }
            canvas.drawLine(stepX, stepStartY, stepX, height, stepPaint);
        }
    }
}
