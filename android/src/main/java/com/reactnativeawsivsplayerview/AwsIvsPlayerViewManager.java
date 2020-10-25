package com.reactnativeawsivsplayerview;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.Map;

import javax.annotation.Nonnull;

public class AwsIvsPlayerViewManager extends SimpleViewManager<AwsIvsPlayerView> {
    public static final String REACT_CLASS = "AwsIvsPlayerView";

    @Nonnull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Nonnull
    @Override
    protected AwsIvsPlayerView createViewInstance(@Nonnull ThemedReactContext reactContext) {
        return new AwsIvsPlayerView(reactContext);
    }

    @Override
    public void onDropViewInstance(AwsIvsPlayerView view) {
        super.onDropViewInstance(view);
        view.cleanupMediaPlayerResources();
        view.release();
    }

//    @Override
//    @Nullable
//    public Map getExportedCustomDirectEventTypeConstants() {
//        MapBuilder.Builder builder = MapBuilder.builder();
//        for (RNRtmpView.Events event : RNRtmpView.Events.values()) {
//            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
//        }
//        return builder.build();
//    }

    @Override
    public Map<String,Integer> getCommandsMap() {
        MapBuilder.Builder builder = MapBuilder.builder();
        for (AwsIvsPlayerView.Commands command : AwsIvsPlayerView.Commands.values()) {
            builder.put(command.toString(), command.ordinal());
        }
        return builder.build();
    }

    @Override
    public void receiveCommand(AwsIvsPlayerView videoView, int commandId, @Nullable ReadableArray args) {
        AwsIvsPlayerView.Commands command = AwsIvsPlayerView.Commands.values()[commandId];

        switch (command){
            case COMMAND_LOAD:
                videoView.load(args.getString(0));
                break;
            case COMMAND_PAUSE:
                videoView.pause();
                break;
            default:
                break;
        }
    }
}
