#import "AwsIvsPlayerView.h"
#import <React/RCTUIManager.h>
#import <AmazonIVSPlayer/AmazonIVSPlayer.h>
#import "AwsIvsBitrateCalculator.h"
#import "AwsIvsAdapterPlayerView.h"

@implementation RCTConvert (IVSPlayerState)

RCT_ENUM_CONVERTER(IVSPlayerState, (@{
    @"IVSPlayerStateIdle": @(IVSPlayerStateIdle),
    @"IVSPlayerStateReady": @(IVSPlayerStateReady),
    @"IVSPlayerStateBuffering": @(IVSPlayerStateBuffering),
    @"IVSPlayerStatePlaying": @(IVSPlayerStatePlaying),
    @"IVSPlayerStateEnded": @(IVSPlayerStateEnded)
                                    }), IVSPlayerStateIdle, integerValue);
@end

@interface AwsIvsPlayerView()

@end

@implementation AwsIvsPlayerView

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

- (UIView *)view {
    return [[AwsIvsAdapterPlayerView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(onPlayerWillRebuffer, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDidChangeState, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDidChangeDuration, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDidOutputCue, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDidSeekToTime, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onBitrateRecalculated, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(maxBufferTimeSeconds, NSInteger)

RCT_EXPORT_METHOD(load:(NSNumber * __nonnull)reactTag url:(NSString *)urlString) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AwsIvsAdapterPlayerView *> *viewRegistry) {
        AwsIvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AwsIvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AwsIvsAdapterPlayerView, got: %@", view);
        }

        IVSPlayer *player = [[IVSPlayer alloc] init];
        player.delegate = view;
        view.playerView.player = player;

        NSURL *videoUrl = [NSURL URLWithString:urlString];
        view.url = videoUrl;
        view.isPaused = NO;
        [view.player load:videoUrl];

        // https://stackoverflow.com/a/45430673
        AVAudioSession *audioSession = [AVAudioSession sharedInstance];
        [audioSession setCategory:AVAudioSessionCategoryPlayback error:nil];
        [audioSession setActive:YES error:nil];
    }];
}

RCT_EXPORT_METHOD(pause:(NSNumber * __nonnull)reactTag) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AwsIvsAdapterPlayerView *> *viewRegistry) {
        AwsIvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AwsIvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AwsIvsAdapterPlayerView, got: %@", view);
        }

        view.isPaused = YES;
        [view.player pause];
    }];
}

RCT_EXPORT_METHOD(mute:(NSNumber * __nonnull)reactTag) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AwsIvsAdapterPlayerView *> *viewRegistry) {
        AwsIvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AwsIvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AwsIvsAdapterPlayerView, got: %@", view);
        }
        // Call your native component's method here
        [view.player setMuted:YES];
    }];
}

RCT_EXPORT_METHOD(unmute:(NSNumber * __nonnull)reactTag) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AwsIvsAdapterPlayerView *> *viewRegistry) {
        AwsIvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AwsIvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AwsIvsAdapterPlayerView, got: %@", view);
        }
        // Call your native component's method here
        [view.player setMuted:NO];
    }];
}

RCT_EXPORT_METHOD(volume:(NSNumber * __nonnull)reactTag level:(NSNumber *)level) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AwsIvsAdapterPlayerView *> *viewRegistry) {
        AwsIvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AwsIvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AwsIvsAdapterPlayerView, got: %@", view);
        }

        [view.player setVolume:level.floatValue];
    }];
}

RCT_EXPORT_METHOD(stop:(NSNumber * __nonnull)reactTag) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AwsIvsAdapterPlayerView *> *viewRegistry) {
        AwsIvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AwsIvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AwsIvsAdapterPlayerView, got: %@", view);
        }
        view.isPaused = YES;
        [view.player pause];
    }];
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (NSDictionary *)constantsToExport {
    return @{ @"IVSPlayerStateIdle":@(IVSPlayerStateIdle),
              @"IVSPlayerStateReady":@(IVSPlayerStateReady),
              @"IVSPlayerStateBuffering":@(IVSPlayerStateBuffering),
              @"IVSPlayerStatePlaying":@(IVSPlayerStatePlaying),
              @"IVSPlayerStateEnded":@(IVSPlayerStateEnded)
              };
}
@end
