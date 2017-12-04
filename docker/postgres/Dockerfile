FROM postgres:9.6.3

RUN apt-get update
RUN apt-get install -y netcat

COPY ./init-db.sh /docker-entrypoint-initdb.d/

CMD ["postgres"]