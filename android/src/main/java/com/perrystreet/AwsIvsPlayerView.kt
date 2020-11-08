package com.perrystreet

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import com.amazonaws.ivs.player.*
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import java.util.*

class AwsIvsPlayerView : FrameLayout, LifecycleEventListener {
    private var mPlayerView: PlayerView? = null
    private var mPlayer: Player? = null
    private var mIsPaused = false
    private var mUri: Uri? = null
    private var mMaxBufferTimeInSeconds: Long = 10
    private var mPlayerListener: Player.Listener? = null
    private var mBitrateCalculator: AwsIvsBitrateCalculator? = null

    fun setMaxBufferTimeInSeconds(bufferTimeInSeconds: Long) {
        mMaxBufferTimeInSeconds = bufferTimeInSeconds
    }

    enum class Commands(private val mName: String) {
        COMMAND_LOAD("load"), COMMAND_PAUSE("pause"), COMMAND_MUTE("mute"), COMMAND_UNMUTE("unmute"), COMMAND_STOP("stop");

        override fun toString(): String {
            return mName
        }
    }

    enum class Events(private val mName: String) {
        EVENT_PLAYER_WILL_REBUFFER("onPlayerWillRebuffer"), EVENT_CHANGE_STATE("onDidChangeState"), EVENT_CHANGE_DURATION("onDidChangeDuration"), EVENT_OUTPUT_CUE("onDidOutputCue"), EVENT_SEEK_TIME("onDidSeekToTime"), EVENT_BITRATE_RECALCULATED("onBitrateRecalculated");

        override fun toString(): String {
            return mName
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        //Inflate xml resource, pass "this" as the parent, we use <merge> tag in xml to avoid
        //redundant parent, otherwise a LinearLayout will be added to this LinearLayout ending up
        //with two view groups
        inflate(getContext(), R.layout.player_view, this)

        val playerView = findViewById<PlayerView>(R.id.player_view)
        this.mPlayerView = playerView
        (context as ThemedReactContext).addLifecycleEventListener(this)

        val player = playerView.getPlayer()
        mPlayer = player

        mBitrateCalculator = AwsIvsBitrateCalculator(player, object : AwsIvsTransferListener {
            override fun onBitrateRecalculated(bitrate: Long) {
                notifyBitrateRecalculated(bitrate)
            }
        })
        val playerListener = object : Player.Listener() {
            override fun onCue(cue: Cue) {
                notifyDidOutputCue(cue)
            }

            override fun onDurationChanged(duration: Long) {
                notifyDidChangeDuration(duration)
            }

            override fun onStateChanged(state: Player.State) {
                onDidChangeState(state)
            }

            override fun onError(e: PlayerException) {}
            override fun onRebuffering() {
                notifyPlayerWillRebuffer()
            }

            override fun onSeekCompleted(time: Long) {
                notifyDidSeekToTime(time)
            }

            override fun onVideoSizeChanged(i: Int, i1: Int) {
                // https://stackoverflow.com/a/39838774/61072
                post(measureAndLayout)
            }

            override fun onQualityChanged(quality: Quality) {}
        }
        player.addListener(playerListener)
        this.mPlayerListener = playerListener
    }

    private fun reload() {
        mUri?.let {
            mPlayer?.load(it)
        }
    }

    private val measureAndLayout = Runnable {
        measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
        layout(left, top, right, bottom)
    }

    fun load(urlString: String) {
        mPlayer?.let { player ->
            val uri = Uri.parse(urlString)
            mIsPaused = false
            player.load(uri)
            this.mUri = uri
        } ?: run {
            Log.i(TAG, "Unable to play; not idle")
        }
    }

    fun pause() {
        mIsPaused = true
        mPlayer?.pause()
    }

    fun stop() {
        pause()
    }

    fun mute() {
        mPlayer?.isMuted = true
    }

    fun unMute() {
        mPlayer?.isMuted = false
    }

    override fun onHostResume() {
        Log.i(TAG, "Lifecycle: onHostResume")

//        if (this.getPlayOnResume()) {
//            play();
//        }
    }

