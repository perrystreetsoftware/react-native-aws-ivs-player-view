# react-native-aws-ivs-player-view

React Native view bindings for the [AWS Interactive Video Service](https://aws.amazon.com/ivs/)

## Installation

```sh
npm install react-native-aws-ivs-player-view
```

## Usage

```js
import PlayerView from "react-native-aws-ivs-player-view";

// ...
<PlayerView
    style={styles.player}
    ref={(e: any) => {
        this.player = e;
    }}
/>
<Button
    onPress={() => {
        this.player.pause();
    }}
    title="Pause"
/>
<Button
    onPress={() => {
        this.player.load(
        '<SOME_HLS_URL>'
        );
    }}
    title="Play Me"
/>

```

## Development

If you are interested in extending or enhacning this project, first clone it, then:

Run

```
yarn bootstrap
```

When the bootstrap is done, you will be able to start the example app by executing one of the following commands:

```
# Android app
yarn example android
# iOS app
yarn example ios
```

## Requirements

iOS minimum: 12.0

Android minimum: 21 ([per AWS](https://github.com/aws-samples/amazon-ivs-player-android-sample/blob/master/basicplayback/build.gradle))

## Contributing

Please open PRs or issues and we will merge accordingly.

## License

MIT
