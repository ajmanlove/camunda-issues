# camunda-issues
Repo for sharing camunda issues

## Requires
- Docker
- Java 8
Note: docker integration only tested on OSX with Docker for Mac

## To run the tests
```
mvn clean verify
```

## To run single test
First stand up the integration docker env:
```
./teardown-it.sh && setup-it.sh
```
Then run any IT test in your IDE

## To view the docker logs
```
cd docker && docker-compose logs -f
```
