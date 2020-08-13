FROM docker.pkg.github.com/navikt/pus-nais-java-app/pus-nais-java-app:java11
COPY /target/veilarbdirigent.jar app.jar
COPY /public /public
