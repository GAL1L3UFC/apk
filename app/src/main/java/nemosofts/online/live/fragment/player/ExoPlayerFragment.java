package nemosofts.online.live.fragment.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.SimpleExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.dash.DefaultDashChunkSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.smoothstreaming.DefaultSsChunkSource;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import nemosofts.online.live.R;
import nemosofts.online.live.utils.Events;
import nemosofts.online.live.utils.GlobalBus;

@UnstableApi
public class ExoPlayerFragment extends Fragment {

    private static final String TAG = "StreamPlayerActivity";
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;
    private DefaultBandwidthMeter BANDWIDTH_METER;
    private DataSource.Factory mediaDataSourceFactory;
    private ProgressBar progressBar;
    ImageView imgResize, imgFull, img_rewind, img_forward;
    public boolean isFullScr = false;
    Button btnTryAgain;
    String channelUrl, name, userAgentName;
    boolean isUserAgent;
    private static final String streamName = "streamName", streamUrl = "streamUrl", userAgent = "userAgent", userAgentOnOff = "userAgentOnOff";
    int RESIZE_MODE = 0;
    LinearLayout ll_more_op;
    RelativeLayout rl_video_top;
    ImageView iv_play;

    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    @NonNull
    public static ExoPlayerFragment newInstance(String videoUrl, String name, String userAgentName, boolean isUserAgent) {
        ExoPlayerFragment f = new ExoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(streamUrl, videoUrl);
        args.putString(streamName, name);
        args.putString(userAgent, userAgentName);
        args.putBoolean(userAgentOnOff, isUserAgent);
        f.setArguments(args);
        return f;
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exo_player, container, false);
        GlobalBus.getBus().register(this);

        if (getArguments() != null) {
            channelUrl = getArguments().getString(streamUrl);
            name = getArguments().getString(streamName);
            userAgentName = getArguments().getString(userAgent);
            isUserAgent = getArguments().getBoolean(userAgentOnOff, false);
        }

        rl_video_top = rootView.findViewById(R.id.rl_video_top);
        rl_video_top.setVisibility(isFullScr ? View.VISIBLE : View.GONE);

        progressBar = rootView.findViewById(R.id.pb_player);
        imgFull = rootView.findViewById(R.id.img_full_scr);
        imgResize = rootView.findViewById(R.id.img_resize_mode);
        img_rewind = rootView.findViewById(R.id.img_rewind);
        img_forward = rootView.findViewById(R.id.img_forward);
        btnTryAgain = rootView.findViewById(R.id.btn_try_again);
        ll_more_op = rootView.findViewById(R.id.ll_more_op);
        iv_play = rootView.findViewById(R.id.iv_play);

