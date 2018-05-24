package info.mschmitt.video;

import android.content.Context;
import android.widget.Scroller;

/**
 * @author Matthias Schmitt
 */
public class Scaler {
    private static final String TAG = "X";
    private final Scroller scroller;
    private int distance;
    private int oldCurrX;

    public Scaler(Context context) {
        scroller = new Scroller(context);
    }

    void abortAnimation() {
        scroller.abortAnimation();
    }

    void fling(int velocity) {
        oldCurrX = 0;
        distance = 0;
        scroller.fling(0, 0, velocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    boolean computeScrollOffset() {
        boolean b = scroller.computeScrollOffset();
        int currX = scroller.getCurrX();
        distance = oldCurrX - currX;
        oldCurrX = currX;
        return b;
    }

    int getDistance() {
        return distance;
    }

    boolean isFinished() {
        return scroller.isFinished();
    }

    float getMultiplier() {
        return 1;
    }
}
