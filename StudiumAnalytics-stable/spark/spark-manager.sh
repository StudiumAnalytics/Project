#!/bin/bash
[[ -z "${SPARK_ACTION}" ]] && { echo "SPARK_ACTION required"; exit 1; }

# ACTIONS start-zk, start-kafka, create-topic, 

echo "Running action ${SPARK_ACTION}"
case ${SPARK_ACTION} in
"example")
echo "Running example ARGS $@"
./bin/run-example $@
;;
"spark-shell")
./bin/spark-shell --master local[2]
;;
"spark-submit-apps")
 ./bin/spark-submit --class studium.analytics.streaming.App /opt/tap/apps/studium-analytics-streaming-1.0.0/target/studium-analytics-streaming-1.0.0-jar-with-dependencies.jar
;;
"pytap")
cd /opt/tap/
python ${TAP_CODE}
;;
"bash")
while true
do
	echo "Keep Alive"
	sleep 10
done
;;
esac