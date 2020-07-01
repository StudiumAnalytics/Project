#!/usr/bin/env bash
set -v
# Stop
docker stop kafkaTopic

# Remove previuos container 
docker container rm kafkaTopic

docker build ../kafka/ --tag tap:kafka
docker run -e KAFKA_ACTION=create-topic --name kafkaTopic --network host   -it tap:kafka