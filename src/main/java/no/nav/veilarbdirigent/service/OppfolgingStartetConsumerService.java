package no.nav.veilarbdirigent.service;

import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.kafka.consumer.ConsumeStatus;
import no.nav.common.kafka.consumer.TopicConsumer;
import no.nav.common.kafka.consumer.util.TopicConsumerConfig;
import no.nav.common.kafka.consumer.util.deserializer.Deserializers;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbdirigent.client.veilarboppfolging.VeilarboppfolgingClient;
import no.nav.veilarbdirigent.client.veilarboppfolging.domain.Oppfolgingsperiode;
import no.nav.veilarbdirigent.client.veilarbregistrering.VeilarbregistreringClient;
import no.nav.veilarbdirigent.client.veilarbregistrering.domain.BrukerRegistreringWrapper;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import no.nav.veilarbdirigent.repository.TaskRepository;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.utils.DbUtils;
import no.nav.veilarbdirigent.utils.OppfolgingsperiodeUtils;
import no.nav.veilarbdirigent.utils.RegistreringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static no.nav.veilarbdirigent.utils.TaskFactory.lagCvJobbprofilAktivitetTask;
import static no.nav.veilarbdirigent.utils.TaskFactory.lagKanskjePermittertDialogTask;
import static no.nav.veilarbdirigent.utils.TaskUtils.createTaskIfNotStoredInDb;
import static no.nav.veilarbdirigent.utils.TaskUtils.getStatusFromTry;

@Slf4j
@Service
public class OppfolgingStartetConsumerService extends TopicConsumerConfig<String, OppfolgingStartetKafkaDTO> implements TopicConsumer<String, OppfolgingStartetKafkaDTO> {

    private final AktorOppslagClient aktorOppslagClient;

    private final VeilarboppfolgingClient veilarboppfolgingClient;

    private final VeilarbregistreringClient veilarbregistreringClient;

    private final TaskProcessorService taskProcessorService;

    private final TaskRepository taskRepository;

    private final JdbcTemplate jdbcTemplate;

    public OppfolgingStartetConsumerService(
            AktorOppslagClient aktorOppslagClient,
            VeilarboppfolgingClient veilarboppfolgingClient,
            VeilarbregistreringClient veilarbregistreringClient,
            TaskProcessorService taskProcessorService,
            TaskRepository taskRepository,
            JdbcTemplate jdbcTemplate,
            @Value("${app.kafka.oppfolgingStartetTopic}") String topic
    ) {
        this.aktorOppslagClient = aktorOppslagClient;
        this.veilarboppfolgingClient = veilarboppfolgingClient;
        this.veilarbregistreringClient = veilarbregistreringClient;
        this.taskProcessorService = taskProcessorService;
        this.taskRepository = taskRepository;
        this.jdbcTemplate = jdbcTemplate;

        this.setTopic(topic);
        this.setKeyDeserializer(Deserializers.stringDeserializer());
        this.setValueDeserializer(Deserializers.jsonDeserializer(OppfolgingStartetKafkaDTO.class));
        this.setConsumer(this);
    }


