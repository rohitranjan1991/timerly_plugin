import 'TimerLap.dart';

class TimerData {
  int id;
  String name;
  int currentTime;
  List<TimerLap> laps;

  TimerData(this.id, this.name, this.currentTime, this.laps);

  TimerData.fromJson(Map<String, dynamic> json)
      : id = json["id"],
        name = json["name"],
        currentTime = json["currentTime"],
        laps = json["laps"].map((i) => TimerLap.fromJson(i)).toList();

  Map<String, dynamic> toJson() => {
        "id": id,
        "name": name,
        "currentTime": currentTime,
        "laps": laps.map((f) => f.toJson()).toList()
      };
}