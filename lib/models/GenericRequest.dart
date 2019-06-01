class GenericRequest {
  int id;

  GenericRequest(this.id);

  GenericRequest.fromJson(Map<String, dynamic> json) : id = json["id"];

  Map<String, dynamic> toJson() => {"id": id};
}
