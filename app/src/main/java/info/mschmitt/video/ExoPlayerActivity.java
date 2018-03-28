package info.mschmitt.video;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import info.mschmitt.video.databinding.ExoPlayerActivityBinding;

public class ExoPlayerActivity extends AppCompatActivity {
    private ExoPlayerActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.exo_player_activity);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "VideoApplication"));
        ExtractorMediaSource.Factory mediaSourceFactory = new ExtractorMediaSource.Factory(dataSourceFactory);
        Uri uri = Uri.parse("file:///android_asset/ragnar.mp4");
        MediaSource mediaSource = mediaSourceFactory.createMediaSource(uri);
        player.prepare(mediaSource);
        binding.exoPlayerView.setPlayer(player);
    }

    @Override
    protected void onDestroy() {
        binding.exoPlayerView.getPlayer().release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        SimpleExoPlayer player = (SimpleExoPlayer) binding.exoPlayerView.getPlayer();
        player.setPlayWhenReady(false);
        super.onPause();
    }
}
