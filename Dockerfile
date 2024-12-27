FROM amd64/amazoncorretto:17

WORKDIR /app

COPY ./build/libs/mogak-server-0.0.1-SNAPSHOT.jar /app/mogak-server.jar

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=dev", "/app/mogak-server.jar"]
