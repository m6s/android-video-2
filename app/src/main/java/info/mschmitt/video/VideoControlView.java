package info.mschmitt.video;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * @author Matthias Schmitt
 */
public class VideoControlView extends FrameLayout {
    private final View playButton;
    private SimpleExoPlayer player;

    public VideoControlView(@NonNull Context context) {
        this(context, null, 0);
    }

    public VideoControlView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.video_control_view, this, true);
        playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(this::onPlayClick);
    }

    public VideoControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void onPlayClick(View view) {
        player.setPlayWhenReady(true);
    }

    void setPlayer(SimpleExoPlayer player) {
        this.player = player;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }
}
