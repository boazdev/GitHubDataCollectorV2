empty repo example:
https://github.com/mzsrtgzr2/compilationHelper
redis examples:
./mvnw.cmd package -DskipTests
docker build -t collector .
boazdev 86 commits

./mvnw.cmd package -DskipTests
docker build -t artofterran135/githubcollector . #for dockerhub then tag
docker tag artofterran135/githubcollector artofterran135/githubcollector:112
docker push artofterran135/githubcollector:112