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
  onDidChangeState?(any): any,
  style?: ViewStyle
}

class PlayerView extends Component<IAwsIvsPlayerView> {
  constructor(props) {
    super(props);
  }

  _onDidChangeState = (event) => {
    if (!this.props.onDidChangeState) {
      return;
    }
    this.props.onDidChangeState(event.nativeEvent);
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
        ref={RCT_IVS_VIDEO_REF}
        onLoadState={this._onDidChangeState.bind(this)}
        {...this.props}
      />
    );
  }
}

// PlayerView.name = RCT_IVS_VIDEO_REF;
// PlayerView.propTypes = {
//   url: PropTypes.string,
//   ...View.propTypes,
// };

interface INativeIvsPlayer {
  onLoadState?(any): any
}

const NativeIvsPlayerView: HostComponent<INativeIvsPlayer> = requireNativeComponent('AwsIvsPlayerView');

export default PlayerView;
