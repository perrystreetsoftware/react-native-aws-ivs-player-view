#import "AwsIvsPlayerView.h"
#import <React/RCTUIManager.h>
#import <AmazonIVSPlayer/AmazonIVSPlayer.h>

@implementation RCTConvert (IVSPlayerState)

RCT_ENUM_CONVERTER(IVSPlayerState, (@{
    @"IVSPlayerStateIdle": @(IVSPlayerStateIdle),
    @"IVSPlayerStateReady": @(IVSPlayerStateReady),
    @"IVSPlayerStateBuffering": @(IVSPlayerStateBuffering),
    @"IVSPlayerStatePlaying": @(IVSPlayerStatePlaying),
    @"IVSPlayerStateEnded": @(IVSPlayerStateEnded)
                                    }), IVSPlayerStateIdle, integerValue);
@end

static const NSInteger kDefaultMaxBufferTimeInSeconds = 10;

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

@interface IvsAdapterPlayerView : UIView <IVSPlayerDelegate>
@property (nonatomic, strong) IVSPlayerView *playerView;
@property (nonatomic, copy) RCTBubblingEventBlock onPlayerWillRebuffer;
@property (nonatomic, copy) RCTBubblingEventBlock onDidChangeState;
@property (nonatomic, copy) RCTBubblingEventBlock onDidChangeDuration;
@property (nonatomic, copy) RCTBubblingEventBlock onDidOutputCue;
@property (nonatomic, copy) RCTBubblingEventBlock onDidSeekToTime;
@property (nonatomic, strong) NSURL *url;
@property (nonatomic) NSInteger maxBufferTimeSeconds;
@property (nonatomic) BOOL isPaused;

@end

@implementation IvsAdapterPlayerView

- (IVSPlayer *)player {
    return _playerView.player;
}

- (instancetype)init {
    if (self = [super init]) {
        self.playerView = [[IVSPlayerView alloc] init];
        [self addSubview:self.playerView];
        self.playerView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

        self.maxBufferTimeSeconds = kDefaultMaxBufferTimeInSeconds;
    }

    return self;
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
    NSLog(@"Changed duration to %@", @(CMTimeGetSeconds(duration)));

    if (self.onDidChangeDuration) {
        self.onDidChangeDuration(@{@"duration": @(CMTimeGetSeconds(duration))});
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
    NSLog(@"Position is %@", @(CMTimeGetSeconds(player.liveLatency)));

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

@interface AwsIvsPlayerView()

@end

@implementation AwsIvsPlayerView

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

- (UIView *)view {
    return [[IvsAdapterPlayerView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(onPlayerWillRebuffer, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDidChangeState, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDidChangeDuration, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDidOutputCue, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDidSeekToTime, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(maxBufferTimeSeconds, NSInteger)

RCT_EXPORT_METHOD(load:(NSNumber * __nonnull)reactTag url:(NSString *)urlString) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, IvsAdapterPlayerView *> *viewRegistry) {
        IvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[IvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting IvsAdapterPlayerView, got: %@", view);
        }

        IVSPlayer *player = [[IVSPlayer alloc] init];
        player.delegate = view;
        view.playerView.player = player;

        NSURL *videoUrl = [NSURL URLWithString:urlString];
        view.url = videoUrl;
        view.isPaused = NO;
        [view.player load:videoUrl];
    }];
}

RCT_EXPORT_METHOD(pause:(NSNumber * __nonnull)reactTag) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, IvsAdapterPlayerView *> *viewRegistry) {
        IvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[IvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting IvsAdapterPlayerView, got: %@", view);
        }

        view.isPaused = YES;
        [view.player pause];
    }];
}

RCT_EXPORT_METHOD(mute:(NSNumber * __nonnull)reactTag) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, IvsAdapterPlayerView *> *viewRegistry) {
        IvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[IvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting IvsAdapterPlayerView, got: %@", view);
        }
        // Call your native component's method here
        [view.player setMuted:YES];
    }];
}

RCT_EXPORT_METHOD(unmute:(NSNumber * __nonnull)reactTag) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, IvsAdapterPlayerView *> *viewRegistry) {
        IvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[IvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting IvsAdapterPlayerView, got: %@", view);
        }
        // Call your native component's method here
        [view.player setMuted:NO];
    }];
}

RCT_EXPORT_METHOD(volume:(NSNumber * __nonnull)reactTag level:(NSNumber *)level) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, IvsAdapterPlayerView *> *viewRegistry) {
        IvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[IvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting IvsAdapterPlayerView, got: %@", view);
        }

        [view.player setVolume:level.floatValue];
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
