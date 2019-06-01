class StopwatchData {
  int id;
  String name;
  int initialTime;
  int currentTime;


  StopwatchData(this.id, this.name, this.initialTime, this.currentTime);

  StopwatchData.fromJson(Map<String, dynamic> json)
      : id = json["id"],
        name = json["name"],
        initialTime = json["initialTime"],
        currentTime = json["currentTime"];

  Map<String, dynamic> toJson() =>
      {
        "id": id,
        "name": name,
        "currentTime": currentTime,
        "initialTime": initialTime
      };
}

//data class Stopwatch(val id: Int, val initialTime: Long, var currentTime: Long, val name: String)