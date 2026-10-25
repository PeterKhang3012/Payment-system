package com.paymentsystem.otpservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentsystem.otpservice.dto.OtpDtos.VerifyOtpRequest;
import static org.junit.jupiter.api.Assertions.*;

// API parsing regression: test production configuration without SMTP or database infrastructure.
class JsonParsingTest {
    @Test void incorrectScalarTypesAreRejected() {
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        var properties = yaml.getObject();
        var values = properties.stringPropertyNames().stream().filter(k -> k.startsWith("spring.jackson."))
            .map(k -> k + "=" + properties.getProperty(k)).toArray(String[]::new);
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(com.paymentsystem.otpservice.config.ApiJsonConfig.class)
            .withPropertyValues(values).run(context -> {
                var mapper = context.getBean(ObjectMapper.class);
                assertThrows(Exception.class, () -> mapper.readValue("{\"paymentId\":12,\"code\":123456}", VerifyOtpRequest.class));
                assertEquals("123456",mapper.readValue("{\"paymentId\":\"p\",\"code\":\"123456\"}",VerifyOtpRequest.class).code());
            });
    }
}
