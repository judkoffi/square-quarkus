package fr.umlv.square.service;

class ImageInfo {
  final String containerId;
  final String imageName;
  final String command;
  final String created;
  final String status;
  final int appPort;
  final int servicePort;
  final String dockerInstance;
  final int squareId;

  public ImageInfo(String containerId, String image, String command, String created, String status,
      int appPort, int servicePort, String dockerInstance, int squareId) {
    this.containerId = containerId;
    this.imageName = image;
    this.command = command;
    this.created = created;
    this.status = status;
    this.appPort = appPort;
    this.servicePort = servicePort;
    this.dockerInstance = dockerInstance;
    this.squareId = squareId;
  }
}
