#!/bin/bash
ZK_DATA_DIR=/tmp/zookeeper
ZK_SERVER="localhost"
[[ -z "${KAFKA_ACTION}" ]] && { echo "KAFKA_ACTION required"; exit 1; }
[[ -z "${KAFKA_DIR}" ]] && { echo "KAFKA_DIR missing"; exit 1; }
# ACTIONS start-zk, start-kafka, create-topic, 

echo "Running action ${KAFKA_ACTION} (Kakfa Dir:${KAFKA_DIR}, ZK Server: ${ZK_SERVER})"
case ${KAFKA_ACTION} in
"start-zk")
echo "Starting ZK"
mkdir -p ${ZK_DATA_DIR}; # Data dir is setup in conf/zookeeper.properties
cd ${KAFKA_DIR}
zookeeper-server-start.sh config/zookeeper.properties
;;
"start-kafka")
cd ${KAFKA_DIR}
kafka-server-start.sh config/server.properties
;;
"create-topic")
cd ${KAFKA_DIR}
kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --replication-factor 1 --partitions 1 --topic TeachingDidLoad
kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --replication-factor 1 --partitions 1 --topic SectionDidLoad
kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --replication-factor 1 --partitions 1 --topic TeachingStateRefresh
kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --replication-factor 1 --partitions 1 --topic DocumentDidOpen
kafka-topics.sh --create --zookeeper 127.0.0.1:2181 --replication-factor 1 --partitions 1 --topic NotificationDidOpen
kafka-topics.sh --list --zookeeper 127.0.0.1:2181
;;
"producer")
cd ${KAFKA_DIR}
kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic ${KAFKA_TOPIC}
;;
esac

