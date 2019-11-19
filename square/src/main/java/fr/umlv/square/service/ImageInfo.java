package fr.umlv.square.service;

import java.util.Objects;

/**
 * Model class use to store information read from docker ps command
 */
class ImageInfo {
  final String imageName;
  final long created;
  final int appPort;
  final int servicePort;
  final String dockerInstance;
  final int squareId;

  public ImageInfo(String image, long created, int appPort, int servicePort, String dockerInstance,
      int squareId) {
    this.imageName = image;
    this.created = created;
    this.appPort = appPort;
    this.servicePort = servicePort;
    this.dockerInstance = dockerInstance;
    this.squareId = squareId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(imageName, created, appPort, servicePort, dockerInstance, squareId);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ImageInfo))
      return false;
    ImageInfo info = (ImageInfo) obj;
    return info.squareId == squareId && info.created == created && info.appPort == appPort
        && info.servicePort == servicePort && info.dockerInstance.equals(dockerInstance)
        && info.imageName.equals(imageName);
  }

  @Override
  public String toString() {
    return "ImageInfo [imageName=" + imageName + ", created=" + created + ", appPort=" + appPort
        + ", servicePort=" + servicePort + ", dockerInstance=" + dockerInstance + ", squareId="
        + squareId + "]";
  }
}
