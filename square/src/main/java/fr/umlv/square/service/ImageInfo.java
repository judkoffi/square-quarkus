package fr.umlv.square.service;

class ImageInfo {
  /*
   * final String name; final String dockerId; final int squareId; final int servicePort; final
   * String dockerInstance;
   * 
   * 
   * public ImageInfo(String name, String dockerId, String serviceDocker, int servicePort, int id) {
   * this.name = Objects.requireNonNull(name); this.dockerId = Objects.requireNonNull(dockerId);
   * this.dockerInstance = Objects.requireNonNull(serviceDocker); this.servicePort =
   * Objects.requireNonNull(servicePort); this.squareId = Objects.requireNonNull(id); }
   * 
   * @Override public int hashCode() { return Objects.hash(name, dockerId, squareId, servicePort,
   * dockerInstance); }
   * 
   * @Override public boolean equals(Object obj) { if (!(obj instanceof ImageInfo)) return false;
   * 
   * ImageInfo imageInfo = (ImageInfo) obj; return imageInfo.squareId == squareId &&
   * imageInfo.servicePort == servicePort && imageInfo.name.equals(name) &&
   * imageInfo.dockerInstance.equals(dockerInstance) && imageInfo.dockerId.equals(dockerId); }
   * 
   * @Override public String toString() { return "name: " + name + "; dockerID " + dockerId +
   * "; squareId" + squareId + ": serviceDocker " + dockerInstance + "; servicePort :" +
   * servicePort; }
   */

  final String containerId;
  final String image;
  final String command;
  final String created;
  final String status;
  final String servicePort;
  final String name;
  final int squareId;

  public ImageInfo(String containerId, String image, String command, String created, String status,
      String servicePort, String name, int squareId) {
    this.containerId = containerId;
    this.image = image;
    this.command = command;
    this.created = created;
    this.status = status;
    this.servicePort = servicePort;
    this.name = name;
    this.squareId = squareId;
  }
}
