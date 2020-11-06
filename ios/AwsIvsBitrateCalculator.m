//
//  AwsIvsBitrateCalculator.m
//  react-native-aws-ivs-player-view
//
//  Created by Eric Silverberg on 11/6/20.
//

#import "AwsIvsBitrateCalculator.h"
#import "AwsIvsAdapterPlayerView.h"
#import <AmazonIVSPlayer/AmazonIVSPlayer.h>

@implementation AwsIvsBitrateCalculator

- (instancetype _Nonnull)init:(AwsIvsAdapterPlayerView * _Nonnull)playerView {
    if (self = [super init]) {
        self.playerViewAdapter = playerView;

        self.timer = [NSTimer scheduledTimerWithTimeInterval:1.0
                                                      target:self
                                                    selector:@selector(onTimer:)
                                                    userInfo:nil
                                                     repeats:YES];
    }

    return self;
}

- (void)onTimer:(id)sender {
    if (self.playerViewAdapter) {
        CMTime currentTime = self.playerViewAdapter.playerView.player.position;

        if (0 == CMTimeGetSeconds(lastCheckTime)) {
            lastCheckTime = currentTime;

            return;
        }

        lastCheckTime = currentTime;

        NSInteger calculatedBitrate = 0;

        if (self.playerViewAdapter.playerView.player.state == IVSPlayerStatePlaying) {
            calculatedBitrate = self.playerViewAdapter.playerView.player.videoBitrate;
        }
        self.bitrate = calculatedBitrate;

        [self.playerViewAdapter handleBitrateRecalculated:self.bitrate];

        NSLog(@"Bitrate recalculated %@", @(self.bitrate));
    }
}

- (void)cleanup {
    if (self.timer) {
        [self.timer invalidate];
        self.timer = nil;
    }
}

@end
