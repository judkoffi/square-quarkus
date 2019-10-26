FROM fabric8/java-alpine-openjdk8-jre
WORKDIR /app
COPY ./helloapp.jar /app/app.jar
RUN chmod 775 /app
EXPOSE 8080
CMD java -jar app.jar