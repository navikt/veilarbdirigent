package no.nav.veilarbdirigent.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import no.nav.common.kafka.consumer.KafkaConsumerClient;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRepository;
import no.nav.common.kafka.consumer.feilhandtering.backoff.LinearBackoffStrategy;
import no.nav.common.kafka.consumer.feilhandtering.util.KafkaConsumerRecordProcessorBuilder;
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder;
import no.nav.common.kafka.consumer.util.deserializer.Deserializers;
import no.nav.common.kafka.spring.PostgresJdbcTemplateConsumerRepository;
import no.nav.veilarbdirigent.service.OppfolgingPeriodeService;
import no.nav.veilarbdirigent.service.OppfolgingsperiodeDto;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

import static no.nav.common.kafka.consumer.util.ConsumerUtils.findConsumerConfigsWithStoreOnFailure;
import static no.nav.common.kafka.util.KafkaPropertiesPreset.aivenDefaultConsumerProperties;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;

@Configuration
public class KafkaConfigAiven {

    public static final String CONSUMER_GROUP_ID = "veilarbdirigent-consumer";

    public enum Topic {
        OPPFOLGING_PERIODE("pto.oppfolgingsperiode-v1");
        @Getter
        final String topicName;

        Topic(String topicName) {
            this.topicName = topicName;
        }
    }

    private final KafkaConsumerRecordProcessor consumerRecordProcessor;

    private final List<KafkaConsumerClient> consumerClientAiven;

    public KafkaConfigAiven(JdbcTemplate jdbcTemplate, OppfolgingPeriodeService oppfolgingPeriodeService){
        MeterRegistry prometheusMeterRegistry = new MetricsReporter.ProtectedPrometheusMeterRegistry();
        KafkaConsumerRepository consumerRepository = new PostgresJdbcTemplateConsumerRepository(jdbcTemplate);
        List<KafkaConsumerClientBuilder.TopicConfig<?, ?>> topicConfigsAiven =
                List.of(
                        new KafkaConsumerClientBuilder.TopicConfig<String, OppfolgingsperiodeDto>()
                                .withLogging()
                                .withMetrics(prometheusMeterRegistry)
                                .withStoreOnFailure(consumerRepository)
                                .withConsumerConfig(
                                        Topic.OPPFOLGING_PERIODE.topicName,
                                        Deserializers.stringDeserializer(),
                                        Deserializers.jsonDeserializer(OppfolgingsperiodeDto.class),
                                        oppfolgingPeriodeService::behandleKafkaRecord
                                )
                );

        Properties aivenConsumerProperties = aivenDefaultConsumerProperties(CONSUMER_GROUP_ID);
        aivenConsumerProperties.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumerClientAiven = topicConfigsAiven.stream()
                .map(config ->
                        KafkaConsumerClientBuilder.builder()
                                .withProperties(aivenConsumerProperties)
                                .withTopicConfig(config)
                                .build())
                .toList();


        consumerRecordProcessor = KafkaConsumerRecordProcessorBuilder
                .builder()
                .withLockProvider(new JdbcTemplateLockProvider(jdbcTemplate))
                .withKafkaConsumerRepository(consumerRepository)
                .withConsumerConfigs(findConsumerConfigsWithStoreOnFailure(topicConfigsAiven))
                .withBackoffStrategy(new LinearBackoffStrategy(0, 2 * 60 * 60, 144))
                .build();
    }


    @PostConstruct
    public void start() {
        consumerRecordProcessor.start();
        consumerClientAiven.forEach(KafkaConsumerClient::start);
    }
}
