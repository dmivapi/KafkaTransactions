package com.appsdeveloperblog.estore.transfers.config;

import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.withdraw-money-topic}")
    private String withdrawTopicName;

    @Value("${app.kafka.deposit-money-topic}")
    private String depositTopicName;

    @Bean
    ProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
        Map<String, Object> config = properties.buildProducerProperties();
        DefaultKafkaProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(config);

        String transactionalIdPrefix = properties.getProducer().getTransactionIdPrefix();
        if (transactionalIdPrefix != null && !transactionalIdPrefix.isEmpty()) {
            producerFactory.setTransactionIdPrefix(transactionalIdPrefix);
        }
        // dynamically override properties here
        return producerFactory;
    }

    @Bean("transactionManager")
    JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }


    @Bean
    KafkaTransactionManager<String, Object> kafkaTransactionManager(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic createWithdrawTopic() {
        return TopicBuilder.name(withdrawTopicName).partitions(3).replicas(3).build();
    }

    @Bean
    NewTopic createDepositTopic() {
        return TopicBuilder.name(depositTopicName).partitions(3).replicas(3).build();
    }
}
