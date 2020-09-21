# IsoCode
IsoCode microservice using Spring Boot

The detailed instructions to run demo, can be found at the following repository:


Build and Deploy IsoCode locally
----------------------------

1. Open a command prompt and navigate to the root directory of this microservice.
2. Type this command to build and execute the microservice:

        mvn clean compile spring-boot:run

3. The application will be running at the following URL to get the Swagger APIs page: <http://localhost:8080/>
4. Run this from another cli session, to test the api endpoint: curl -X GET --header 'Accept: application/json' 'http://localhost:8080/health'


Deploy the application in OpenShift
-----------------------------------

1. Make sure to be connected to the OpenShift
2. Execute

		mvn package fabric8:deploy

