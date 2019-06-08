class TimerData {
  int id;
  String name;
  int initialTime;
  int currentTime;
  int alarmValue;

  TimerData(
      this.id, this.name, this.initialTime, this.currentTime, this.alarmValue);

  TimerData.fromJson(Map<String, dynamic> json)
      : id = json["id"],
        name = json["name"],
        initialTime = json["initialTime"],
        currentTime = json["currentTime"],
        alarmValue = json["alarmValue"];

  Map<String, dynamic> toJson() => {
        "id": id,
        "name": name,
        "currentTime": currentTime,
        "initialTime": initialTime,
        "alarmValue": alarmValue,
      };
}

//data class Stopwatch(val id: Int, val initialTime: Long, var currentTime: Long, val name: String)
