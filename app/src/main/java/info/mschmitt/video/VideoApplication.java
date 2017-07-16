package info.mschmitt.video;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

/**
 * @author Matthias Schmitt
 */
public class VideoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
