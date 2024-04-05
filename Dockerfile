####
# mvn package
#
# docker build -t proxy-service .
#
# docker run -it --rm -p 8080:8080 proxy-service
#
# docker run -d --rm --name proxy-teste -p 8080:8080 proxy-service
##
FROM openjdk:21

ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"\
    QUARKUS_PORT=8080 \
    AB_ENABLED=jmx_exporter

# RUN apk --no-cache add curl

EXPOSE 8080

ADD . /app

WORKDIR /app

COPY target/* /app/target

ENTRYPOINT ["java", "-Duser.country=BR", "-Duser.language=pt","-Dquarkus.http.host=0.0.0.0", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-Dquarkus.profile=quarkus:dev", "-jar", "/app/target/quarkus-app/quarkus-run.jar"]
