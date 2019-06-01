class TimerLap {
  int count;
  int atTime;

  TimerLap(this.count, this.atTime);

  TimerLap.fromJson(Map<String, dynamic> json)
      : count = json["count"],
        atTime = json["atTime"];

  Map<String, dynamic> toJson() => {'count': count, 'atTimer': atTime};
}
