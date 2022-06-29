FROM openjdk:8-jdk-alpine
WORKDIR application
EXPOSE 8080
ADD /build/libs/*.jar template-service.jar
ENTRYPOINT ["java", "-jar", "template-service.jar"]