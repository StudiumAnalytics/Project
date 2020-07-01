#!/usr/bin/env bash
# Stop
docker stop sparkSubmitApp

# Remove previuos container 
docker container rm sparkSubmitApp

docker build ../spark/ --tag tap:spark
docker run -e SPARK_ACTION=spark-submit-apps --name sparkSubmitApp --network host  -it tap:spark 