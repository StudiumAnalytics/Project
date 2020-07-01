#!/usr/bin/env bash
docker build ../kafka/ --tag tap:kafka
docker run -e KAFKA_ACTION=producer -e KAFKA_TOPIC=TeachingDidLoad  --network host  -it tap:kafka