//
//  AwsIvsBitrateCalculator.h
//  react-native-aws-ivs-player-view
//
//  Created by Eric Silverberg on 11/6/20.
//

#import <Foundation/Foundation.h>
#import <CoreMedia/CoreMedia.h>

@class AwsIvsAdapterPlayerView;

@interface AwsIvsBitrateCalculator: NSObject {
    CMTime lastCheckTime;
    NSInteger calculatedBitrate;
}

// Avoid retain cycle with timers by using a separate object
@property (nonatomic, weak, nullable) AwsIvsAdapterPlayerView *playerViewAdapter;
@property (nonatomic, strong, nullable) NSTimer *timer;
@property (nonatomic) double bitrate;

- (instancetype _Nonnull)init:(AwsIvsAdapterPlayerView * _Nonnull)playerView;
- (void)cleanup;

@end
