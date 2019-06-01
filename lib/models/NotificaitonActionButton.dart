class NotificationActionButton {
  int notificationId;
  String name;
  String command;

  NotificationActionButton(this.notificationId, this.name, this.command);

  NotificationActionButton.fromJson(Map<String, dynamic> json)
      : notificationId = json['notificationId'],
        name = json['name'],
        command = json['command'];

  Map<String, dynamic> toJson() => {
        'notificationId': notificationId,
        'name': name,
        'command': command,
      };
}
