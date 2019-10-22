package fr.umlv.square.model.request;

import java.util.Objects;

public class DeployRequest {
	private String app;

	public DeployRequest() {
	}

	public DeployRequest(String app) {
		this.app = Objects.requireNonNull(app);
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getAppName() {
		return app.split(":")[0];
	}

	public int getPort() {
		return Integer.parseInt(app.split(":")[1]);
	}

	@Override
	public String toString() {
		return "request :" + app;
	}

}
