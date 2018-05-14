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
public class NavigationBarPlaceholderView extends View {
    private int navigationBarHeight;

    public NavigationBarPlaceholderView(Context context) {
        super(context);
        init();
    }

    public NavigationBarPlaceholderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NavigationBarPlaceholderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NavigationBarPlaceholderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public static int getNavigationBarHeight(Resources resources) {
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    private static boolean hasNavigationBar(Resources resources) {
        int hasNavBarId = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return hasNavBarId > 0 && resources.getBoolean(hasNavBarId);
    }

    private void init() {
        Resources resources = getResources();
        navigationBarHeight = !hasNavigationBar(resources) || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? 0
                : getNavigationBarHeight(resources);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), navigationBarHeight);
    }
}
