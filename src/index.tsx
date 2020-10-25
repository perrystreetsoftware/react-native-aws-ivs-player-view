//
//  PlayerView.js
//
//  Created by Eric Silverberg on 2020/11/01.
//  Copyright © 2020 Perry Street Software. All rights reserved.
//

import React, { Component } from 'react';
import { PropTypes } from 'prop-types';
import {
  requireNativeComponent,
  View,
  UIManager,
  findNodeHandle,
} from 'react-native';

var RCT_IVS_VIDEO_REF = 'AwsIvsPlayerView';

class PlayerView extends Component {
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

  load(urlString) {
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

PlayerView.name = RCT_IVS_VIDEO_REF;
PlayerView.propTypes = {
  url: PropTypes.string,
  ...View.propTypes,
};

const NativeIvsPlayerView = requireNativeComponent(
  'AwsIvsPlayerView',
  PlayerView,
  {}
);

export default PlayerView;
