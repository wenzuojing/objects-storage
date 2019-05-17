#!/bin/bash

mvn clean package -Dmaven.test.skip=true
ps -ef | grep data-server-0.0.1.jar | grep -v grep | awk '{print $2}' | xargs kill -9
java -jar data-server/target/data-server-0.0.1.jar --spring.profiles.active=dev0 &
java -jar data-server/target/data-server-0.0.1.jar --spring.profiles.active=dev1 &