    override fun onHostPause() {
        Log.i(TAG, "Lifecycle: onHostPause")

//        if (this.getPauseOnStop()) {
//            stop();
//        }
    }

    override fun onHostDestroy() {
        Log.i(TAG, "Lifecycle: onHostDestroy")
        cleanupMediaPlayerResources()
        release()
    }

    fun cleanupMediaPlayerResources() {
        mPlayerListener?.let { listener ->
           mPlayer?.removeListener(listener)
        }
        mPlayer?.release()
        mBitrateCalculator?.dispose()
    }

    fun release() {
        mPlayer?.release()
        mPlayer = null
    }

    fun onDidChangeState(state: Player.State) {
        when (state) {
            Player.State.IDLE -> if (!mIsPaused) {
                reload()
            }
            Player.State.BUFFERING -> {
            }
            Player.State.READY -> mPlayer?.play()
            Player.State.PLAYING -> {
                mPlayer?.let { player ->
                    Log.i(TAG, String.format("Buffered position is: %d", player.bufferedPosition))
                    if (player.bufferedPosition / 1000 >= mMaxBufferTimeInSeconds) {
                        Log.i(TAG, String.format("Buffered position exceeds: %d", mMaxBufferTimeInSeconds))
                        player.pause()
                    }
                }
            }
        }
        Log.i(TAG, String.format("onDidChangeState: %s", state.toString()))
        Log.i(TAG, String.format("Notify is %s", state))
        Log.i(TAG, String.format("Buffered is %d", mPlayer?.bufferedPosition))
        Log.i(TAG, String.format("LiveLowLatency is %d", mPlayer?.liveLatency))
        Log.i(TAG, String.format("Position is %d", mPlayer?.position))
        notifyDidChangeState(state)
    }

    private fun notifyPlayerWillRebuffer() {
        val event = Arguments.createMap()
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
                id,
                Events.EVENT_PLAYER_WILL_REBUFFER.toString(),
                event)
    }

    private fun notifyDidChangeState(state: Player.State) {
        val event = Arguments.createMap()
        event.putString("state", String.format(Locale.US, "%d", state.ordinal))
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
                id,
                Events.EVENT_CHANGE_STATE.toString(),
                event)
    }

    private fun notifyDidChangeDuration(duration: Long) {
        val event = Arguments.createMap()
        event.putString("duration", String.format(Locale.US, "%d", duration))
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
                id,
                Events.EVENT_CHANGE_DURATION.toString(),
                event)
    }

    private fun notifyDidSeekToTime(time: Long) {
        val event = Arguments.createMap()
        event.putString("time", String.format(Locale.US, "%d", time))
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
                id,
                Events.EVENT_SEEK_TIME.toString(),
                event)
    }

    private fun notifyDidOutputCue(cue: Cue) {
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
                id,
                Events.EVENT_OUTPUT_CUE.toString(),
                convertCueToMap(cue))
    }

    private fun convertCueToMap(cue: Cue): WritableMap {
        val event = Arguments.createMap()
        if (cue is TextMetadataCue) {
            event.putString("text", String.format(Locale.US, "%s", cue.text))
            event.putString("description", String.format(Locale.US, "%s", cue.description))
        } else if (cue is TextCue) {
            event.putString("text", String.format(Locale.US, "%s", cue.text))
        } else {
            event.putString("type", String.format(Locale.US, "%s", cue.javaClass.toString()))
            event.putString("start_time", String.format(Locale.US, "%d", cue.startTime))
            event.putString("end_time", String.format(Locale.US, "%d", cue.endTime))
        }
        return event
    }

    private fun notifyBitrateRecalculated(bitrate: Long) {
        val event = Arguments.createMap()
        event.putString("bitrate", String.format(Locale.US, "%d", bitrate))
        val reactContext = context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
                id,
                Events.EVENT_BITRATE_RECALCULATED.toString(),
                event)
    }

    companion object {
        private const val TAG = "RN_AwsIvsPlayerView"
    }
}