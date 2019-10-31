package fr.umlv.square.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class SquareClient {
	private ProcessBuilder processBuider;

	private final String squareHost = "172.17.0.1";
	private final int squarePort = 4000;

	public SquareClient() {

	}

	public void sendLog() {
		var url = squareHost + ":" + squarePort;
		var httpClient = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
	}

}