    @Override
    @SneakyThrows
    public ConsumeStatus consume(ConsumerRecord<String, OppfolgingStartetKafkaDTO> consumerRecord) {
        OppfolgingStartetKafkaDTO oppfolgingStartetKafkaDTO = consumerRecord.value();
        /*
            Siden vi utfører oppgaver som ikke er idempotent før vi lagrer resultatet i databasen, så gjør vi en ekstra sjekk
            på om koblingen til databasen er grei, slik at vi ikke utfører oppgaver og ikke får lagret resultatet.
        */

        if (DbUtils.checkDbHealth(jdbcTemplate).isUnhealthy()) {
            log.error("Health check failed, aborting consumption of kafka consumerRecord");
            throw new IllegalStateException("Cannot connect to database");
        }
        // TODO: Fjerne, dette er en quick fix for å unngå race condition.
        //  Når man henter siste registrering fra veilarbregistrering,
        //  så har ikke nødvendigvis veilarbregistrering fått svar fra arena og oppdatert så siste registrering er gjeldende
        var date = ZonedDateTime.now().minusMinutes(1);
        if(oppfolgingStartetKafkaDTO.getOppfolgingStartet().isAfter(date)) {
            Thread.sleep(60000);
        }

        AktorId aktorId = oppfolgingStartetKafkaDTO.getAktorId();
        Fnr fnr = aktorOppslagClient.hentFnr(aktorId);

        List<Oppfolgingsperiode> oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(fnr);

        Optional<BrukerRegistreringWrapper> maybeBrukerRegistrering = veilarbregistreringClient.hentRegistrering(fnr)
                .getOrElseThrow((Function<Throwable, RuntimeException>) RuntimeException::new);

        if (maybeBrukerRegistrering.isEmpty()) {
            log.info("Bruker aktorId={} har ikke registrert seg gjennom arbeidssokerregistrering og skal ikke ha aktivitet/dialog", aktorId);
            return ConsumeStatus.OK;
        }

        BrukerRegistreringWrapper brukerRegistrering = maybeBrukerRegistrering.get();

        LocalDateTime registreringsdato = RegistreringUtils.hentRegistreringDato(brukerRegistrering);

        if (!RegistreringUtils.erNyligRegistrert(registreringsdato, oppfolgingsperioder)) {
            log.info("Bruker {} er ikke nylig registrert og skal ikke ha aktivitet/dialog", aktorId);
            return ConsumeStatus.OK;
        }

        Oppfolgingsperiode gjeldendeOppfolgingsperiode = OppfolgingsperiodeUtils.hentGjeldendeOppfolgingsperiode(oppfolgingsperioder)
                .orElseThrow(() -> new IllegalStateException("Bruker har ikke gjeldende oppfølgingsperiode"));

        String oppfolgingsperiodeId = gjeldendeOppfolgingsperiode.getUuid().toString();

        boolean erNyRegistrert = RegistreringUtils.erNyregistrert(brukerRegistrering);
        boolean erNySykmeldtBrukerRegistrert = RegistreringUtils.erNySykmeldtBrukerRegistrert(brukerRegistrering);

        log.info(
                "Behandler bruker hvor oppfølging har startet. aktorId={} erNyRegistrert={} erNySykmeldtBrukerRegistrert={}",
                aktorId, erNyRegistrert, erNySykmeldtBrukerRegistrert
        );

        List<Task> tasksToPerform = new ArrayList<>();

        if (erNyRegistrert) {
            Optional<Task> maybePermittertDialogTask = createTaskIfNotStoredInDb(
                    () -> lagKanskjePermittertDialogTask(oppfolgingsperiodeId, aktorId), taskRepository
            );

            if (maybePermittertDialogTask.isPresent()) {
                Task permittertDialogTask = maybePermittertDialogTask.get();

                Try<String> dialogTaskResult = taskProcessorService.processOpprettDialogTask(permittertDialogTask);
                permittertDialogTask.setTaskStatus(getStatusFromTry(dialogTaskResult));

                tasksToPerform.add(permittertDialogTask);
            }
        }

        if (erNySykmeldtBrukerRegistrert || erNyRegistrert) {
            Optional<Task> maybeCvJobbprofilAktivitetTask = createTaskIfNotStoredInDb(
                    () -> lagCvJobbprofilAktivitetTask(oppfolgingsperiodeId, aktorId), taskRepository
            );

            if (maybeCvJobbprofilAktivitetTask.isPresent()) {
                Task cvJobbprofilAktivitetTask = maybeCvJobbprofilAktivitetTask.get();

                Try<String> cvJobbprofilAktivitetResult = taskProcessorService.processOpprettAktivitetTask(cvJobbprofilAktivitetTask);
                cvJobbprofilAktivitetTask.setTaskStatus(getStatusFromTry(cvJobbprofilAktivitetResult));

                tasksToPerform.add(cvJobbprofilAktivitetTask);
            }
        }

        if (tasksToPerform.isEmpty()) {
            log.info("No tasks for aktorId={}", aktorId);
        } else {
            log.info("Inserting tasks for aktorId={} tasks={}", aktorId, tasksToPerform);
            taskRepository.insert(tasksToPerform);
        }

        log.info("Finished consuming kafka consumerRecord for aktorId={}", aktorId);
        return ConsumeStatus.OK;
    }
}
