//
//  AwsIvsAdapterPlayerView.h
//  react-native-aws-ivs-player-view
//
//  Created by Eric Silverberg on 11/6/20.
//

#import <Foundation/Foundation.h>
#import <AmazonIVSPlayer/AmazonIVSPlayer.h>
#import <React/RCTUIManager.h>

NS_ASSUME_NONNULL_BEGIN

@class AwsIvsBitrateCalculator;

@interface AwsIvsAdapterPlayerView : UIView <IVSPlayerDelegate>
@property (nonatomic, strong) IVSPlayerView *playerView;
@property (nonatomic, strong) AwsIvsBitrateCalculator *bitrateCalculator;
@property (nonatomic, copy) RCTBubblingEventBlock onPlayerWillRebuffer;
@property (nonatomic, copy) RCTBubblingEventBlock onDidChangeState;
@property (nonatomic, copy) RCTBubblingEventBlock onDidChangeDuration;
@property (nonatomic, copy) RCTBubblingEventBlock onDidOutputCue;
@property (nonatomic, copy) RCTBubblingEventBlock onDidSeekToTime;
@property (nonatomic, copy) RCTBubblingEventBlock onBitrateRecalculated;
@property (nonatomic, strong) NSURL *url;
@property (nonatomic) NSInteger maxBufferTimeSeconds;
@property (nonatomic) BOOL isPaused;

@property (nonatomic, readonly) IVSPlayer *player;

- (void)handleBitrateRecalculated:(double)bitrate;

@end

NS_ASSUME_NONNULL_END
