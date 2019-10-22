package fr.umlv.square.model.request;

public class RequestDeploy {
	
	private String app;

	public RequestDeploy() {
	}
	
	public RequestDeploy(String app) {
		this.app = app;
	}

	public String getAppName() {
		return app;
	}

	public void setApp(String name) {
		this.app = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestDeploy other = (RequestDeploy) obj;
		if (app == null) {
			if (other.app != null)
				return false;
		} else if (!app.equals(other.app))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "" + app;
	}

}
