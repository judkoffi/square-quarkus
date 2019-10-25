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
		var splited = app.split(":")[1];
		if (isNumeric(splited))
			return Integer.parseInt(splited);
		return -1;
	}

	public boolean isValidRequest() {
		return isValidRawRequest() && !isEmptyRequest();
	}

	@Override
	public String toString() {
		return "deploy request :" + app;
	}

	private static boolean isNumeric(String number) {
		return number.matches("-?\\d+(\\.\\d+)?");
	}

	private boolean isEmptyRequest() {
		return getAppName().isEmpty() || getPort() == -1;
	}

	private boolean isValidRawRequest() {
		return app != null && app.contains(":");
	}
}
