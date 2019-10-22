package fr.umlv.square.model.response;

public class ResponseDeploy {
	private int id;
	private String app;
	private String port;
	private String servicePort;
	private String dockerInstance;

	public ResponseDeploy() {

	}

	public ResponseDeploy(int id, String app, String port, String servicePort, String dockerInstance) {
		super();
		this.id = id;
		this.app = app;
		this.port = port;
		this.servicePort = servicePort;
		this.dockerInstance = dockerInstance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getServicePort() {
		return servicePort;
	}

	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

	public String getDockerInstance() {
		return dockerInstance;
	}

	public void setDockerInstance(String dockerInstance) {
		this.dockerInstance = dockerInstance;
	}

	@Override
	public String toString() {
		return "{\"id\":" + id + ", \"app\": " + app + "}";
	}

}
