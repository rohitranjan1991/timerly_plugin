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
  int serviceId;
  String title;
  String message;
  String bigContentTitle;
  String bigContentMessage;
  bool autoCancel;
  int priority;
  bool setFullScreenIntent;
  String channelName;
  String channelId;
  List<NotificationActionButton> actionButtons;

  CreateForegroundServiceRequest(
      @required this.serviceId,
      @required this.title,
      @required this.message,
      this.bigContentTitle,
      this.bigContentMessage,
      this.autoCancel,
      this.priority,
      this.setFullScreenIntent,
      this.channelName,
      this.channelId,
      this.actionButtons);

  CreateForegroundServiceRequest.fromJson(Map<String, dynamic> json)
      : serviceId = json['serviceId'],
        title = json['title'],
        message = json['message'],
        bigContentTitle = json['bigContentTitle'],
        bigContentMessage = json['bigContentMessage'],
        autoCancel = json['autoCancel'],
        priority = json['priority'],
        setFullScreenIntent = json['setFullScreenIntent'],
        channelName = json['channelName'],
        channelId = json['channelId'],
        actionButtons = json['actionButtons']
            .map((i) => NotificationActionButton.fromJson(i))
            .toList();

  Map<String, dynamic> toJson() => {
        'serviceId': serviceId,
        'title': title,
        'message': message,
        'bigContentTitle': bigContentTitle,
        'bigContentMessage': bigContentMessage,
        'autoCancel': autoCancel,
        'priority': priority,
        'setFullScreenIntent': setFullScreenIntent,
        'channelName': channelName,
        'channelId': channelId,
        'actionButtons': actionButtons.map((f) => f.toJson()).toList(),
      };
}
