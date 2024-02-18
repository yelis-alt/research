FROM eclipse-temurin:17-jdk AS BUILD_IMAGE

ENV APP_HOME=/usr/share/application/

RUN mkdir -p $APP_HOME/src/main/java

WORKDIR $APP_HOME

COPY mvnw pom.xml mvnw.cmd $APP_HOME

COPY .mvn $APP_HOME/.mvn

RUN ./mvnw dependency:resolve

COPY . .

RUN ./mvnw package

FROM eclipse-temurin:17-jre

WORKDIR /usr/share/application

COPY --from=BUILD_IMAGE /usr/share/application/target/backend*.jar ./application.jar

EXPOSE 8080

CMD ["java","-jar","application.jar"]