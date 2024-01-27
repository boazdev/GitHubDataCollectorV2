FROM pixelgroup/docker-java-git:11.0.4-jdk
COPY target/GitHubDataCollector-0.0.1-SNAPSHOT.jar /app.jar
COPY src/main/resources/static/index.html /static/githubcollector.html
ENTRYPOINT ["java","-jar","/app.jar"]
