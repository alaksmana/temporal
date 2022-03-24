FROM gradle:jdk11-hotspot AS build

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src

RUN gradle build --no-daemon

FROM openjdk:11.0.9.1-jre-buster

ENV TZ=Asia/Bangkok

WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/greeting-worker-1.0-SNAPSHOT.jar /app/worker.jar

CMD ["java","-jar","/app/worker.jar"]
