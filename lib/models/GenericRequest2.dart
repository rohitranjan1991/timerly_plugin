class GenericRequest2 {
  int id;
  var arg1;

  GenericRequest2(this.id, this.arg1);

  GenericRequest2.fromJson(Map<String, dynamic> json)
      : id = json["id"],
        arg1 = json["arg1"];

  Map<String, dynamic> toJson() => {"id": id, "arg1": arg1};
}
