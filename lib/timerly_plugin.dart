import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:timerly_plugin/models/CreateForegroundServiceRequest.dart';

import 'models/GenericRequest.dart';
import 'models/RemoveNotificationRequet.dart';
import 'models/StopwatchData.dart';
import 'models/TimerData.dart';

class TimerlyPlugin {
  static const codec = JSONMessageCodec();
  static const MethodChannel _channel = const MethodChannel('timerly_plugin');
  static const notificationEventStream =
      const EventChannel('com.timerly/notification/event');

  static List<Function> subscriberList1 = new List();

  static StreamSubscription _notificationSubscription;

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static subscribeToNotificationEvents(Function func) {
    if (Platform.isAndroid) {
      subscriberList1.add(func);
      if (_notificationSubscription == null)
        _notificationSubscription =
            notificationEventStream.receiveBroadcastStream().listen((message) {
//              print(message);
          subscriberList1.forEach((f) => f(message));
        });
    }
  }

  static unsubscribeToNotificationEvents(Function func) {
    if (Platform.isAndroid && _notificationSubscription != null) {
      if (subscriberList1.isNotEmpty) {
        subscriberList1.remove(func);
      } else {
        _notificationSubscription.cancel();
        _notificationSubscription = null;
      }
    }
  }

  static addTimer(TimerData timer) async {
    if (Platform.isAndroid) {
      await _channel
          .invokeMethod('addTimer', {"data": json.encode(timer.toJson())});
    }
  }

  static startTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'startTimer', {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static stopTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'stopTimer', {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static removeTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'removeTimer', {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static lapTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'lapTimer', {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static resetTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'resetTimer', {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static updateTimer(TimerData timer) async {
    if (Platform.isAndroid) {
      await _channel
          .invokeMethod('updateTimer', {"data": json.encode(timer.toJson())});
    }
  }

  // Stopwatch Commands
  static addStopwatch(StopwatchData stopwatch) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'addStopwatch', {"data": json.encode(stopwatch.toJson())});
    }
  }

  static startStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'startStopwatch', {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static stopStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'stopStopwatch', {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static removeStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('removeStopwatch',
          {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static resetStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'resetStopwatch', {"data": json.encode(GenericRequest(id).toJson())});
    }
  }

  static updateStopwatch(StopwatchData stopwatch) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'updateStopwatch', {"data": json.encode(stopwatch.toJson())});
    }
  }
}
