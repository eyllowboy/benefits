## Instruction to run

1. For install docker container with postgresql and pgadmin 4 go to ./docker run in console
> docker-compose up -d
2. Deploying docker containers for keycloak. Go to ./keycloak
> docker-compose up -d
3. Go to http://localhost:8484/auth/
and to "Administration Console"
- login: admin\
pass: admin
- When hovering over "Master" in the upper left corner, click "Add realm". \
Next, click "Select file" and specify the path to the "./keycloak/realm-export.json" file 
in the project directory. \
Next "Create". 
- Next, you need to add users. The roles have already been created. \
a) To create user click "Users" -> "Add user".
Fill in the "Username" field and click "Save". \
Next, go to the "Credentials" tab and set a password. Set "Temporary" to "Off". \
And click "Set password". \
Next, go to the "Role Mappings" tab and add one of the three roles to the user \
(ROLE_ADMIN, ROLE_MODERATOR or ROLE_USER). \
Similarly, we create an HR user and a regular user. \
b) Or follow the link http://localhost:8484/auth/realms/benefits/account/#/ and create users 
via the web. \
Roles are assigned same as in a). 
- All endpoints can be viewed in "Realm settings" -> "Endpoints".
4. If you want to authorize via http, then you need to send a POST request to the address 
http://localhost:8484/auth/realms/benefits/protocol/openid-connect/token
with matching body. See an example: ./keycloak/benefits.postman_collection.json 
(postman collection file, just import in postman)

5. To start app:
- in root dir at first build its
> mvn clean install
- then to run
> java -jar ./target/a-benefits-0.0.1-SNAPSHOT.jar
- to stop
> mvn spring-boot:stop
6. App run on http://localhost:8080/benefits/
7. Swagger run on http://localhost:8080/benefits/swagger-ui/index.html \
"client_id" is benefits-auth.
"Secret" also is required for authorization. Go to keycloak 
"Clients" -> "benefits-auth" under "Credentials" tab copy it. 
Next, enter the username and password of the user you created in step 3. \
In order to log in as a different user, you need to wait until the end of the session or 
force them to end in keycloak in "Sessions".
