## Instruction to run

1. For install docker container with postgresql and pgadmin 4 go to ./docker run in console
> docker-compose up -d
2. Then check ip of db container and correct application.properties file
3. To start app:
- in root dir at first build its
> mvn clean install 
- then to run
> mvn spring-boot:start
- to stop
> mvn spring-boot:stop
4. App run on http://localhost:8080/benefits/