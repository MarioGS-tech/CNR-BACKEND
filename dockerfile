# Usa la imagen base de OpenJDK para Java 17
FROM registry.access.redhat.com/ubi8/openjdk-17:1.19-1
# Copia el archivo JAR de tu aplicación al contenedor
COPY target/factElectrCnrPuntoDeVentaBackend.jar /app/factElectrCnrPuntoDeVentaBackend.jar
COPY /app/admin/sisucc3/config/application.properties /app/admin/sisucc3/config/application.properties
# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app
# Comando para ejecutar tu aplicación Spring Boot
ENTRYPOINT [ "java", "-jar", "factElectrCnrPuntoDeVentaBackend.jar" ]
