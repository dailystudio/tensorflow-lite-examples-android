./gradlew :tensorflow-lite-examples-common:clean
./gradlew :tensorflow-lite-examples-common:build
./gradlew :tensorflow-lite-examples-common:publishToMavenLocal
./gradlew :tensorflow-lite-examples-common:bintrayUpload -PdryRun=false --no-configure-on-demand --no-parallel
