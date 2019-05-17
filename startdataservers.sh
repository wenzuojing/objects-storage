#!/bin/bash

mvn clean package -Dmaven.test.skip=true

java -jar data-server/target/data-server-0.0.1.jar --spring.profiles.active=dev0 &
java -jar data-server/target/data-server-0.0.1.jar --spring.profiles.active=dev1 &