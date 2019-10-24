package fr.umlv.square.model.request;

import java.util.Objects;

public class DeployInstanceRequest {
	private String app;

	public DeployInstanceRequest() {
	}

	public DeployInstanceRequest(String app) {
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

	public boolean isEmpty() {
		return app == null || app.length() == 0;
	}

	@Override
	public String toString() {
		return "deploy request :" + app;
	}

}
