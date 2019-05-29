import 'package:meta/meta.dart';
import 'package:timerly_plugin/models/NotificaitonActionButton.dart';

enum notificationPriority {
  IMPORTANCE_NONE,
  IMPORTANCE_MIN,
  IMPORTANCE_LOW,
  IMPORTANCE_DEFAULT,
  IMPORTANCE_HIGH,
  IMPORTANCE_MAX
}

class CreateForegroundServiceRequest {
  int serviceID;
  String title;
  String message;
  String bigContentTitle;
  String bigContentMessage;
  bool autoCancel;
  int priority;
  bool setFullScreenIntent;
  String channelName;
  String channelID;
  List<NotificationActionButton> actionButtons;

  CreateForegroundServiceRequest(
      @required this.serviceID,
      @required this.title,
      @required this.message,
      this.bigContentTitle,
      this.bigContentMessage,
      this.autoCancel,
      this.priority,
      this.setFullScreenIntent,
      this.channelName,
      this.channelID,
      this.actionButtons);

  CreateForegroundServiceRequest.fromJson(Map<String, dynamic> json)
      : serviceID = json['serviceID'],
        title = json['title'],
        message = json['message'],
        bigContentTitle = json['bigContentTitle'],
        bigContentMessage = json['bigContentMessage'],
        autoCancel = json['autoCancel'],
        priority = json['priority'],
        setFullScreenIntent = json['setFullScreenIntent'],
        channelName = json['channelName'],
        channelID = json['channelID'],
        actionButtons = json['actionButtons']
            .map((i) => NotificationActionButton.fromJson(i))
            .toList();

  Map<String, dynamic> toJson() => {
        'serviceID': serviceID,
        'title': title,
        'message': message,
        'bigContentTitle': bigContentTitle,
        'bigContentMessage': bigContentMessage,
        'autoCancel': autoCancel,
        'priority': priority,
        'setFullScreenIntent': setFullScreenIntent,
        'channelName': channelName,
        'channelID': channelID,
        'actionButtons': actionButtons.map((f) => f.toJson()).toList(),
      };
}
