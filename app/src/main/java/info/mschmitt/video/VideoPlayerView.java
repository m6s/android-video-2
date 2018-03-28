package info.mschmitt.video;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * @author Matthias Schmitt
 */
public class VideoPlayerView extends FrameLayout {
    private final SurfaceView surfaceView;
    private final ExtractorMediaSource.Factory mediaSourceFactory;
    private final VideoControlView videoControlView;
    private SimpleExoPlayer player;
    private PlayerInfo playerInfo;
    private final Player.DefaultEventListener playerListener = new Player.DefaultEventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            VideoPlayerView.this.onPlayerStateChanged(playWhenReady, playbackState);
        }
    };

    public VideoPlayerView(@NonNull Context context) {
        this(context, null, 0);
    }

    public VideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.video_player_view, this, true);
        setBackgroundColor(0xff000000);
        surfaceView = findViewById(R.id.surfaceView);
        videoControlView = findViewById(R.id.videoControlView);
        String userAgent = Util.getUserAgent(context, "Buzz");
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, userAgent);
        mediaSourceFactory = new ExtractorMediaSource.Factory(dataSourceFactory);
        updateViews();
    }

    public VideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private static TrackSelector newTrackSelector() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        return new DefaultTrackSelector(trackSelectionFactory);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (player != null && playerInfo.playWhenReadyOnAttach) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (player != null) {
            playerInfo.playWhenReadyOnAttach = player.getPlayWhenReady();
            player.setPlayWhenReady(false);
        }
    }

    private void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Format videoFormat = player.getVideoFormat();
        if (playerInfo.videoFormat == null && videoFormat != null) {
            playerInfo.videoFormat = videoFormat;
            applyVideoFormat();
        }
        if (!playerInfo.playbackEnded && playbackState == Player.STATE_ENDED) {
            playerInfo.playbackEnded = true;
        }
        if (!playerInfo.playbackReady && playbackState == Player.STATE_READY) {
            playerInfo.playbackReady = true;
        }
        if (playbackState != Player.STATE_ENDED) {
            playerInfo.playbackEnded = false;
        }
        if (playbackState != Player.STATE_READY) {
            playerInfo.playbackReady = false;
        }
    }

    private void applyVideoFormat() {
        int videoWidth = playerInfo.videoFormat.width;
        int videoHeight = playerInfo.videoFormat.height;
        int width = getWidth();
        int height = getHeight();
        float scaleFactor = (float) width / videoWidth;
        if (scaleFactor * videoHeight > height) {
            scaleFactor = (float) height / videoHeight;
        }
        surfaceView.getLayoutParams().width = (int) (videoWidth * scaleFactor);
        surfaceView.getLayoutParams().height = (int) (videoHeight * scaleFactor);
        surfaceView.requestLayout();
    }

    private void updateViews() {
        boolean playing = false;
        boolean playWhenReady = false;
        if (player != null) {
            int playbackState = player.getPlaybackState();
            playing = playbackState == Player.STATE_READY && player.getPlayWhenReady();
            playWhenReady = player.getPlayWhenReady();
        }
    }

    public void preparePlayer(String url) {
        Uri uri = Uri.parse(url);
        MediaSource mediaSource = mediaSourceFactory.createMediaSource(uri);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getContext(), newTrackSelector());
        player.prepare(mediaSource, true, true);
        setPlayer(player);
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(SimpleExoPlayer player) {
        this.player = player;
        playerInfo = new PlayerInfo();
        player.addListener(playerListener);
        videoControlView.setPlayer(player);
        player.setVideoSurfaceView(surfaceView);
//        player.addVideoDebugListener(new VideoRendererEventListener() {});
//        Player.VideoComponent newVideoComponent = player.getVideoComponent();
//        if (newVideoComponent != null) {
//            newVideoComponent.setVideoSurfaceView(surfaceView);
//        }
    }

    private static class PlayerInfo {
        Format videoFormat;
        boolean playWhenReadyOnAttach;
        boolean playbackEnded;
        boolean playbackReady;
    }
}
