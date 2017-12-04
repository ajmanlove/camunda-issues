#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER camunda WITH PASSWORD 'camunda';
    CREATE DATABASE camunda;
    GRANT ALL PRIVILEGES ON DATABASE camunda TO camunda;

EOSQL