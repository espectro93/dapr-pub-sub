FROM amazoncorretto:21

WORKDIR /app
COPY build/quarkus-app/lib/ /app/lib/
COPY build/quarkus-app/app/ /app/app/
COPY build/quarkus-app/quarkus/ /app/quarkus/
COPY build/quarkus-app/quarkus-run.jar /app/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"] 