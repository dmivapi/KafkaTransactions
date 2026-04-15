package com.appsdeveloperblog.estore.DepositService;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import com.appsdeveloperblog.payments.ws.core.error.NotRetryableException;
import com.appsdeveloperblog.payments.ws.core.error.RetryableException;

@Configuration
public class KafkaConsumerConfiguration {

	@Bean
	ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> config = kafkaProperties.buildConsumerProperties();

		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);

		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
			ConsumerFactory<String, Object> consumerFactory, KafkaTemplate<String, Object> kafkaTemplate) {

		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		DefaultErrorHandler errorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate),
				new FixedBackOff(5000, 3));
		errorHandler.addNotRetryableExceptions(NotRetryableException.class);
		errorHandler.addRetryableExceptions(RetryableException.class);
		factory.setCommonErrorHandler(errorHandler);

		return factory;
	}

	@Bean
	KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> config = kafkaProperties.buildConsumerProperties();

		// here we can override properties, otherwise we don't need this method, Spring Boot will autoconfigure

		return new DefaultKafkaProducerFactory<>(config);
	}

}