        rootView.findViewById(R.id.iv_back_player).setOnClickListener(v -> {
            if (isFullScr) {
                isFullScr = false;
                Events.FullScreen fullScreen = new Events.FullScreen();
                fullScreen.setFullScreen(false);
                GlobalBus.getBus().post(fullScreen);
            } else {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        TextView tv_player_title = rootView.findViewById(R.id.tv_player_title);
        tv_player_title.setText(name);

        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(requireActivity()).build();
        mediaDataSourceFactory = buildDataSourceFactory(true);
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        exoPlayer = new SimpleExoPlayer.Builder(requireActivity()).build();
        playerView = rootView.findViewById(R.id.exoPlayerView);
        playerView.setPlayer(exoPlayer);
        playerView.setUseController(true);
        playerView.requestFocus();

        Uri uri = Uri.parse(channelUrl);

        MediaSource mediaSource = buildMediaSource(uri);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);

        iv_play.setImageResource(R.drawable.ic_pause);
        exoPlayer.addListener(new Player.Listener() {

            @Override
            public void onTimelineChanged(@NotNull Timeline timeline, int reason) {
                Log.d(TAG, "onTimelineChanged: ");
            }

            @Override
            public void onTracksChanged(@NonNull Tracks tracks) {
                Player.Listener.super.onTracksChanged(tracks);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Player.Listener.super.onPlayerStateChanged(playWhenReady, playbackState);
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    iv_play.setImageResource(R.drawable.ic_pause);
                    progressBar.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                exoPlayer.stop();
                btnTryAgain.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                iv_play.setImageResource(R.drawable.ic_play);
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                Player.Listener.super.onPlayerError(error);
            }
        });

        imgFull.setOnClickListener(v -> {
            if (isFullScr) {
                isFullScr = false;
                Events.FullScreen fullScreen = new Events.FullScreen();
                fullScreen.setFullScreen(false);
                GlobalBus.getBus().post(fullScreen);
            } else {
                isFullScr = true;
                Events.FullScreen fullScreen = new Events.FullScreen();
                fullScreen.setFullScreen(true);
                GlobalBus.getBus().post(fullScreen);
            }
        });

        btnTryAgain.setOnClickListener(v -> {
            btnTryAgain.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            retryLoad();
        });

        imgResize.setOnClickListener(view -> setResize());
        img_rewind.setOnClickListener(v -> setRewind());
        img_forward.setOnClickListener(v -> setForward());
        imgResize.setVisibility(isFullScr ? View.VISIBLE : View.GONE);
        ll_more_op.setVisibility(isFullScr ? View.VISIBLE : View.GONE);

        final Boolean[] isVolume = {false};
        AudioManager audioManager = (AudioManager) requireActivity().getSystemService(Context.AUDIO_SERVICE);
        SeekBar volumeSeekBar = rootView.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setProgress(volume_level);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        ImageView iv_volume = rootView.findViewById(R.id.img_volume_up);
        volumeSeekBar.setVisibility(Boolean.TRUE.equals(isVolume[0]) ? View.VISIBLE : View.GONE);
        iv_volume.setOnClickListener(view -> {
            isVolume[0] = !Boolean.TRUE.equals(isVolume[0]);
            if (Boolean.TRUE.equals(isVolume[0])){
                volumeSeekBar.setProgress(volume_level);
            }
            iv_volume.setImageResource(Boolean.TRUE.equals(isVolume[0]) ? R.drawable.ic_close : R.drawable.ic_volume_up);
            volumeSeekBar.setVisibility(Boolean.TRUE.equals(isVolume[0]) ? View.VISIBLE : View.GONE);
        });

        iv_play.setOnClickListener(v -> {
            exoPlayer.setPlayWhenReady(!exoPlayer.getPlayWhenReady());
            iv_play.setImageResource(Boolean.TRUE.equals(exoPlayer.getPlayWhenReady()) ? R.drawable.ic_pause : R.drawable.ic_play);
        });

        return rootView;
    }

    private void setForward() {
        if (!playerView.isControllerFullyVisible()) {
            playerView.showController();
        }
        exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 10000);
    }

    private void setRewind() {
        if (!playerView.isControllerFullyVisible()) {
            playerView.showController();
        }
        long num = exoPlayer.getCurrentPosition() - 10000;
        if (num < 0){
            exoPlayer.seekTo(0);
        }else {
            exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 10000);
        }
    }

    public void retryLoad() {
        Uri uri = Uri.parse(channelUrl);
        MediaSource mediaSource = buildMediaSource(uri);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    @SuppressLint("SwitchIntDef")
    @NonNull
    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_SS -> {
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(false)).createMediaSource(MediaItem.fromUri(uri));
            }
            case C.TYPE_DASH -> {
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(false)).createMediaSource(MediaItem.fromUri(uri));
            }
            case C.TYPE_HLS -> {
                return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
            }
            case C.TYPE_RTSP -> {
                return new RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(uri));
            }
            case C.TYPE_OTHER -> {
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
            }
            default -> throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(requireActivity(), bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSource.Factory()
                .setUserAgent(isUserAgent ? userAgentName : Util.getUserAgent(requireActivity(), "ExoPlayerDemo"))
                .setTransferListener(bandwidthMeter)
                .setAllowCrossProtocolRedirects(true)
                .setKeepPostFor302Redirects(true);
    }

    private void setResize() {
        if (RESIZE_MODE == 0){
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            RESIZE_MODE = 1;
        } else if (RESIZE_MODE == 1){
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            RESIZE_MODE = 2;
        } else if (RESIZE_MODE == 2){
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            RESIZE_MODE = 3;
        } else if (RESIZE_MODE == 3){
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            RESIZE_MODE = 0;
        }
    }

    @Subscribe
    public void getFullScreen(@NonNull Events.FullScreen fullScreen) {
        isFullScr = fullScreen.isFullScreen();
        if (fullScreen.isFullScreen()) {
            rl_video_top.setVisibility(View.VISIBLE);
            imgFull.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            rl_video_top.setVisibility(View.GONE);
            imgFull.setImageResource(R.drawable.ic_fullscreen);
        }
        imgResize.setVisibility(fullScreen.isFullScreen() ? View.VISIBLE : View.GONE);
        rl_video_top.setVisibility(fullScreen.isFullScreen() ? View.VISIBLE : View.GONE);
        ll_more_op.setVisibility(fullScreen.isFullScreen() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (exoPlayer != null && exoPlayer.getPlayWhenReady()) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null && exoPlayer.getPlayWhenReady()) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop();
            exoPlayer.release();
        }
    }
}
