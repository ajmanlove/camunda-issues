#!/usr/bin/env bash
cp ../../target/camunda-issues.jar ./
docker build -t sandbox/camunda .