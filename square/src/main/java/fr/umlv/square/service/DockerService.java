package fr.umlv.square.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DockerService {

	private final static String DOCKERFILE_TEMPLATE;

	private final static String DOCKER_IMAGE_DIRECTORY = "docker-images/";

	static {
		DOCKERFILE_TEMPLATE = "FROM hirokimatsumoto/alpine-openjdk-11\n" + "WORKDIR /app\n"
				+ "COPY {{1}} /app/app.jar\n" + "RUN chmod 775 /app\n" + "EXPOSE {{2}}\n" + "CMD java -jar app.jar";
	}

	private final ProcessBuilder processBuilder;

	public DockerService() {
		this.processBuilder = new ProcessBuilder();
	}

	public String buildImage(String appName, int port) {

		try {
			var imageFile = DOCKERFILE_TEMPLATE.replace("{{1}}", appName).replace("{{2}}", "" + port);

			var createDockerfileCommand = "echo \"" + imageFile + "\" > " + DOCKER_IMAGE_DIRECTORY + "Dockerfile."
					+ appName;

			processBuilder.directory(new File("../"));

			// processBuilder.command("/bin/bash", "-c", "pwd");

			processBuilder.command("/bin/bash", "-c", createDockerfileCommand);

			var prefix = "docker build -f docker-images/Dockerfile." + appName + "-t helloapp apps/";

			var reader = new BufferedReader(new InputStreamReader(processBuilder.start().getInputStream()));
			var builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}

			return builder.toString();

		} catch (IOException exception) {
			return exception.getMessage();
		}
	}
}
