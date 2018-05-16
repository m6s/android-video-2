package info.mschmitt.video;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import com.google.android.exoplayer2.util.Util;
import info.mschmitt.video.databinding.PanBarActivityBinding;

import java.util.Formatter;
import java.util.Locale;

public class PanBarActivity extends AppCompatActivity {
    static final int DURATION = 305700;
    private PanBarActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

        binding = DataBindingUtil.setContentView(this, R.layout.pan_bar_activity);
        binding.panBar.setDragTimeIncrement(500, TypedValue.COMPLEX_UNIT_DIP, 100);
        binding.panBar.setPosition(0);
        binding.panBar.setDuration(DURATION);
        binding.pbPositionView.setText(Util.getStringForTime(formatBuilder, formatter, 0));
        binding.pbDurationView.setText(Util.getStringForTime(formatBuilder, formatter, DURATION));
        binding.timeBar.setPosition(0);
        binding.timeBar.setDuration(DURATION);
        binding.tbPositionView.setText(Util.getStringForTime(formatBuilder, formatter, 0));
        binding.tbDurationView.setText(Util.getStringForTime(formatBuilder, formatter, DURATION));
    }
}
