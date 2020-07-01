# Stop previous container
docker stop kafkaServer

# Remove previuos container 
docker container rm kafkaServer

docker build ../kafka/ --tag tap:kafka
docker stop kafkaServer
docker run -e KAFKA_ACTION=start-kafka -p 9092:9092 --name kafkaServer --network host  -it tap:kafka