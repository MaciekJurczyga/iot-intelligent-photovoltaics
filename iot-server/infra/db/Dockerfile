FROM postgres:16-alpine

ENV POSTGRES_DB=iot-db
ENV POSTGRES_USER=admin
ENV POSTGRES_PASSWORD=password

COPY init/ /docker-entrypoint-initdb.d/

EXPOSE 5432
