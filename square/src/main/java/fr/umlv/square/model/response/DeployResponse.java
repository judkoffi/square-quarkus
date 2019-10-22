package fr.umlv.square.model.response;

import java.util.Objects;

import javax.json.bind.annotation.JsonbProperty;

public class DeployResponse {
	private final int id;
	private final int port;

	@JsonbProperty("app")
	private final String appName;

	@JsonbProperty("service-port")
	private final String servicePort;

	@JsonbProperty("docker-instance")
	private final String dockerInstance;

	public DeployResponse(int id, String appName, int port, String servicePort, String dockerInstance) {
		super();
		this.id = Objects.requireNonNull(id);
		this.appName = Objects.requireNonNull(appName);
		this.port = Objects.requireNonNull(port);
		this.servicePort = Objects.requireNonNull(servicePort);
		this.dockerInstance = Objects.requireNonNull(dockerInstance);
	}

	public int getId() {
		return id;
	}

	public String getAppName() {
		return appName;
	}

	public int getPort() {
		return port;
	}

	public String getServicePort() {
		return servicePort;
	}

	public String getDockerInstance() {
		return dockerInstance;
	}

}
