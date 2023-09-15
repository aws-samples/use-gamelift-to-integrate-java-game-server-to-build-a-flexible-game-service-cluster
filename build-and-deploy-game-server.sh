#!/bin/bash
./gradlew clean build
rm -rf ./ServerBuild/gamelift-java-game-server-demo-1.0-SNAPSHOT-all.jar
cp ./build/libs/gamelift-java-game-server-demo-1.0-SNAPSHOT-all.jar ./ServerBuild/

region=us-east-1
echo "Deploying build to GameLift in $region"
buildversion=$(date +%Y-%m-%d.%H:%M:%S)
aws gamelift upload-build \
    --operating-system AMAZON_LINUX_2 \
    --server-sdk-version 5.1.1 \
    --build-root ./ServerBuild/ \
    --name "Java Game Server Example" \
    --build-version $buildversion \
    --region $region