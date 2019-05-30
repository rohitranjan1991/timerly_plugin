import 'dart:async';
import 'dart:convert';
import 'dart:io';

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
    if (Platform.isAndroid) {
      var hasStarted = await _channel.invokeMethod('startForegroundService',
          {"data": json.encode(createForegroundServiceRequest.toJson())});
      return hasStarted;
    }
    return false;
  }

  static Future<bool> updateForegroundService(
      CreateForegroundServiceRequest createForegroundServiceRequest) async {
    if (Platform.isAndroid) {
      var hasStarted = await _channel.invokeMethod('updateForegroundService',
          {"data": json.encode(createForegroundServiceRequest.toJson())});
      return hasStarted;
    }
    return false;
  }

  static Future<bool> stopForegroundService() async {
    if (Platform.isAndroid) {
      var hasStarted = await _channel.invokeMethod('stopForegroundService');
      return hasStarted;
    }
    return false;
  }

  static subscribeToNotificationEvents(Function func) {
    if (Platform.isAndroid) {
      _timerSubscription =
          notificationEventStream.receiveBroadcastStream().listen((message) {
        func(message);
      });
    }
  }

  static unsubscribeToNotificationEvents() {
    if (Platform.isAndroid && _timerSubscription != null) {
      _timerSubscription.cancel();
      _timerSubscription = null;
    }
  }
}
