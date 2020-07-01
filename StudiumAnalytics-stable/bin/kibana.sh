#!/usr/bin/env bash
# Stop
docker stop kibana

#  Remove previuos container 
docker container rm kibana

# Build
docker build ../kibana/ --tag tap:kibana

docker stop kibana
docker run -p 5601:5601 --name kibana -v /kibanaVol --mount source=kibanaVol,destination=/usr/share/kibana/data  --network host tap:kibana
