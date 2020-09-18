FROM alpine:3.12
RUN apk --update add openjdk8-jre --repository=http://nl.alpinelinux.org/alpine/edge/community
COPY ./target/Demo-SpringApplication*.jar ./
WORKDIR ./
CMD ["/bin/sh", "-c", "java -jar Demo-SpringApplication*.jar"]

