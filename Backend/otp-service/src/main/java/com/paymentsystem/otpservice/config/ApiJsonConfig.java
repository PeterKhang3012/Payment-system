package com.paymentsystem.otpservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;

@Configuration
public class ApiJsonConfig {
    // API validation: payment IDs, email and OTP values must be JSON strings.
    @Bean
    Jackson2ObjectMapperBuilderCustomizer strictApiStrings() {
        return builder -> builder.postConfigurer(mapper -> {
            var config = mapper.coercionConfigFor(LogicalType.Textual);
            config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
            config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
            config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
        });
    }
}
