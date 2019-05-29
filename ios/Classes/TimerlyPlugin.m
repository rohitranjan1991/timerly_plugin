#import "TimerlyPlugin.h"
#import <timerly_plugin/timerly_plugin-Swift.h>

@implementation TimerlyPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTimerlyPlugin registerWithRegistrar:registrar];
}
@end
