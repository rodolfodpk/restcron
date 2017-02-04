# restcron

[![Build Status](https://travis-ci.org/rodolfodpk/keyvent.svg?branch=master)](https://travis-ci.org/rodolfodpk/restcron)

## Requirements

1. Java 8 
2. Maven 3

## Build
```
mvn clean install
```
## Running 

```
mvn exec:java 
```

or 

```
java -jar target/restcron-jar-with-dependencies.jar 
```

## Swagger

You can open swagger-ui on your browser 

```
xdg-open target/swagger-ui/index.html 
## or in Mac 
open target/swagger-ui/index.html 
```

then on Swagger page, inform url bellow and click "explore" button 

http://localhost:8080/api-doc
