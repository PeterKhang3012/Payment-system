package com.myproject.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.myproject.payment.dto.PaymentAttempt;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, PaymentAttempt> paymentAttemptRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, PaymentAttempt> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Key: String
        template.setKeySerializer(
                new StringRedisSerializer());

        // Value: PaymentAttempt -> JSON
        template.setValueSerializer(
                new JacksonJsonRedisSerializer<>(PaymentAttempt.class));

        // Hash key: String
        template.setHashKeySerializer(
                new StringRedisSerializer());

        // Hash value: PaymentAttempt -> JSON
        template.setHashValueSerializer(
                new JacksonJsonRedisSerializer<>(PaymentAttempt.class));

        template.afterPropertiesSet();

        return template;
    }
}
