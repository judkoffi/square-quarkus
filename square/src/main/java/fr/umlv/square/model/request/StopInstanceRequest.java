package fr.umlv.square.model.request;

import java.util.Objects;

public class StopInstanceRequest {
	private int id;

	public StopInstanceRequest() {
	}

	public StopInstanceRequest(int id) {
		this.id = Objects.requireNonNull(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "stop request :" + id;
	}
}
