FROM openjdk:17
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone
COPY coral-0.2.jar /app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar", "--collect=true","--host=100.64.1.4"]