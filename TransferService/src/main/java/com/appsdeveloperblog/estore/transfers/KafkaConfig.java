package com.appsdeveloperblog.estore.transfers;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

	@Value("${app.kafka.withdraw-money-topic}")
	private String withdrawTopicName;

	@Value("${app.kafka.deposit-money-topic}")
	private String depositTopicName;

	@Bean
	ProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
		Map<String, Object> config = properties.buildProducerProperties();

		// dynamically override properties here
		return new DefaultKafkaProducerFactory<>(config);
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
