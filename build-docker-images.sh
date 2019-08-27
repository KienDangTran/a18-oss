#!/bin/sh
./build-project.sh \
  && ./gradlew --build-cache -i dockerCreateDockerfile dockerBuildImage