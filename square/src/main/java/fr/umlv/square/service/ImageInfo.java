package fr.umlv.square.service;

import java.util.Objects;

class ImageInfo {
  final String name;
  final String dockerId;
  final int squareId;
  final int servicePort;
  final String dockerInstance;

  public ImageInfo(String name, String dockerId, String serviceDocker, int servicePort, int id) {
    this.name = Objects.requireNonNull(name);
    this.dockerId = Objects.requireNonNull(dockerId);
    this.dockerInstance = Objects.requireNonNull(serviceDocker);
    this.servicePort = Objects.requireNonNull(servicePort);
    this.squareId = Objects.requireNonNull(id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, dockerId, squareId, servicePort, dockerInstance);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ImageInfo))
      return false;

    ImageInfo imageInfo = (ImageInfo) obj;
    return imageInfo.squareId == squareId && imageInfo.servicePort == servicePort
        && imageInfo.name.equals(name) && imageInfo.dockerInstance.equals(dockerInstance)
        && imageInfo.dockerId.equals(dockerId);
  }

  @Override
  public String toString() {
    return "name: " + name + "; dockerID " + dockerId + "; squareId" + squareId + ": serviceDocker "
        + dockerInstance + "; servicePort :" + servicePort;
  }
}
