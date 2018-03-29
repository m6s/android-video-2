package info.mschmitt.video;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import info.mschmitt.video.databinding.TimeBarActivityBinding;

public class TimeBarActivity extends AppCompatActivity {
    private TimeBarActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.time_bar_activity);
    }
}
