package com.reactnativeawsivsplayerview;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.amazonaws.ivs.player.Cue;
import com.amazonaws.ivs.player.TextCue;
import com.amazonaws.ivs.player.TextMetadataCue;
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
  private boolean mIsPaused = false;
  private Uri mUri;
  private long mMaxBufferTimeInSeconds = 10;

  public void setMaxBufferTimeInSeconds(long bufferTimeInSeconds) {
    this.mMaxBufferTimeInSeconds = bufferTimeInSeconds;
  }

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
    EVENT_PLAYER_WILL_REBUFFER("onPlayerWillRebuffer"),
    EVENT_CHANGE_STATE("onDidChangeState"),
    EVENT_CHANGE_DURATION("onDidChangeDuration"),
    EVENT_OUTPUT_CUE("onDidOutputCue"),
    EVENT_SEEK_TIME("onDidSeekToTime");

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
        AwsIvsPlayerView.this.notifyDidOutputCue(cue);
      }

      @Override
      public void onDurationChanged(long duration) {
        AwsIvsPlayerView.this.notifyDidChangeDuration(duration);
      }

      @Override
      public void onStateChanged(@NonNull Player.State state) {
        AwsIvsPlayerView.this.onDidChangeState(state);
      }

      @Override
      public void onError(@NonNull PlayerException e) {

      }

      @Override
      public void onRebuffering() {
        AwsIvsPlayerView.this.notifyPlayerWillRebuffer();
      }

      @Override
      public void onSeekCompleted(long time) {
        AwsIvsPlayerView.this.notifyDidSeekToTime(time);
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

  private void reload() {
    if (this.mUri != null) {
      mPlayer.load(this.mUri);
    }
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
      this.mUri = Uri.parse(urlString);

      mIsPaused = false;
      mPlayer.load(this.mUri);
    } else {
      Log.i(TAG, "Unable to play; not idle");
    }
  }

  public void pause() {
    if (this.mPlayer != null) {
      mIsPaused = true;
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
    switch (state) {
      case IDLE:
        if (!AwsIvsPlayerView.this.mIsPaused) {
          AwsIvsPlayerView.this.reload();
        }
        break;
      case BUFFERING:
        // player is buffering
        break;
      case READY:
        mPlayer.play();
        break;
      case PLAYING:
        Log.i(TAG, String.format("Buffered position is: %d", this.mPlayer.getBufferedPosition()));

        if (this.mPlayer.getBufferedPosition() / 1000 >= this.mMaxBufferTimeInSeconds) {
          Log.i(TAG, String.format("Buffered position exceeds: %d", this.mMaxBufferTimeInSeconds));
          mPlayer.pause();
        }
        // playback started
        break;
    }

    Log.i(TAG, String.format("onDidChangeState: %s", state.toString()));
    Log.i(TAG, String.format("Notify is %s", state));
    Log.i(TAG, String.format("Buffered is %d", this.mPlayer.getBufferedPosition()));
    Log.i(TAG, String.format("LiveLowLatency is %d", this.mPlayer.getLiveLatency()));
    Log.i(TAG, String.format("Position is %d", this.mPlayer.getPosition()));

    notifyDidChangeState(state);
  }

  private void notifyPlayerWillRebuffer() {
    WritableMap event = Arguments.createMap();

    ReactContext reactContext = (ReactContext)getContext();
    reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            Events.EVENT_PLAYER_WILL_REBUFFER.toString(),
            event);
  }

  private void notifyDidChangeState(@NonNull Player.State state) {
    WritableMap event = Arguments.createMap();
    event.putString("state", String.format(Locale.US, "%d", state.ordinal()));

    ReactContext reactContext = (ReactContext)getContext();
    reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            Events.EVENT_CHANGE_STATE.toString(),
            event);
  }

  private void notifyDidChangeDuration(@NonNull long duration) {
    WritableMap event = Arguments.createMap();
    event.putString("duration", String.format(Locale.US, "%d", duration));

    ReactContext reactContext = (ReactContext)getContext();
    reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            Events.EVENT_CHANGE_DURATION.toString(),
            event);
  }

  private void notifyDidSeekToTime(@NonNull long time) {
    WritableMap event = Arguments.createMap();
    event.putString("time", String.format(Locale.US, "%d", time));

    ReactContext reactContext = (ReactContext)getContext();
    reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            Events.EVENT_SEEK_TIME.toString(),
            event);
  }

  private void notifyDidOutputCue(@NonNull Cue cue) {
    ReactContext reactContext = (ReactContext)getContext();
    reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            Events.EVENT_OUTPUT_CUE.toString(),
            convertCueToMap(cue));
  }

  private WritableMap convertCueToMap(Cue cue) {
    WritableMap event = Arguments.createMap();

    if (cue instanceof TextMetadataCue) {
      event.putString("text", String.format(Locale.US, "%s", ((TextMetadataCue)cue).text));
      event.putString("description", String.format(Locale.US, "%s", ((TextMetadataCue)cue).description));
    } else if (cue instanceof TextCue) {
      event.putString("text", String.format(Locale.US, "%s", ((TextCue)cue).text));
    } else {
      event.putString("type", String.format(Locale.US, "%s", cue.getClass().toString()));
      event.putString("start_time", String.format(Locale.US, "%d", cue.startTime));
      event.putString("end_time", String.format(Locale.US, "%d", cue.endTime));
    }

    return event;
  }
}
