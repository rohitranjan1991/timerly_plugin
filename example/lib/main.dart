import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:timerly_plugin/models/Timer.dart';
import 'package:timerly_plugin/timerly_plugin.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  int timerValue = 0;

  @override
  void initState() {
    super.initState();
    initPlatformState();

    TimerlyPlugin.subscribeToNotificationEvents((message) {
      var msg = json.decode(message);
      setState(() {
        timerValue = json.decode(msg["data"])["currentTime"];
      });
      print("${json.decode(msg["data"])}");
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
              Text("Timer Value = ${timerValue}"),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.addTimer(TimerData(1, "timer 1", 0, []));
                  },
                  child: Text("Add Timer")),
              RaisedButton(
                  onPressed: () {
                    /*new CreateForegroundServiceRequest(
                        9213,
                        "Timerly StopWatch Widget",
                        "Timer Stoped",
                        "Timerly StopWatch Widget",
                        "Timer Stoped",
                        false,
                        notificationPriority.IMPORTANCE_LOW.index,
                        true,
                        "Timerly Stopwatch Notification",
                        "100456", [])*/
                    TimerlyPlugin.startTimer(1);
                  },
                  child: Text("Start Timer")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.lapTimer(1);
                  },
                  child: Text("Lap Timer")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.stopTimer(1);
                  },
                  child: Text("Stop Timer")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.removeTimer(1);
                  },
                  child: Text("Remove Timer")),
              SizedBox(
                height: 30,
              ),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.addTimer(TimerData(2, "timer 2", 0, []));
                  },
                  child: Text("Add Timer")),
              RaisedButton(
                  onPressed: () {
                    /*new CreateForegroundServiceRequest(
                        9223,
                        "Timerly StopWatch Widget",
                        "Timer Stoped",
                        "Timerly StopWatch Widget",
                        "Timer Stoped",
                        false,
                        notificationPriority.IMPORTANCE_LOW.index,
                        true,
                        "Timerly Stopwatch Notification",
                        "200456", [])*/
                    TimerlyPlugin.startTimer(2);
                  },
                  child: Text("Start Timer")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.lapTimer(2);
                  },
                  child: Text("Lap Timer")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.stopTimer(2);
                  },
                  child: Text("Stop Timer")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.removeTimer(2);
                  },
                  child: Text("Remove Timer")),
            ],
          ),
        ),
      ),
    );
  }
}
