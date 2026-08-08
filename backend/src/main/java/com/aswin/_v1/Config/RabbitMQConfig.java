package com.aswin._v1.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
 @Configuration
public class RabbitMQConfig {
@Bean
public Queue submissionQueue() {
    return new Queue("submission_queue", true);
}
}
