FROM tomcat:10.1-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

RUN groupadd -r tomcat && useradd -r -g tomcat -d /usr/local/tomcat -s /bin/false tomcat

RUN chown -R tomcat:tomcat /usr/local/tomcat

COPY target/sub*.war /usr/local/tomcat/webapps/ROOT.war

USER tomcat

EXPOSE 8080
CMD ["catalina.sh", "run"]