# Usa la imagen base de OpenJDK para Java 17
#FROM registry.access.redhat.com/ubi8/openjdk-17:1.19-1
# Copia el archivo JAR de tu aplicación al contenedor
#COPY target/factElectrCnrPuntoDeVentaBackend.jar /app/factElectrCnrPuntoDeVentaBackend.jar
#COPY /app/admin/sisucc3/config/application.properties /app/admin/sisucc3/config/application.properties
# Establece el directorio de trabajo dentro del contenedor
#WORKDIR /app
# Comando para ejecutar tu aplicación Spring Boot
#ENTRYPOINT [ "java", "-jar", "factElectrCnrPuntoDeVentaBackend.jar" ]

#?-------------Docker file cambiado par la aplicacion BACKEND

# Usa una imagen base de OpenJDK 17
FROM openjdk:17.0.10-jdk-slim

# Establece el directorio de trabajo en /app
WORKDIR /app

# Copia el archivo JAR desde la carpeta resources (ajústalo si es necesario)
COPY resource/factElectrCnrPuntoDeVentaBackend-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto 8080
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
