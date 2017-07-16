package info.mschmitt.video;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
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
import info.mschmitt.video.databinding.MainActivityBinding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MainActivity extends AppCompatActivity {
    private static final String STATE_VIEW_MODEL = "VIEW_MODEL";
    public MainViewModel viewModel;
    private boolean postResumed;
    private MainActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onPreCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        // https://stackoverflow.com/a/28041425/2317680
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        binding.setActivity(this);
        if (binding.simpleExoPlayerView.getPlayer() == null) {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
            SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            binding.simpleExoPlayerView.setPlayer(player);
        }
        if (viewModel.uri == null) {
            processIntent(getIntent());
        } else {
            preparePlayer();
        }
    }

    private void processIntent(Intent intent) {
        String action = intent.getAction();
        if (!Intent.ACTION_VIEW.equals(action)) {
            return;
        }
        viewModel.uri = intent.getData();
        viewModel.notifyChange();
        if (viewModel.uri == null) {
            return;
        }
        preparePlayer();
    }

    private void onPreCreate(Bundle savedInstanceState) {
        viewModel = savedInstanceState == null ? new MainViewModel()
                : (MainViewModel) savedInstanceState.getSerializable(STATE_VIEW_MODEL);
    }

    private void preparePlayer() {
        SimpleExoPlayer player = binding.simpleExoPlayerView.getPlayer();
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "VideoApplication"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource =
                new ExtractorMediaSource(viewModel.uri, dataSourceFactory, extractorsFactory, null, null);
        player.prepare(mediaSource);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        postResumed = true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        binding.simpleExoPlayerView.getPlayer().release();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public void onBackPressed() {
        if (!postResumed) {
            // https://www.reddit.com/r/androiddev/comments/4d2aje/ever_launched_a_fragmenttransaction_in_response/
            // https://developer.android.com/topic/libraries/support-library/revisions.html#26-0-0-beta1
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        SimpleExoPlayer player = binding.simpleExoPlayerView.getPlayer();
        // Store off if we were playing so we know if we should start when we're foregrounded again.
        viewModel.playVideoWhenForegrounded = player.getPlayWhenReady();
        // Store off the last position our player was in before we paused it.
        viewModel.lastPosition = player.getCurrentPosition();
        // Pause the player
        player.setPlayWhenReady(false);
        postResumed = false;
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SimpleExoPlayer player = binding.simpleExoPlayerView.getPlayer();
        // Seek to the last position of the player.
        player.seekTo(viewModel.lastPosition);
        // Put the player into the last state we were in.
        player.setPlayWhenReady(viewModel.playVideoWhenForegrounded);
    }

    public static class MainViewModel extends BaseObservable implements Serializable {
        @Bindable public transient Uri uri;
        private boolean playVideoWhenForegrounded;
        private long lastPosition;

        private void writeObject(ObjectOutputStream oop) throws IOException {
            try {
                oop.defaultWriteObject();
                oop.writeUTF(uri != null ? uri.toString() : "");
            } catch (IOException e) {
                throw new Error(e);
            }
        }

        private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
            try {
                ois.defaultReadObject();
                String uriString = ois.readUTF();
                if (!uriString.isEmpty()) {
                    uri = Uri.parse(uriString);
                }
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
}
