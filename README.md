# Micronaut_FileUpload

!! Need to change KAFKA_CFG_ADVERTISED_LISTENERS in docker-compose.yml depending on your ip address !!
KAFKA_CFG_ADVERTISED_LISTENERS represent the host external address (host ip) so that client can connect to it, otherwire they'll try to connect to the internal host address that is not reachable.

curl -H "Content-Type:multipart/form-data" -X POST -F "file=@01.pdf" http://localhost:8080/upload

https://github.com/mmindenhall/micronaut-examples/blob/large-file-upload-disk/hello-world-kotlin/src/main/kotlin/example/UploadController.kt

Use Docker-compose (create a docker-compose.yml file)
$ docker-compose up -d

$ docker-compose down
