FROM openjdk:17-alpine

WORKDIR /app

COPY target/GroceryBot-0.0.1-SNAPSHOT.jar /app/app.jar

CMD ["java", "-jar", "app.jar"]
