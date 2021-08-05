#!/usr/bin/env bash

# Script that sets up a local minIO server for testing.
CONTAINER_NAME=pureharm_test_minio # Name of the docker container used to run postgres
EXPOSED_PORT=31312                 # this is the port on the host machine;
INTERNAL_PORT=9000                 # this is the default port on which minIO starts on within the container.
MINIO_ACCESS_KEY=AKIAIOSFODOO3EXAMPLE
MINIO_SECRET_KEY=wJalrXUtnFEMI/K7MDENG/bPxRACABEXAMPLEKEY

# actual script #
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
  if [ ! "$(docker ps -aq -f name=$CONTAINER_NAME -f status=exited)" ]; then
    echo "Stopping minIO container"
    docker stop $CONTAINER_NAME
  fi
  echo "Starting minIO container"
  docker start $CONTAINER_NAME
else
  echo "Creating & starting minIO container — no stable data mapping done"
  docker run -d \
    --name $CONTAINER_NAME \
    -p $EXPOSED_PORT:$INTERNAL_PORT \
    -e "MINIO_ACCESS_KEY=$MINIO_ACCESS_KEY" \
    -e "MINIO_SECRET_KEY=$MINIO_SECRET_KEY" \
    minio/minio server /data
fi
