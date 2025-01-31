package com.example.mogakserver.common.config.rabbitMQ;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue queue() {
        return new Queue("user_ranking_queue", true);
    }
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(5); // 병렬 Consumer 개수
        factory.setMaxConcurrentConsumers(10); // 최대 Consumer 개수
        factory.setPrefetchCount(20); // 한 번에 20개 메시지 처리
        return factory;
    }
}