package no.nav.veilarbdirigent.config;

import io.micrometer.core.instrument.MeterRegistry;
import net.javacrumbs.shedlock.core.LockProvider;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.kafka.consumer.KafkaConsumerClient;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRepository;
import no.nav.common.kafka.consumer.feilhandtering.util.KafkaConsumerRecordProcessorBuilder;
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder;
import no.nav.common.kafka.consumer.util.TopicConsumerConfig;
import no.nav.common.kafka.spring.OracleJdbcTemplateConsumerRepository;
import no.nav.common.utils.Credentials;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static no.nav.common.kafka.util.KafkaPropertiesPreset.onPremDefaultConsumerProperties;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    public static final String CONSUMER_GROUP_ID = "veilarbdirigent-consumer";
    private static final String ONPREM_KAFKA_DISABLED = "veilarbdirigent.kafka.onprem.consumer.disabled";

    @Bean
    public KafkaConsumerRepository kafkaConsumerRepository(JdbcTemplate jdbcTemplate) {
        return new OracleJdbcTemplateConsumerRepository(jdbcTemplate);
    }

    @Bean
    public KafkaConsumerClient consumerClient(
            List<TopicConsumerConfig<?, ?>> topicConfigs,
            Credentials credentials,
            KafkaProperties kafkaProperties,
            MeterRegistry meterRegistry,
            UnleashClient unleashClient,
            KafkaConsumerRepository kafkaConsumerRepository
    ) {
        var clientBuilder = KafkaConsumerClientBuilder.builder()
                .withProperties(onPremDefaultConsumerProperties(CONSUMER_GROUP_ID, kafkaProperties.getBrokersUrl(), credentials))
                .withToggle(() -> unleashClient.isEnabled(ONPREM_KAFKA_DISABLED));

        topicConfigs.forEach(it ->
            clientBuilder.withTopicConfig(
                    new KafkaConsumerClientBuilder
                            .TopicConfig()
                            .withConsumerConfig(it)
                            .withMetrics(meterRegistry)
                            .withLogging()
                            .withStoreOnFailure(kafkaConsumerRepository)
            )
        );

        var client = clientBuilder.build();
        client.start();

        return client;
    }

    @Bean
    public KafkaConsumerRecordProcessor consumerRecordProcessor(
            List<TopicConsumerConfig<?, ?>> topicConfigs,
            LockProvider lockProvider,
            KafkaConsumerRepository kafkaConsumerRepository
    ) {
        KafkaConsumerRecordProcessor kafkaConsumerRecordProcessor = KafkaConsumerRecordProcessorBuilder
                .builder()
                .withLockProvider(lockProvider)
                .withKafkaConsumerRepository(kafkaConsumerRepository)
                .withConsumerConfigs(topicConfigs)
                .build();

        kafkaConsumerRecordProcessor.start();

        return kafkaConsumerRecordProcessor;
    }
}
