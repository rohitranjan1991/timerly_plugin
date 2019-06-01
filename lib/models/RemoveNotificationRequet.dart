class RemoveNotificationRequest {
  int notificationId;

  RemoveNotificationRequest(this.notificationId);

  RemoveNotificationRequest.fromJson(Map<String, dynamic> json)
      : notificationId = json['notificationId'];

  Map<String, dynamic> toJson() => {"notificationId": notificationId};
}
