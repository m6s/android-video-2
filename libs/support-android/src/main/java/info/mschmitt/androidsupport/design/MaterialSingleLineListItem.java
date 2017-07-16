package info.mschmitt.androidsupport.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import info.mschmitt.androidsupport.ObjectsBackport;
import info.mschmitt.androidsupport.R;

/**
 * @author Matthias Schmitt
 */
public class MaterialSingleLineListItem extends LinearLayout {
    private final TextView textView1;
    private String text1;

    public MaterialSingleLineListItem(Context context) {
        this(context, null, 0);
    }

    public MaterialSingleLineListItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.material_single_line_list_item, this);
        initAttributes(attrs, defStyleAttr);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setVisibility(text1 == null ? GONE : VISIBLE);
        textView1.setText(text1);
        setOrientation(LinearLayout.VERTICAL);
        int padding =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        setPadding(padding, padding, padding, padding);
        int minHeight =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        setMinimumHeight(minHeight);
    }

    private void initAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.MaterialMultiLineListItem, defStyleAttr, 0);
        text1 = typedArray.getString(R.styleable.MaterialMultiLineListItem_text1);
        typedArray.recycle();
    }

    public MaterialSingleLineListItem(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setText1(String text1) {
        if (ObjectsBackport.equals(text1, this.text1)) {
            return;
        }
        if (text1 == null || this.text1 == null) {
            textView1.setVisibility(text1 == null ? GONE : VISIBLE);
        }
        textView1.setText(text1);
    }
}
