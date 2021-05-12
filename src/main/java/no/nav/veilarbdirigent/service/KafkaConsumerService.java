package no.nav.veilarbdirigent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.utils.OppfolgingsperiodeUtils;
import no.nav.veilarbdirigent.utils.RegistreringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static no.nav.veilarbdirigent.utils.TaskFactory.*;
import static no.nav.veilarbdirigent.utils.TaskUtils.createTaskIfNotStoredInDb;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final AktorOppslagClient aktorOppslagClient;

    private final VeilarboppfolgingClient veilarboppfolgingClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final UnleashService unleashService;

    private final TaskRepository taskRepository;

    public void behandleOppfolgingStartet(OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO) {
        if (!unleashService.isKafkaEnabled()) {
            throw new RuntimeException("Kafka toggle is not enabled");
        }

        AktorId aktorId = oppfolgingStartetKafkaDTO.getAktorId();
        Fnr fnr = aktorOppslagClient.hentFnr(aktorId);

        List<Oppfolgingsperiode> oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(fnr);

        BrukerRegistreringWrapper brukerRegistrering = veilarbregistreringClient.hentRegistrering(fnr)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

        LocalDateTime registreringsdato = RegistreringUtils.hentRegistreringDato(brukerRegistrering);

        if (!RegistreringUtils.erNyligRegistrert(registreringsdato, oppfolgingsperioder)) {
            log.info("Bruker {} er ikke nylig registrert og skal ikke ha aktivitet/dialog", aktorId);
            return;
        }

        Oppfolgingsperiode gjeldendeOppfolgingsperiode = OppfolgingsperiodeUtils.hentGjeldendeOppfolgingsperiode(oppfolgingsperioder)
                .orElseThrow(() -> new IllegalStateException("Bruker har ikke gjeldende oppfølgingsperiode"));

        String oppfolgingsperiodeId = gjeldendeOppfolgingsperiode.getUuid().toString();

        boolean erNyRegistrert = RegistreringUtils.erNyregistrert(brukerRegistrering);
        boolean erNySykmeldtBrukerRegistrert = RegistreringUtils.erNySykmeldtBrukerRegistrert(brukerRegistrering);

        List<Task> tasksToStore = new ArrayList<>();

        if (erNyRegistrert) {
            Optional<Task> maybePermittertDialogTask = createTaskIfNotStoredInDb(
                    () -> lagKanskjePermittertDialogTask(oppfolgingsperiodeId, aktorId), taskRepository
            );

            maybePermittertDialogTask.ifPresent(tasksToStore::add);
        }

        if (erNySykmeldtBrukerRegistrert || erNyRegistrert) {
            Optional<Task> maybeCvJobbprofilAktivitetTask = createTaskIfNotStoredInDb(
                    () -> lagCvJobbprofilAktivitetTask(oppfolgingsperiodeId, aktorId), taskRepository
            );

            Optional<Task> maybeJobbsokerkompetanseAktivitetTask = createTaskIfNotStoredInDb(
                    () -> lagJobbsokerkompetanseAktivitetTask(oppfolgingsperiodeId, aktorId), taskRepository
            );

            maybeCvJobbprofilAktivitetTask.ifPresent(tasksToStore::add);
            maybeJobbsokerkompetanseAktivitetTask.ifPresent(tasksToStore::add);
        }

        taskRepository.insert(tasksToStore);
    }

}
