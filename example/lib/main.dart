import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:timerly_plugin/models/CreateForegroundServiceRequest.dart';
import 'package:timerly_plugin/models/NotificaitonActionButton.dart';
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

    TimerlyPlugin.subscribeToNotificationEvents((message){
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
                    TimerlyPlugin.startForegroundService(
                        new CreateForegroundServiceRequest(
                            1,
                            "Some Title",
                            "Some Message",
                            "Some Big Title",
                            "Some Big Message",
                            false,
                            notificationPriority.IMPORTANCE_HIGH.index,
                            true,
                            "sampleChannel",
                            "10004", [
                      new NotificationActionButton("Play", "PLAY"),
                      new NotificationActionButton("Pause", "PAUSE")
                    ]));
                  },
                  child: Text("Start Foreground Service")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.stopForegroundService();
                  },
                  child: Text("Stop Foreground Service"))
            ],
          ),
        ),
      ),
    );
  }
}
