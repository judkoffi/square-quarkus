package fr.umlv.square.model.service;

import java.util.Objects;

/**
 * Model class use to store information read from docker ps command
 */

public class ImageInfo {
  private final String imageName;
  private final long created;
  private final int appPort;
  private final int servicePort;
  private final String dockerInstance;
  private final int squareId;
  private boolean isAlive;

  public String getImageName() {
    return imageName;
  }

  public long getCreated() {
    return created;
  }

  public int getAppPort() {
    return appPort;
  }

  public int getServicePort() {
    return servicePort;
  }

  public String getDockerInstance() {
    return dockerInstance;
  }

  public int getSquareId() {
    return squareId;
  }

  public ImageInfo(String image, long created, int appPort, int servicePort, String dockerInstance,
      int squareId) {
    this.imageName = image;
    this.created = created;
    this.appPort = appPort;
    this.servicePort = servicePort;
    this.dockerInstance = dockerInstance;
    this.squareId = squareId;
    this.isAlive = true;
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

  public void updateIsAlive(boolean status) {
    isAlive = status;
  }
}
