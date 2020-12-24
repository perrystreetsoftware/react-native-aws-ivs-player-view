import * as React from 'react';
import { StyleSheet, Button, View, Text } from 'react-native';
import PlayerView from 'react-native-aws-ivs-player-view';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  player: {
    width: '100%',
    height: '50%',
  },
});

export default function App() {
  const [bitrate, setBitrate] = React.useState(0);
  const [playbackState, setPlaybackState] = React.useState(0);

  return (
    <View style={styles.container}>
      <Text>Bitrate: {bitrate}</Text>
      <Text>Playback state: {playbackState}</Text>
      <PlayerView
        style={styles.player}
        maxBufferTimeSeconds={15}
        rebufferToLive={true}
        ref={(e: any) => {
          this.player = e;
        }}
        onDidChangeState={(data) => {
          console.log('Did change state: ' + JSON.stringify(data));
          setPlaybackState(data.state);
        }}
        onBitrateRecalculated={(data) => {
          console.log('Did recalculated bitrate: ' + JSON.stringify(data));
          setBitrate(data.bitrate);
        }}
      />
      <Button
        onPress={() => {
          this.player.load(
            'https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8'
          );
        }}
        title="Play"
      />
      <Button
        onPress={() => {
          this.player.pause();
        }}
        title="Pause"
      />
      <Button
        onPress={() => {
          this.player.mute();
        }}
        title="Mute"
      />
      <Button
        onPress={() => {
          this.player.unmute();
        }}
        title="Unmute"
      />
      <Button
        onPress={() => {
          this.player.stop();
        }}
        title="Stop"
      />
    </View>
  );
}
