import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:timerly_plugin/models/StopwatchData.dart';
import 'package:timerly_plugin/models/TimerData.dart';
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
              Text("Stopwatch Value = $timerValue"),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.addStopwatch(
                        StopwatchData(1, "stopwatch 1", 10, 0, 1));
                  },
                  child: Text("Add StopWatch")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.startStopwatch(1);
                  },
                  child: Text("Start StopWatch")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.stopStopwatch(1);
                  },
                  child: Text("Stop StopWatch")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.removeStopwatch(1);
                  },
                  child: Text("Remove StopWatch")),
              SizedBox(
                height: 30,
              ),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.addStopwatch(
                        StopwatchData(2, "stopwatch 2", 40, 0, 1));
                  },
                  child: Text("Add StopWatch")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.startStopwatch(2);
                  },
                  child: Text("Start StopWatch")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.stopStopwatch(2);
                  },
                  child: Text("Stop StopWatch")),
              RaisedButton(
                  onPressed: () {
                    TimerlyPlugin.removeStopwatch(2);
                  },
                  child: Text("Remove StopWatch")),
            ],
          ),
        ),
      ),
    );
  }
}
