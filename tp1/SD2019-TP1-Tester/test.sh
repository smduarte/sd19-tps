echo "Launching tester... args: " "$@"

[ ! "$(docker network ls | grep sd-net )" ] && \
	docker network create --driver=bridge --subnet=172.20.0.0/16 sd-net


java -Djava.util.concurrent.ForkJoinPool.common.parallelism=16 -cp "/home/sd/*" tests.Tester "$@"
