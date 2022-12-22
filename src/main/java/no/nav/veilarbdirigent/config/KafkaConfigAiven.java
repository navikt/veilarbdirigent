package no.nav.veilarbdirigent.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.kafka.consumer.KafkaConsumerClient;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRepository;
import no.nav.common.kafka.consumer.feilhandtering.backoff.LinearBackoffStrategy;
import no.nav.common.kafka.consumer.feilhandtering.util.KafkaConsumerRecordProcessorBuilder;
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder;
import no.nav.common.kafka.consumer.util.deserializer.Deserializers;
import no.nav.common.kafka.spring.OracleJdbcTemplateConsumerRepository;
import no.nav.pto_schema.kafka.json.topic.SisteOppfolgingsperiodeV1;
import no.nav.veilarbdirigent.service.OppfolgingPeriodeService;
import no.nav.veilarbdirigent.unleash.KafkaAivenUnleash;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static no.nav.common.kafka.consumer.util.ConsumerUtils.findConsumerConfigsWithStoreOnFailure;
import static no.nav.common.kafka.util.KafkaPropertiesPreset.aivenDefaultConsumerProperties;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;

@Configuration
public class KafkaConfigAiven {

    public final static String CONSUMER_GROUP_ID = "veilarbdirigent-consumer";

    public enum Topic {
        OPPFOLGING_PERIODE("pto.siste-oppfolgingsperiode-v1");
        @Getter
        final String topicName;

        Topic(String topicName) {
            this.topicName = topicName;
        }
    }

    private final KafkaConsumerRecordProcessor consumerRecordProcessor;

    private final List<KafkaConsumerClient> consumerClientAiven;

    public KafkaConfigAiven(JdbcTemplate jdbcTemplate,
                            UnleashClient unleashClient, OppfolgingPeriodeService oppfolgingPeriodeService){
        MeterRegistry prometheusMeterRegistry = new MetricsReporter.ProtectedPrometheusMeterRegistry();
        KafkaConsumerRepository consumerRepository = new OracleJdbcTemplateConsumerRepository(jdbcTemplate);
        List<KafkaConsumerClientBuilder.TopicConfig<?, ?>> topicConfigsAiven =
                List.of(
                        new KafkaConsumerClientBuilder.TopicConfig<String, SisteOppfolgingsperiodeV1>()
                                .withLogging()
                                .withMetrics(prometheusMeterRegistry)
                                .withStoreOnFailure(consumerRepository)
                                .withConsumerConfig(
                                        Topic.OPPFOLGING_PERIODE.topicName,
                                        Deserializers.stringDeserializer(),
                                        Deserializers.jsonDeserializer(SisteOppfolgingsperiodeV1.class),
                                        oppfolgingPeriodeService::behandleKafkaRecord
                                )
                );

        KafkaAivenUnleash kafkaAivenUnleash = new KafkaAivenUnleash(unleashClient);
        Properties aivenConsumerProperties = aivenDefaultConsumerProperties(CONSUMER_GROUP_ID);
        aivenConsumerProperties.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumerClientAiven = topicConfigsAiven.stream()
                .map(config ->
                        KafkaConsumerClientBuilder.builder()
                                .withProperties(aivenConsumerProperties)
                                .withTopicConfig(config)
                                .withToggle(kafkaAivenUnleash)
                                .build())
                .collect(Collectors.toList());


        consumerRecordProcessor = KafkaConsumerRecordProcessorBuilder
                .builder()
                .withLockProvider(new JdbcTemplateLockProvider(jdbcTemplate))
                .withKafkaConsumerRepository(kafkaConsumerRepository)
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
