package info.mschmitt.androidsupport.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Matthias Schmitt
 */
public class StatusBarPlaceholderView extends View {
    private int mStatusBarHeight;

    public StatusBarPlaceholderView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mStatusBarHeight = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? 0 : getStatusBarHeight(getContext());
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public StatusBarPlaceholderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusBarPlaceholderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusBarPlaceholderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), mStatusBarHeight);
    }
}
