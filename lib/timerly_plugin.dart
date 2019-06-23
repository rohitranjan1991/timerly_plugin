import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';

import 'models/GenericRequest1.dart';
import 'models/GenericRequest2.dart';
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

  static getAllWidgets() async {
    if (Platform.isAndroid) {
      return await _channel.invokeMethod('getAllWidgets');
    }
  }
  
  static getWidgetsById(int id) async {
    if (Platform.isAndroid) {
      return await _channel.invokeMethod(
          'getWidgetsById', {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }


  /// Stopwatch Commands

  static addStopwatch(StopwatchData timer) async {
    if (Platform.isAndroid) {
      await _channel
          .invokeMethod('addStopwatch', {"data": json.encode(timer.toJson())});
    }
  }

  static startStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'startStopwatch', {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static stopStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'stopStopwatch', {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static removeStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'removeStopwatch', {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static lapStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'lapStopwatch', {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static resetStopwatch(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'resetStopwatch', {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static updateStopwatchName(GenericRequest2 gr) async {
    if (Platform.isAndroid) {
      await _channel
          .invokeMethod('updateStopwatchName', {"data": json.encode(gr.toJson())});
    }
  }

  static toggleStopwatchFloatingWidget(GenericRequest1 gr) async{
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'toggleStopwatchFloatingWidget', {'data': json.encode(gr.toJson())});
    }
  }

  // Timer Commands
  static addTimer(TimerData timer) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'addTimer', {"data": json.encode(timer.toJson())});
    }
  }

  static startTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('startTimer',
          {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static stopTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'stopTimer', {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static removeTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('removeTimer',
          {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static resetTimer(int id) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod('resetTimer',
          {"data": json.encode(GenericRequest1(id).toJson())});
    }
  }

  static updateTimerInitialTimer(GenericRequest2 gr) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'updateInitialTimeTimer', {"data": json.encode(gr.toJson())});
    }
  }

  static updateTimerName(GenericRequest2 gr) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'updateTimerName', {"data": json.encode(gr.toJson())});
    }
  }

  static playAlarm(GenericRequest2 gr) async {
    if (Platform.isAndroid) {
      await _channel
          .invokeMethod('playAlarm', {'data': json.encode(gr.toJson())});
    }
  }

  static stopAlarm(GenericRequest2 gr) async {
    if (Platform.isAndroid) {
      await _channel
          .invokeMethod('stopAlarm', {'data': json.encode(gr.toJson())});
    }
  }

  static updateTimerAlarm(GenericRequest2 gr) async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'updateTimerAlarm', {'data': json.encode(gr.toJson())});
    }
  }

  static toggleTimerFloatingWidget(GenericRequest1 gr) async{
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'toggleTimerFloatingWidget', {'data': json.encode(gr.toJson())});
    }
  }

  //Misc

  static updateAppVisibilityState(GenericRequest1 gr) async{
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'updateAppVisibilityState', {'data': json.encode(gr.toJson())});
    }
  }

  static testCmd1() async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'testCmd1');
    }
  }

  static testCmd2() async {
    if (Platform.isAndroid) {
      await _channel.invokeMethod(
          'testCmd2');
    }
  }
}
