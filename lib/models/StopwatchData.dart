import 'StopwatchLap.dart';

class StopwatchData {
  int id;
  String name;
  int currentTime;
  List<StopwatchLap> laps;
  bool isPlaying;
  bool isFloatingWidgetDisplayed;

  StopwatchData(this.id, this.name, this.currentTime, this.laps,this.isPlaying,this.isFloatingWidgetDisplayed);

  StopwatchData.fromJson(Map<String, dynamic> json)
      : id = json["id"],
        name = json["name"],
        currentTime = json["currentTime"],
        laps = json["laps"].map((i) => StopwatchLap.fromJson(i)).toList(),
        isPlaying = json["isPlaying"],
        isFloatingWidgetDisplayed = json["isFloatingWidgetDisplayed"];

  Map<String, dynamic> toJson() => {
        "id": id,
        "name": name,
        "currentTime": currentTime,
        "laps": laps.map((f) => f.toJson()).toList(),
        "isPlaying": isPlaying,
        "isFloatingWidgetDisplayed": isFloatingWidgetDisplayed,
      };
}