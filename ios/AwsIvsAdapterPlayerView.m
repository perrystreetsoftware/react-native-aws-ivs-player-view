//
//  AwsIvsAdapterPlayerView.m
//  react-native-aws-ivs-player-view
//
//  Created by Eric Silverberg on 11/6/20.
//

#import "AwsIvsAdapterPlayerView.h"
#import "AwsIvsBitrateCalculator.h"

@interface IVSCue (AwsIvsPlayerView)

@end

@implementation IVSCue (AwsIvsPlayerView)

- (NSDictionary *)toDictionary {
    if ([self isKindOfClass:[IVSTextMetadataCue class]]) {
        IVSTextMetadataCue *textMetadataCue = (IVSTextMetadataCue *)self;

        return @{@"text": textMetadataCue.text,
                 @"description": textMetadataCue.textDescription};
    } else if ([self isKindOfClass:[IVSTextCue class]]) {
        IVSTextCue *textCue = (IVSTextCue *)self;

        return @{@"text": textCue.text};
    } else {
        return @{@"type": self.type,
                 @"start_time": @(CMTimeGetSeconds(self.startTime)),
                 @"end_time": @(CMTimeGetSeconds(self.endTime))};
    }
}

@end

@implementation AwsIvsAdapterPlayerView

static const NSInteger kDefaultMaxBufferTimeInSeconds = 10;

- (IVSPlayer *)player {
    return _playerView.player;
}

- (instancetype)init {
    if (self = [super init]) {
        self.playerView = [[IVSPlayerView alloc] init];
        [self addSubview:self.playerView];
        self.playerView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

        self.maxBufferTimeSeconds = kDefaultMaxBufferTimeInSeconds;
        self.bitrateCalculator = [[AwsIvsBitrateCalculator alloc] init:self];
    }

    return self;
}

- (void)dealloc{
    [self cleanup];
}

- (void)cleanup {
    [self.bitrateCalculator cleanup];
}

- (void)handleBitrateRecalculated:(double)bitrate {
    if (self.onBitrateRecalculated) {
        self.onBitrateRecalculated(@{@"bitrate": @(self.bitrateCalculator.bitrate)});
    }
}

- (void)setRebufferToLive:(BOOL)rebufferToLive {
    [self.playerView.player performSelector:@selector(setRebufferToLive:)
                                 withObject:@(rebufferToLive)];
}

// MARK: - IVSPlayerDelegate

- (void)player:(IVSPlayer *)player didSeekToTime:(CMTime)time {
    NSLog(@"Seeked to time %@", @(CMTimeGetSeconds(time)));

    if (self.onDidSeekToTime) {
        self.onDidSeekToTime(@{@"time": @(CMTimeGetSeconds(time))});
    }
}

- (void)playerWillRebuffer:(IVSPlayer *)player {
    if (self.onPlayerWillRebuffer) {
        self.onPlayerWillRebuffer(@{});
    }
}

- (void)player:(IVSPlayer *)player didChangeDuration:(CMTime)duration {
    if (!CMTIME_IS_INDEFINITE(duration)) {
        NSLog(@"Changed duration to %@", @(CMTimeGetSeconds(duration)));

        if (self.onDidChangeDuration) {
            self.onDidChangeDuration(@{@"duration": @(CMTimeGetSeconds(duration))});
        }
    }
}

- (void)player:(IVSPlayer *)player didOutputCue:(__kindof IVSCue *)cue {
    NSLog(@"Did output Cue to %@", cue.type);

    if (self.onDidOutputCue) {
        self.onDidOutputCue([cue toDictionary]);
    }
}

- (void)player:(IVSPlayer *)player didChangeState:(IVSPlayerState)state {
    NSLog(@"Notify is %@", @(state));
    NSLog(@"Buffered is %@", @(CMTimeGetSeconds(player.buffered)));
    NSLog(@"LiveLowLatency is %@", @(player.liveLowLatency));
    NSLog(@"LiveLatency is %@", @(CMTimeGetSeconds(player.liveLatency)));

    if (self.onDidChangeState) {
        self.onDidChangeState(@{@"state": @(state)});
    }

    switch(state) {
        case IVSPlayerStateIdle:
            NSLog(@"State: Idle");

            if (!self.isPaused) {
                NSLog(@"State: not paused -- reloading");

                [self reload];
            } else {
                NSLog(@"State: we are paused -- not reloading %@", @(self.isPaused));
            }
            break;
        case IVSPlayerStateBuffering:
            NSLog(@"State: Buffering");
            break;
        case IVSPlayerStateEnded:
            NSLog(@"State: Ended");
            break;
        case IVSPlayerStateReady:
            NSLog(@"State: Ready");
            [player play];
            break;
        case IVSPlayerStatePlaying:
            NSLog(@"State: Playing");
            // If we have accumulated too much of a buffer,
            // then we need to move ourselves back into the
            // IVSPlayerStateIdle state, which will then trigger
            // a reload
            if (@(CMTimeGetSeconds(player.buffered)).integerValue >= self.maxBufferTimeSeconds) {
                [player pause];
            }
            break;
    }
}

- (void)reload {
    if (self.url) {
        [[self player] load:self.url];
    }
}

@end
