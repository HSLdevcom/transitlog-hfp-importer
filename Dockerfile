FROM openjdk:11-jre-slim
ADD build/libs/transitlog-hfp-importer.jar /usr/app/transitlog-hfp-importer.jar
ENTRYPOINT ["java", "-XX:InitialRAMPercentage=10.0", "-XX:MaxRAMPercentage=95.0", "-jar", "/usr/app/transitlog-hfp-importer.jar"]
