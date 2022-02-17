FROM openjdk:17.0.2-oracle

EXPOSE 6000
COPY ./target/a-benefits-0.0.1-SNAPSHOT.jar /tmp/app.jar
CMD ["java", "-Xms200m", "-Xmx1024m", "-jar", "/tmp/app.jar"]
