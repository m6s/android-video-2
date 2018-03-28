package info.mschmitt.video;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.android.exoplayer2.SimpleExoPlayer;
import info.mschmitt.video.databinding.VideoPlayerActivityBinding;

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoPlayerActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.video_player_activity);
        binding.playerView.preparePlayer("file:///android_asset/ragnar.mp4");
    }

    @Override
    protected void onDestroy() {
        binding.playerView.getPlayer().release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        SimpleExoPlayer player = binding.playerView.getPlayer();
        player.setPlayWhenReady(false);
        super.onPause();
    }
}
