war:
	mvn package

clean:
	mvn clean
	rm src/main/docker/*.war

docker:
	cp target/*.war src/main/docker
	cd src/main/docker && docker build -t lappsgrid/udpipe .
	
tag:
	docker tag lappsgrid/udpipe docker.lappsgrid.org/lappsgrid/udpipe

push:
	docker push docker.lappsgrid.org/lappsgrid/udpipe

start:
	docker run -d -p 8080:8080 --name udpipe docker.lappsgrid.org/lappsgrid/udpipe
	
stop:
	docker rm -f udpipe
