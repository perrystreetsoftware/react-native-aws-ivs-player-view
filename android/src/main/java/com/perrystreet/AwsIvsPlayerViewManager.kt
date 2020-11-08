package com.perrystreet

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import javax.annotation.Nonnull

class AwsIvsPlayerViewManager : SimpleViewManager<AwsIvsPlayerView>() {
    @Nonnull
    override fun getName(): String {
        return REACT_CLASS
    }

    @Nonnull
    override fun createViewInstance(@Nonnull reactContext: ThemedReactContext): AwsIvsPlayerView {
        return AwsIvsPlayerView(reactContext)
    }

    override fun onDropViewInstance(view: AwsIvsPlayerView) {
        super.onDropViewInstance(view)
        view.cleanupMediaPlayerResources()
        view.release()
    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any?>? {
        val builder: MapBuilder.Builder<String, Any?> = MapBuilder.builder<String, Any?>()
        for (event in AwsIvsPlayerView.Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()))
        }
        return builder.build()
    }

    override fun getCommandsMap(): Map<String, Int>? {
        val builder: MapBuilder.Builder<String, Int> = MapBuilder.builder<String, Int>()
        for (command in AwsIvsPlayerView.Commands.values()) {
            builder.put(command.toString(), command.ordinal)
        }
        return builder.build()
    }

    override fun receiveCommand(videoView: AwsIvsPlayerView, commandId: Int, args: ReadableArray?) {
        val command = AwsIvsPlayerView.Commands.values()[commandId]
        when (command) {
            AwsIvsPlayerView.Commands.COMMAND_LOAD -> videoView.load(args!!.getString(0))
            AwsIvsPlayerView.Commands.COMMAND_PAUSE -> videoView.pause()
            AwsIvsPlayerView.Commands.COMMAND_MUTE -> videoView.mute()
            AwsIvsPlayerView.Commands.COMMAND_UNMUTE -> videoView.unMute()
            AwsIvsPlayerView.Commands.COMMAND_STOP -> videoView.stop()
            else -> {
            }
        }
    }

    @ReactProp(name = PROP_MAX_BUFFER_TIME_SECONDS)
    fun setMaxBufferTimeSeconds(videoView: AwsIvsPlayerView, bufferTimeInSeconds: Float) {
        videoView.setMaxBufferTimeInSeconds(bufferTimeInSeconds.toLong())
    }

    companion object {
        const val REACT_CLASS = "AwsIvsPlayerView"
        const val PROP_MAX_BUFFER_TIME_SECONDS = "maxBufferTimeSeconds"
    }
}