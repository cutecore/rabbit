FROM openjdk:17
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone
COPY rabbit-user-service-1.0-SNAPSHOT.jar /app.jar
EXPOSE 8080
ENTRYPOINT  ["java", "-jar", "/app.jar", "--collect=true","--host=100.64.1.4"]