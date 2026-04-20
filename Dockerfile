FROM tomcat:10.1-jdk17-temurin

# Limpia las apps de ejemplo de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copia el WAR como ROOT (se despliega en /)
COPY target/sub*.war /usr/local/tomcat/webapps/ROOT.war

# Crea el directorio de uploads con permisos
#RUN mkdir -p /usr/local/tomcat/webapps/ROOT/uploads && chmod 777 /usr/local/tomcat/webapps/ROOT/uploads

EXPOSE 8080
CMD ["catalina.sh", "run"]