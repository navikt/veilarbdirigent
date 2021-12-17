VeilarbDirigent
================
Microservice som dirigerer oppgaver mellom ulike FO-applikasjoner. 

### Arkitektur
- Bedskjeder kommer inn i systemet fra diverse input. Dette kan være ulike feeds eller REST endepunkter
- Bedskjedene blir oversatt til en eller flere oppgaver og lagret for sennere utførelse av systemet.
- Oppgavene ikke fullført blir periodisk plukket opp og prøvd utført. Hvis utførelsen feiler så vil oppgaven bli plukket opp på et sennere tidspunkt, som øker ved hver feilende utførelse. 
- Utførelsene av oppgavene er forskjellig basert på oppgave typen. Det kan være disse blir oversatt til utgående bedskjeder og sendt til andre systemer. Resultat av utførelsen av oppgaven blir alltid lagret og status oppdatert, både når ting funker og feiler. 

### Komme i gang

```sh
# bygge
mvn clean install 

# test
mvn test

# starte
# Kjør main-metoden i no.nav.veilarbdirigent.VeilarbdirigentApp.java
# For lokal test kjøring kjør VeilarbdirigentTestApp.java
```

### Kontakt og spørsmål

Spørsmål knyttet til koden eller prosjektet kan stilles via issues her på github.
