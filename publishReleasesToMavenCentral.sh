#!/bin/sh
artifactsToPublish=(
  "tensorflow-litex"
)

echo "--------- [STEP 1: Building artifacts] ---------"
for artifact in ${artifactsToPublish[@]}; do
  task=":${artifact}:assemble"
  echo "Processing artifact: [${artifact}] ..."
  ./gradlew ${task} > /dev/null
done

echo "--------- [STEP 2: Publishing artifacts] ---------"
for artifact in ${artifactsToPublish[@]}; do
  task=":${artifact}:publish"
  echo "Uploading artifact: [${artifact}] ..."
  ./gradlew ${task} > /dev/null
done

./updateVersionInDocs.sh
