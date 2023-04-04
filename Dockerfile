FROM openjdk:17-jdk-slim
EXPOSE 9090
ADD build/libs/oauth-server-0.0.1-SNAPSHOT.jar /usr/local/app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "/usr/local/app.jar"]
