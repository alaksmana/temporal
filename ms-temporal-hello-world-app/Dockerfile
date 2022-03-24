FROM gradle:jdk11-hotspot AS build

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src

RUN gradle build --no-daemon

FROM openjdk:11.0.9.1-jre-buster

ENV TZ=Asia/Bangkok

WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/greeting-0.0.1-SNAPSHOT.jar /app/spring-boot-application.jar

CMD ["java","-jar","/app/spring-boot-application.jar"]
