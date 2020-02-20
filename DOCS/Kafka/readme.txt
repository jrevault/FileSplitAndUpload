https://itnext.io/how-to-install-kafka-using-docker-a2b7c746cbdc

We create a docker network for our cluster:
$ docker network create --driver bridge kafka-net 

Run a ZooKeeper container from Bitnami ZooKeeper image :
$ docker run --name zookeeper-server -p 2181:2181 --network kafka-net -e ALLOW_ANONYMOUS_LOGIN=yes bitnami/zookeeper:latest -d

Test zookeeper
$ telnet localhost 2181

In order to add a broker to the cluster, we need to introduce it to ZooKeeper.
A Kafka broker introduces itself to the Zookeeper server (or cluster)
by knowing about the Zookeeper server IP address (CHANGE IP ADDRESS):
$ docker run --name kafka-server1 --network kafka-net -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper-server:2181 -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -p 9092:9092 bitnami/kafka:latest -d
To add another broker :
$ docker run --name kafka-server2 --network kafka-net -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper-server:2181 -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9093 -p 9093:9092 bitnami/kafka:latest -d

install https://www.conduktor.io/download
