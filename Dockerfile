FROM amazoncorretto:21
WORKDIR /app
COPY target/springboot-aws-docker.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]