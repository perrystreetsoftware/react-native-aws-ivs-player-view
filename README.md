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

## Contributing

Please open PRs or issues and we will merge accordingly.

## License

MIT
