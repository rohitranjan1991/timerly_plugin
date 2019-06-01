import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:timerly_plugin/models/CreateForegroundServiceRequest.dart';

import 'models/RemoveNotificationRequet.dart';

class TimerlyPlugin {
  static const codec = JSONMessageCodec();
  static const MethodChannel _channel = const MethodChannel('timerly_plugin');
  static const notificationEventStream =
  const EventChannel('com.timerly/notification/stream');
  static List<Function> subscriberList1 = new List();
  static StreamSubscription _timerSubscription;

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> addNotification(
      CreateForegroundServiceRequest createForegroundServiceRequest) async {
    if (Platform.isAndroid) {
      var hasStarted = await _channel.invokeMethod('addNotification',
          {"data": json.encode(createForegroundServiceRequest.toJson())});
      return hasStarted;
    }
    return false;
  }

  static Future<bool> updateForegroundService(
      CreateForegroundServiceRequest createForegroundServiceRequest) async {
    if (Platform.isAndroid) {
      var hasStarted = await _channel.invokeMethod('updateNotification',
          {"data": json.encode(createForegroundServiceRequest.toJson())});
      return hasStarted;
    }
    return false;
  }

  static Future<bool> removeNotification(
      RemoveNotificationRequest removeNotificationRequest) async {
    var hasStarted = await _channel.invokeMethod('removeNotification',{"data": json.encode(removeNotificationRequest.toJson())});
    return hasStarted;
  }

  static subscribeToNotificationEvents(Function func) {
    if (Platform.isAndroid) {
      subscriberList1.add(func);
      if (_timerSubscription == null)
        _timerSubscription =
            notificationEventStream.receiveBroadcastStream().listen((message) {
              subscriberList1.forEach((f) => f(message));
            });
    }
  }

  static unsubscribeToNotificationEvents(Function func) {
    if (Platform.isAndroid && _timerSubscription != null) {
      if (subscriberList1.isNotEmpty) {
        subscriberList1.remove(func);
      } else {
        _timerSubscription.cancel();
        _timerSubscription = null;
      }
    }
  }
}
