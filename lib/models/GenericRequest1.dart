class GenericRequest1 {
  int id;

  GenericRequest1(this.id);

  GenericRequest1.fromJson(Map<String, dynamic> json) : id = json["id"];

  Map<String, dynamic> toJson() => {"id": id};
}
