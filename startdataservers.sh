#!/bin/bash

mvn clean package -Dmaven.test.skip=true
ps -ef | grep data-server-0.0.1.jar | grep -v grep | awk '{print $2}' | xargs kill -9
java -jar data-server/target/data-server-0.0.1.jar --server.port=8000 --storage.root=/data/storage_root0 &
java -jar data-server/target/data-server-0.0.1.jar --server.port=8001 --storage.root=/data/storage_root1 &
java -jar data-server/target/data-server-0.0.1.jar --server.port=8002 --storage.root=/data/storage_root2 &
java -jar data-server/target/data-server-0.0.1.jar --server.port=8003 --storage.root=/data/storage_root3 &
java -jar data-server/target/data-server-0.0.1.jar --server.port=8004 --storage.root=/data/storage_root4 &
java -jar data-server/target/data-server-0.0.1.jar --server.port=8005 --storage.root=/data/storage_root5 &
