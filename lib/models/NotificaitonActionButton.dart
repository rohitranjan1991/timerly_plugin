class NotificationActionButton {
  String name;
  String command;

  NotificationActionButton(this.name, this.command);

  NotificationActionButton.fromJson(Map<String, dynamic> json)
      : name = json['name'],
        command = json['command'];

  Map<String, dynamic> toJson() => {
        'name': name,
        'command': command,
      };
}
