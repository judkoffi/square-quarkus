package fr.umlv.square.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped	// One DockerService instance for the whole application
public class DockerService {

	private final static String DOCKERFILE_TEMPLATE;

	private final static String DOCKER_IMAGE_DIRECTORY = "docker-images/";

	private final static String APPS_DIRECTORY = "apps/";

	static {
		DOCKERFILE_TEMPLATE = "FROM hirokimatsumoto/alpine-openjdk-11\n" + "WORKDIR /app\n"
				+ "COPY {{1}}.jar /app/app.jar\n" + "RUN chmod 775 /app\n" + "EXPOSE {{2}}\n" + "CMD java -jar app.jar";
	}

	private final ProcessBuilder processBuilder;

	public DockerService() {
		this.processBuilder = new ProcessBuilder();
	}

	private String getOutput(InputStream outpuStream) throws IOException {
		var reader = new BufferedReader(new InputStreamReader(outpuStream));
		var builder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}

		return builder.toString();

	}

	public String buildImage(String appName, int port) {

		var imageFile = DOCKERFILE_TEMPLATE.replace("{{1}}", appName).replace("{{2}}", "" + port);

		var imagePath = DOCKER_IMAGE_DIRECTORY + "Dockerfile." + appName;

		var createDockerfileCommand = "echo \"" + imageFile + "\" > " + imagePath;

		processBuilder.directory(new File("../"));

		// processBuilder.command("/bin/bash", "-c", "pwd");

		var builImageCommand = "docker build -f " + imagePath + " -t " + appName + " " + APPS_DIRECTORY;

		var listImagesCommand = "docker images";

		var runDockerCommand = "docker run -p " + port + ":8080 " + appName;

		System.out.println(runDockerCommand);

		var dockerPsCommand = "docker ps";

		try {

			
			var outputStream = processBuilder.command("/bin/bash", "-c", createDockerfileCommand).start().getInputStream();

			outputStream = processBuilder.command("/bin/bash", "-c", builImageCommand).start().getInputStream();

			//outputStream = processBuilder.command("/bin/bash", "-c", runDockerCommand).start().getInputStream();

			//outputStream = processBuilder.command("/bin/bash", "-c", dockerPsCommand).start().getInputStream();

			return getOutput(outputStream);
		} catch (IOException e) {
			return e.getMessage();
		}
	}

}
