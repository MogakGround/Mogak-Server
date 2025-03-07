FROM amd64/amazoncorretto:17

WORKDIR /app

COPY ./build/libs/mogak-server-0.0.1-SNAPSHOT.jar /app/mogak-server.jar

CMD ["java", "-Duser.timezone=Asia/Seoul", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-jar", "-Dspring.profiles.active=dev", "/app/mogak-server.jar"]
