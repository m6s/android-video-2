package info.mschmitt.support.databinding;

import android.databinding.BindingAdapter;
import android.view.View;

/**
 * @author Matthias Schmitt
 */
public class ViewBindingAdapter {
    @BindingAdapter("visibleGone")
    public static void setVisibleGone(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("visibleInvisible")
    public static void setVisibleInvisible(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @BindingAdapter("onFocusGain")
    public static void setOnNavigationItemSelected(View view, OnFocusGainListener listener) {
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                listener.onFocusGain(v);
            }
        });
    }

    public interface OnFocusGainListener {
        void onFocusGain(View sender);
    }
}
