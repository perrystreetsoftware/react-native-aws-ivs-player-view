package com.reactnativeawsivsplayerview;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.amazonaws.ivs.player.Cue;
import com.amazonaws.ivs.player.Player;
import com.amazonaws.ivs.player.PlayerException;
import com.amazonaws.ivs.player.PlayerView;
import com.amazonaws.ivs.player.Quality;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.Locale;

public class AwsIvsPlayerView extends FrameLayout implements LifecycleEventListener {
    private static final String TAG = "RN_AwsIvsPlayerView";

    private PlayerView mPlayerView;
    private Player mPlayer;

    public enum Commands {
        COMMAND_LOAD("load"),
        COMMAND_PAUSE("pause"),
        COMMAND_MUTE("mute"),
        COMMAND_UNMUTE("unmute");

        private final String mName;

        Commands(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    public enum Events {
      EVENT_CHANGE_STATE("onDidChangeState");

      private final String mName;

      Events(final String name) {
        mName = name;
      }

      @Override
      public String toString() {
        return mName;
      }
    }

    public AwsIvsPlayerView(Context context) {
        super(context);
        init(context);
    }

    public AwsIvsPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AwsIvsPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //Inflate xml resource, pass "this" as the parent, we use <merge> tag in xml to avoid
        //redundant parent, otherwise a LinearLayout will be added to this LinearLayout ending up
        //with two view groups
        inflate(getContext(), R.layout.player_view,this);

        mPlayerView = findViewById(R.id.player_view);
        ((ThemedReactContext)context).addLifecycleEventListener(this);

        Player player = mPlayerView.getPlayer();
        mPlayer = player;
        player.addListener(new Player.Listener() {
            @Override
            public void onCue(@NonNull Cue cue) {

            }

            @Override
            public void onDurationChanged(long l) {

            }

            @Override
            public void onStateChanged(@NonNull Player.State state) {
                switch (state) {
                    case BUFFERING:
                        // player is buffering
                        break;
                    case READY:
                        mPlayer.play();
                        break;
                    case IDLE:
                        break;
                    case PLAYING:
                        // playback started
                        break;
                }

                AwsIvsPlayerView.this.onDidChangeState(state);
            }

            @Override
            public void onError(@NonNull PlayerException e) {

            }

            @Override
            public void onRebuffering() {

            }

            @Override
            public void onSeekCompleted(long l) {

            }

            @Override
            public void onVideoSizeChanged(int i, int i1) {
                // https://stackoverflow.com/a/39838774/61072
                post(measureAndLayout);
            }

            @Override
            public void onQualityChanged(@NonNull Quality quality) {

            }
        });
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    public void load(String urlString) {
        if (mPlayer != null) {
            Uri uri = Uri.parse(urlString);

            mPlayer.load(uri);
        } else {
            Log.i(TAG, "Unable to play; not idle");
        }
    }

    public void pause() {
        if (this.mPlayer != null) {
            mPlayer.pause();
        }
    }

    public void mute() {
      if (this.mPlayer != null) {
        mPlayer.setMuted(true);
      }
    }

    public void unMute() {
      if (this.mPlayer != null) {
        mPlayer.setMuted(false);
      }
    }

    @Override
    public void onHostResume() {
        Log.i(TAG, "Lifecycle: onHostResume");

//        if (this.getPlayOnResume()) {
//            play();
//        }
    }

    @Override
    public void onHostPause() {
        Log.i(TAG, "Lifecycle: onHostPause");

//        if (this.getPauseOnStop()) {
//            stop();
//        }
    }

    @Override
    public void onHostDestroy() {
        Log.i(TAG, "Lifecycle: onHostDestroy");
        cleanupMediaPlayerResources();
        release();
    }

    public void cleanupMediaPlayerResources() {
    }

    public void release() {
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void onDidChangeState(@NonNull Player.State state) {
        Log.i(TAG, String.format("onDidChangeState: %s", state.toString()));

        WritableMap event = Arguments.createMap();
        event.putString("state", String.format(Locale.US, "%d", state.ordinal()));

        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            Events.EVENT_CHANGE_STATE.toString(),
            event);
    }
}
