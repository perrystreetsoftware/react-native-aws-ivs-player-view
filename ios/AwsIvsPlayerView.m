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

@interface IvsAdapterPlayerView : UIView <IVSPlayerDelegate>
@property (nonatomic, strong) IVSPlayerView *playerView;
@property (nonatomic, copy) RCTBubblingEventBlock onDidChangeState;

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
    }

    return self;
}

// MARK: - IVSPlayerDelegate

- (void)player:(IVSPlayer *)player didChangeState:(IVSPlayerState)state {
    if (state == IVSPlayerStateReady) {
        [player play];
    }

    if (self.onDidChangeState) {
        NSLog(@"Notify is %@", @(state));

            self.onDidChangeState(@{@"state": @(state)});
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

RCT_EXPORT_VIEW_PROPERTY(onDidChangeState, RCTBubblingEventBlock)

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

        [view.player load:videoUrl];
    }];
}

RCT_EXPORT_METHOD(pause:(NSNumber * __nonnull)reactTag) {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, IvsAdapterPlayerView *> *viewRegistry) {
        IvsAdapterPlayerView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[IvsAdapterPlayerView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting IvsAdapterPlayerView, got: %@", view);
        }

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
