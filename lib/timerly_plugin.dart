import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:timerly_plugin/models/CreateForegroundServiceRequest.dart';

class TimerlyPlugin {
  static const codec = JSONMessageCodec();
  static const MethodChannel _channel = const MethodChannel('timerly_plugin');
  static const notificationEventStream =
      const EventChannel('com.timerly/notification/stream');

  static StreamSubscription _timerSubscription = null;

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> startForegroundService(
      CreateForegroundServiceRequest createForegroundServiceRequest) async {
    print("asdasdasdasdasdasd");
    print(json.encode(createForegroundServiceRequest.toJson()));
    var hasStarted = await _channel.invokeMethod('startForegroundService',
        {"data": json.encode(createForegroundServiceRequest.toJson())});
    return hasStarted;
  }

  static Future<bool> stopForegroundService() async {
    var hasStarted = await _channel.invokeMethod('stopForegroundService');
    return hasStarted;
  }

  static subscribeToNotificationEvents(Function func) {
    _timerSubscription =
        notificationEventStream.receiveBroadcastStream().listen((message){
          func(message);
        });
  }

  static unsubscribeToNotificationEvents(Function func) {
    if (_timerSubscription != null) {
      _timerSubscription.cancel();
      _timerSubscription = null;
    }
  }
}
