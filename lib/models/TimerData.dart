class TimerData {
  int id;
  String name;
  int initialTime;
  int currentTime;
  int alarmSoundValue;
  bool isPlaying;
  bool isFloatingWidgetDisplayed;

  TimerData(this.id, this.name, this.initialTime, this.currentTime,
      this.alarmSoundValue, this.isPlaying,this.isFloatingWidgetDisplayed);

  TimerData.fromJson(Map<String, dynamic> json)
      : id = json["id"],
        name = json["name"],
        initialTime = json["initialTime"],
        currentTime = json["currentTime"],
        alarmSoundValue = json["alarmSoundValue"],
        isPlaying = json["isPlaying"],
        isFloatingWidgetDisplayed = json["isFloatingWidgetDisplayed"];

  Map<String, dynamic> toJson() => {
        "id": id,
        "name": name,
        "currentTime": currentTime,
        "initialTime": initialTime,
        "alarmSoundValue": alarmSoundValue,
        "isPlaying": isPlaying,
        "isFloatingWidgetDisplayed": isFloatingWidgetDisplayed,
      };
}

//data class Stopwatch(val id: Int, val initialTime: Long, var currentTime: Long, val name: String)
