package info.mschmitt.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
    private final Paint linePaint;
    private final int shortLineHeight;
    private final int tallLineHeight;
    private final int textPadding;
    int stepWidth = 240;
    int offset = 200;
    int firstBigStepIndex = 1;
    int bigStepIncrement = 4;
    private int textY;

    {
        textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
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
        shortLineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);
        tallLineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, displayMetrics);
        textPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, displayMetrics);
    }

    public void setStepWidth(int stepWidth) {
        this.stepWidth = stepWidth;
        invalidate();
    }

    public void setOffset(int offset) {
        this.offset = offset;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        textY = h - tallLineHeight - textPadding;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int bigIndex = 0;
        for (int i = 0; i < width / stepWidth; i++) {
            int startX = offset + i * stepWidth;
            String text = TEXTS[i];
            int textWidth = (int) textPaint.measureText(text) + 1;
            int stepStartY;
            if (i == firstBigStepIndex + bigIndex * bigStepIncrement) {
                stepStartY = height - tallLineHeight;
                bigIndex++;
            } else {
                stepStartY = height - shortLineHeight;
            }
            canvas.drawText(text, startX - textWidth / 2, textY, textPaint);
            canvas.drawLine(startX, stepStartY, startX, height, linePaint);
        }
    }
}
