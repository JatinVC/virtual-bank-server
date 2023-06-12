package com.jatinc.ebank.config;

import com.jatinc.ebank.dto.PaymentDTO;
import com.jatinc.ebank.topology.PaymentTopology;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Properties;

@Configuration
public class PaymentStreamConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public Properties getConfiguration(){
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "transactions");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, PaymentDTO.class);
        return properties;
    }

    @Bean
    public KafkaStreams kafkaStreams(){
        Properties configuration = getConfiguration();
        Topology topology = PaymentTopology.buildTopology();
        KafkaStreams kafkaStreams = new KafkaStreams(topology, configuration);

        kafkaStreams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        return kafkaStreams;
    }
}