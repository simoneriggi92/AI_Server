FROM mranest/openjdk9-alpine

# Required for starting application up.
RUN apk update && apk add bash


RUN mkdir -p /opt/app
ENV PROJECT_HOME /opt/app

COPY server.jar $PROJECT_HOME/server.jar

WORKDIR $PROJECT_HOME

CMD ["java", "-Dspring.data.mongodb.uri=mongodb://gruppo3mongo:27017/dblab3","-Djava.security.egd=file:/dev/./urandom", "-jar","./server.jar"]
HEALTHCHECK --interval=10s --timeout=3s CMD curl -f http://gruppo3mongo:27017/health || exit 1