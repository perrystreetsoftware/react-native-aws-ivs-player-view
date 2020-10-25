import * as React from 'react';
import { StyleSheet, Button, View } from 'react-native';
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
  return (
    <View style={styles.container}>
      <PlayerView
        style={styles.player}
        ref={(e: any) => {
          this.player = e;
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
    </View>
  );
}
