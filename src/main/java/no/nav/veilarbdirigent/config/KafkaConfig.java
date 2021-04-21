package no.nav.veilarbdirigent.config;

import io.micrometer.core.instrument.MeterRegistry;
import net.javacrumbs.shedlock.core.LockProvider;
import no.nav.common.kafka.consumer.KafkaConsumerClient;
import no.nav.common.kafka.consumer.TopicConsumer;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRepository;
import no.nav.common.kafka.consumer.feilhandtering.OracleConsumerRepository;
import no.nav.common.kafka.consumer.feilhandtering.StoredRecordConsumer;
import no.nav.common.kafka.consumer.feilhandtering.util.KafkaConsumerRecordProcessorBuilder;
import no.nav.common.kafka.consumer.util.ConsumerUtils;
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder;
import no.nav.common.kafka.util.KafkaPropertiesBuilder;
import no.nav.common.utils.Credentials;
import no.nav.veilarbdirigent.domain.OppfolgingStartetKafkaDTO;
import no.nav.veilarbdirigent.service.KafkaConsumerService;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

import static no.nav.common.kafka.consumer.util.ConsumerUtils.jsonConsumer;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfig {

    public final static String CONSUMER_GROUP_ID = "veilarbdirigent-consumer";

    @Autowired
    KafkaConsumerClient<String, String> consumerClient;

    @Autowired
    KafkaConsumerRecordProcessor consumerRecordProcessor;

    @Bean
    public KafkaConsumerRepository kafkaConsumerRepository(DataSource dataSource) {
        return new OracleConsumerRepository(dataSource);
    }

    @Bean
    public Map<String, TopicConsumer<String, String>> topicConsumers(
            KafkaConsumerService kafkaConsumerService,
            KafkaProperties kafkaProperties
    ) {
        return Map.of(
                kafkaProperties.getOppfolgingStartetTopic(),
                jsonConsumer(OppfolgingStartetKafkaDTO.class, kafkaConsumerService::behandleOppfolgingStartet)
        );
    }

    @Bean
    public KafkaConsumerClient<String, String> consumerClient(
            Map<String, TopicConsumer<String, String>> topicConsumers,
            KafkaConsumerRepository kafkaConsumerRepository,
            Credentials credentials,
            KafkaProperties kafkaProperties,
            MeterRegistry meterRegistry
    ) {
        Properties properties = KafkaPropertiesBuilder.consumerBuilder()
                .withBaseProperties()
                .withConsumerGroupId(CONSUMER_GROUP_ID)
                .withBrokerUrl(kafkaProperties.getBrokersUrl())
                .withOnPremAuth(credentials)
                .withDeserializers(StringDeserializer.class, StringDeserializer.class)
                .build();

        return KafkaConsumerClientBuilder.<String, String>builder()
                .withProps(properties)
                .withRepository(kafkaConsumerRepository)
                .withSerializers(new StringSerializer(), new StringSerializer())
                .withStoreOnFailureConsumers(topicConsumers)
                .withMetrics(meterRegistry)
                .withLogging()
                .build();
    }

    @Bean
    public KafkaConsumerRecordProcessor consumerRecordProcessor(
            LockProvider lockProvider,
            KafkaConsumerRepository kafkaConsumerRepository,
            Map<String, TopicConsumer<String, String>> topicConsumers
    ) {
        Map<String, StoredRecordConsumer> storedRecordConsumers = ConsumerUtils.toStoredRecordConsumerMap(
                topicConsumers,
                new StringDeserializer(),
                new StringDeserializer()
        );

        return KafkaConsumerRecordProcessorBuilder
                .builder()
                .withLockProvider(lockProvider)
                .withKafkaConsumerRepository(kafkaConsumerRepository)
                .withRecordConsumers(storedRecordConsumers)
                .build();
    }


    @PostConstruct
    public void start() {
        consumerClient.start();
        consumerRecordProcessor.start();
    }
}
