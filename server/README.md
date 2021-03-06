## Spotlight server

Recommended IDE: IntelliJ. All the gradle commands are run from a batch file (.bat) in a command prompt (cmd) in Windows 10, you can use the same commands from PowerShell running `.\gradlew`, or in an Unix shell running `./gradlew`. Keep in mind that in an Unix shell you should add execution permission to the script with a `chmod +x gradlew`.

### Requirements

- JDK11
- Maven 3+

First time installing a JDK? After installation create a environment variable called JAVA_HOME pointing to the JDK installation folder. For example `JAVA_HOME=C:\Program Files\Java\jdk-11.0.2`.

### Oracle

Download ojdbc6 driver from Oracle, and install it to local maven repository:

    mvn install:install-file -Dfile="ojdbc6.jar" -DgroupId="com.oracle" -DartifactId="ojdbc6" -Dversion="11.2.0" -Dpackaging="jar"

For local development create a user schema in Oracle named spotlight and with password spotlight. You can follow the instructions found in https://docs.oracle.com/cd/E17781_01/admin.112/e18585/toc.htm#XEGSG111

### Running the Dashboard REST API

Run `gradlew dashboard:bootRun` on the project root folder from a command prompt. The REST API will be served from `http://locahost:9000`.

### Running the Applications API

Run `gradlew applications:bootRun` on the project root folder from a command prompt. The API will be server from `http://localhost:9001`.

### Deploying to Wildfly

Uncomment line

    providedRuntime('org.springframework.boot:spring-boot-starter-tomcat')

in `build.gradle`, and change the active profile property `spring.profiles.active` to `dev` in `dashboard/src/main/resources/application.properties` file.

Finally run `gradlew -Ptomcat dashboard:bootWar` to build dashboard WAR. You can find these WAR files in `build/libs/` in the respective project folder. A similar process is required for building the applications api.

### Testing

Two test suites exist for dashboard and applications modules, and one for cronjobs. All modules implement unit tests for classes that are run with the commands:

    gradlew dashboard:testDev
    gradlew applications:testDev
    gradlew cronjobs:testDev
    
and dashboard and applications modules implement integration tests which load the complete application along with a in-memory H2 database, and are run with the commands:

    gradlew dashboard:testStage
    gradlew applications:testStage
    
Unit tests should be run anytime a pull request is merged into development branch, and as integration tests are heavier than unit tests they should be run only when merging into stage/QA branch.

Please add the suffix `-UnitTest` to classes that implement unit tests, `-JpaTest` for JPA query tests, and `-EndpointTest` for integration tests. For more information please refer to folder `src/test/` in each module.