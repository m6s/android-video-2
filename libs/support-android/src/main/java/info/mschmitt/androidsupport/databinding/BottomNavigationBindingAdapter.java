package info.mschmitt.support.databinding;

import android.databinding.BindingAdapter;
import android.support.design.widget.BottomNavigationView;

/**
 * @author Matthias Schmitt
 */
public class BottomNavigationBindingAdapter {
    @BindingAdapter("onNavigationItemSelected")
    public static void setOnNavigationItemSelected(BottomNavigationView view,
                                                   BottomNavigationView.OnNavigationItemSelectedListener listener) {
        view.setOnNavigationItemSelectedListener(listener);
    }
}
