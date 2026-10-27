FROM busybox:1.36.1-uclibc as busybox

FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

COPY --from=busybox /bin/sh /bin/sh
COPY --from=busybox /bin/printenv /bin/printenv
COPY --from=busybox /bin/mkdir /bin/mkdir
COPY --from=busybox /bin/chown /bin/chown

ENV TZ="Europe/Oslo"
WORKDIR /app
COPY /target/veilarbdirigent.jar app.jar
EXPOSE 8080
CMD ["-jar", "app.jar"]
