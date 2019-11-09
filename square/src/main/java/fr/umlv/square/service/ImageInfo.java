package fr.umlv.square.service;

import java.util.Objects;

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

  @Override
  public int hashCode() {
    return Objects
      .hash(containerId, imageName, command, created, status, appPort, servicePort, dockerInstance,
          squareId);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ImageInfo))
      return false;
    ImageInfo info = (ImageInfo) obj;
    return info.squareId == squareId && info.appPort == appPort && info.servicePort == servicePort
        && info.dockerInstance.equals(dockerInstance) && info.imageName.equals(imageName)
        && info.containerId.equals(containerId) && info.status.equals(status)
        && info.created.equals(created) && info.command.equals(command);
  }

  @Override
  public String toString() {
    return "ImageInfo [containerId=" + containerId + ", imageName=" + imageName + ", command="
        + command + ", created=" + created + ", status=" + status + ", appPort=" + appPort
        + ", servicePort=" + servicePort + ", dockerInstance=" + dockerInstance + ", squareId="
        + squareId + "]";
  }
}
