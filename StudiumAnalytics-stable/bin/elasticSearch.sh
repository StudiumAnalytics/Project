#!/usr/bin/env bash
# Stop
docker stop elasticsearch

# Remove previuos container 
docker container rm elasticsearch

# Build
docker build ../elasticsearch/ --tag tap:elasticsearch

docker run -t  -p 9200:9200  --name elasticsearch -v /elasticVol --mount source=elasticVol,destination=/usr/share/elasticsearch/data -e "discovery.type=single-node" --network host  tap:elasticsearch
