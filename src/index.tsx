//
//  PlayerView.js
//
//  Created by Eric Silverberg on 2020/11/01.
//  Copyright © 2020 Perry Street Software. All rights reserved.
//

import React, { Component } from 'react';
import {
  requireNativeComponent,
  UIManager,
  findNodeHandle,
  ViewStyle,
  HostComponent,
} from 'react-native';

var RCT_IVS_VIDEO_REF = 'AwsIvsPlayerView';

interface IAwsIvsPlayerView {
  onPlayerWillRebuffer?(any): any;
  onDidChangeState?(any): any;
  onDidChangeDuration?(any): any;
  onDidOutputCue?(any): any;
  onDidSeekToTime?(any): any;
  onBitrateRecalculated?(any): any;
  maxBufferTimeSeconds: number;
  rebufferToLive: boolean;
  style?: ViewStyle;
}

class PlayerView extends Component<IAwsIvsPlayerView> {
  constructor(props) {
    super(props);
  }

  _onPlayerWillRebuffer = (event) => {
    if (!this.props.onPlayerWillRebuffer) {
      return;
    }
    this.props.onPlayerWillRebuffer(event.nativeEvent);
  };

  _onDidChangeState = (event) => {
    if (!this.props.onDidChangeState) {
      return;
    }
    this.props.onDidChangeState(event.nativeEvent);
  };

  _onDidChangeDuration = (event) => {
    if (!this.props.onDidChangeDuration) {
      return;
    }
    this.props.onDidChangeDuration(event.nativeEvent);
  };

  _onDidOutputCue = (event) => {
    if (!this.props.onDidOutputCue) {
      return;
    }
    this.props.onDidOutputCue(event.nativeEvent);
  };

  _onDidSeekToTime = (event) => {
    if (!this.props.onDidSeekToTime) {
      return;
    }
    this.props.onDidSeekToTime(event.nativeEvent);
  };

  _onBitrateRecalculated = (event) => {
    if (!this.props.onBitrateRecalculated) {
      return;
    }
    this.props.onBitrateRecalculated(event.nativeEvent);
  };

  initialize() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.getViewManagerConfig('AwsIvsPlayerView').Commands.initialize,
      []
    );
  }

  pause() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.getViewManagerConfig('AwsIvsPlayerView').Commands.pause,
      []
    );
  }

  mute() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.getViewManagerConfig('AwsIvsPlayerView').Commands.mute,
      []
    );
  }

  unmute() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.getViewManagerConfig('AwsIvsPlayerView').Commands.unmute,
      []
    );
  }

  stop() {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.getViewManagerConfig('AwsIvsPlayerView').Commands.stop,
      []
    );
  }

  load(urlString: String) {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.getViewManagerConfig('AwsIvsPlayerView').Commands.load,
      [urlString]
    );
  }

  render() {
    return (
      <NativeIvsPlayerView
        {...this.props}
        ref={RCT_IVS_VIDEO_REF}
        onPlayerWillRebuffer={this._onPlayerWillRebuffer.bind(this)}
        onDidChangeState={this._onDidChangeState.bind(this)}
        onDidChangeDuration={this._onDidChangeDuration.bind(this)}
        onDidOutputCue={this._onDidOutputCue.bind(this)}
        onDidSeekToTime={this._onDidSeekToTime.bind(this)}
        onBitrateRecalculated={this._onBitrateRecalculated.bind(this)}
      />
    );
  }
}

interface INativeIvsPlayer {
  onPlayerWillRebuffer?(any): any;
  onDidChangeState?(any): any;
  onDidChangeDuration?(any): any;
  onDidOutputCue?(any): any;
  onDidSeekToTime?(any): any;
  onBitrateRecalculated?(any): any;

  maxBufferTimeSeconds: number;
  rebufferToLive: boolean;
}

const NativeIvsPlayerView: HostComponent<INativeIvsPlayer> = requireNativeComponent(
  'AwsIvsPlayerView'
);

export default PlayerView;
