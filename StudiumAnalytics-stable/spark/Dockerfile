FROM openjdk:8-jre

ENV SPARK_DIR=/opt/spark
ENV PATH $SPARK_DIR/bin:$PATH
ENV SPARK_VERSION=3.0.0
LABEL maintainer="S. M. CORPORATION"

ADD setup/spark-${SPARK_VERSION}-preview2-bin-hadoop2.7.tgz /opt

RUN apt-get update && apt-get -y install bash python python-pip netcat

RUN pip install pyspark numpy elasticsearch
# Create Sym Link 
RUN ln -s /opt/spark-${SPARK_VERSION}-preview2-bin-hadoop2.7 ${SPARK_DIR}
# Add Java Code
ADD apps /opt/tap/apps
COPY MLTrainingData/* opt/spark-${SPARK_VERSION}-preview2-bin-hadoop2.7/
# Add Spark Manager
ADD spark-manager.sh $SPARK_DIR/bin/spark-manager

WORKDIR ${SPARK_DIR}
ENTRYPOINT ["spark-manager"]

