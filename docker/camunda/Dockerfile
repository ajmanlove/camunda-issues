FROM tomcat:8.0.43-jre8

RUN apt-get update
RUN apt-get install -y netcat

RUN mkdir /camunda
COPY files/camunda-bpm-tomcat-7.7.0.tar.gz /
RUN tar xvxf /camunda-bpm-tomcat-7.7.0.tar.gz -C /camunda
RUN cp /camunda/lib/*.jar /usr/local/tomcat/lib/

RUN cp -r /camunda/server/apache-tomcat-8.0.24/webapps/camunda /usr/local/tomcat/webapps/
RUN cp -r /camunda/server/apache-tomcat-8.0.24/webapps/engine-rest /usr/local/tomcat/webapps/

# Exposed as mount points
RUN mv /usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/server.xml.bak

VOLUME "/usr/local/tomcat/conf/bpm-platform.xml"
VOLUME "/usr/local/tomcat/conf/server.xml"

COPY files/*.jar /usr/local/tomcat/lib/
COPY camunda-issues.jar /usr/local/tomcat/lib/



COPY startup.sh .
CMD ./startup.sh