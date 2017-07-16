package info.mschmitt.support.databinding;

import android.databinding.BindingAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;

/**
 * @author Matthias Schmitt
 */
public class DrawerLayoutBindingAdapter {
    @BindingAdapter("toggle")
    public static void setToggle(DrawerLayout view, ActionBarDrawerToggle oldValue, ActionBarDrawerToggle newValue) {
        if (oldValue != null) {
            view.removeDrawerListener(oldValue);
        }
        if (newValue != null) {
            view.addDrawerListener(newValue);
        }
    }
}
