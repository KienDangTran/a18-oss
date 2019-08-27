#!/bin/sh
./build-project.sh \
  && ./build-docker-images.sh \
  && ./gradlew dockerPushImage