
FROM openjdk:17
COPY target/prometheus-grafana-dashboard-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
