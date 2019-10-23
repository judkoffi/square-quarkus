package fr.umlv.square.model.response;

import java.util.Objects;
import java.util.StringJoiner;

abstract class AbstractInstanceInfo {
	private final int id;
	private final int port;
	private final String appName;
	private final String servicePort;
	private final String dockerInstance;

	public AbstractInstanceInfo(int id, String appName, int port, String servicePort, String dockerInstance) {
		this.id = Objects.requireNonNull(id);
		this.appName = Objects.requireNonNull(appName);
		this.port = Objects.requireNonNull(port);
		this.servicePort = Objects.requireNonNull(servicePort);
		this.dockerInstance = Objects.requireNonNull(dockerInstance);
	}

	String buildJson() {
		StringJoiner joiner = new StringJoiner(", ");
		joiner.add("\"id\": \"" + id + "\"");
		joiner.add("\"app\": \"" + appName + "\"");
		joiner.add("\"port\": \"" + port + "\"");
		joiner.add("\"service-docker\":\"" + servicePort + "\"");
		joiner.add("\"docker-instance\":\"" + dockerInstance + "\"");
		return joiner.toString();
	}

	public String toJson() {
		return "{" + buildJson() + "}";
	}
}
