class StopwatchLap {
  int count;
  int atTime;

  StopwatchLap(this.count, this.atTime);

  StopwatchLap.fromJson(Map<String, dynamic> json)
      : count = json["count"],
        atTime = json["atTime"];

  Map<String, dynamic> toJson() => {'count': count, 'atTimer': atTime};
}
