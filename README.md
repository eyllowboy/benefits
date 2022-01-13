## Instruction to run

1. For install docker container with postgresql and pgadmin 4 go to ./docker run in console
> docker-compose up -d
2. To start app:
- in root dir at first build its
> mvn clean install
- then to run
> java -jar ./target/a-benefits-0.0.1-SNAPSHOT.jar
- to stop
> mvn spring-boot:stop
3. App run on http://localhost:8080/benefits/
4. Swagger run on http://localhost:8080/benefits/swagger-ui/index.html