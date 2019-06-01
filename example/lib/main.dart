import 'dart:convert';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:timerly_plugin/models/CreateForegroundServiceRequest.dart';
import 'package:timerly_plugin/models/NotificaitonActionButton.dart';
import 'package:timerly_plugin/models/RemoveNotificationRequet.dart';
import 'package:timerly_plugin/timerly_plugin.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();

    TimerlyPlugin.subscribeToNotificationEvents((message) {
      print("received Message, Yepii");
      print(message);
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await TimerlyPlugin.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  _subscription1(message) {
    var decodedMessage = json.decode(message);
    if (decodedMessage["notificationId"] != 1) return;
    switch (decodedMessage["command"]) {
      case "PLAY":
        break;
      case "PAUSE":
        TimerlyPlugin.removeNotification(RemoveNotificationRequest(1));
        break;
    }
  }

  _subscription2(message) {
    var decodedMessage = json.decode(message);
    if (decodedMessage["notificationId"] != 2) return;
    switch (decodedMessage["command"]) {
      case "PLAY":
        break;
      case "PAUSE":
        TimerlyPlugin.removeNotification(RemoveNotificationRequest(2));
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: new Center(
          child: Column(
            children: <Widget>[
              new Text('Running on: $_platformVersion\n'),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.addNotification(
                        new CreateForegroundServiceRequest(
                            120958,
                            "Some Title",
                            "Some Message",
                            "Some Big Title",
                            "Some Big Message",
                            false,
                            notificationPriority.IMPORTANCE_MAX.index,
                            true,
                            "sampleChannel",
                            "10004", [
                      new NotificationActionButton(1, "Play", "PLAY"),
                      new NotificationActionButton(2, "Pause", "PAUSE")
                    ]));
                    TimerlyPlugin.subscribeToNotificationEvents(_subscription1);
                  },
                  child: Text("Start Foreground Service")),
              RaisedButton(
                  onPressed: () {
                    print("Stoppinngggggg");
                    TimerlyPlugin.removeNotification(RemoveNotificationRequest(120958));
                  }, child: Text("Stop Foreground Service")),
              RaisedButton(onPressed: () {}, child: Text("Change Text")),
              SizedBox(
                height: 20.0,
              ),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.addNotification(
                        new CreateForegroundServiceRequest(
                            278345,
                            "Some Title 2",
                            "Some Message 2",
                            "Some Big Title 2",
                            "Some Big Message 2",
                            false,
                            notificationPriority.IMPORTANCE_MAX.index,
                            true,
                            "sampleChannel",
                            "10004", [
                      new NotificationActionButton(1, "Play", "PLAY"),
                      new NotificationActionButton(2, "Pause", "PAUSE")
                    ]));
                    TimerlyPlugin.subscribeToNotificationEvents(_subscription2);
                  },
                  child: Text("Start Foreground Service 2")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.removeNotification(RemoveNotificationRequest(278345));
                  }, child: Text("Stop Foreground Service 2")),
            ],
          ),
        ),
      ),
    );
  }
}
