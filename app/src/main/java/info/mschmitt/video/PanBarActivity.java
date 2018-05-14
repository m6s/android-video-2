package info.mschmitt.video;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import info.mschmitt.video.databinding.PanBarActivityBinding;

public class PanBarActivity extends AppCompatActivity {
    private PanBarActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.pan_bar_activity);
        binding.panBar.setStrokeInterval(10000);
        binding.panBar.setPosition(0);
        binding.panBar.setDuration(305700);
        binding.panBar.setDragTimeIncrement(1000, TypedValue.COMPLEX_UNIT_DIP, 1);
    }
}
