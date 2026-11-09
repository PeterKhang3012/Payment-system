package com.myproject.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import tools.jackson.databind.json.JsonMapper;
import com.myproject.payment.dto.PaymentRequest;
import com.myproject.payment.dto.OtpDtos.VerifyOtpRequest;
import static org.junit.jupiter.api.Assertions.*;

// API parsing regression: verify the production Jackson properties, independently of database/JWT setup.
class JsonParsingTest {
    @Test void productionConfigurationRejectsIncorrectScalarTypes() throws Exception {
        var properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
        var values = properties.stringPropertyNames().stream().filter(k -> k.startsWith("spring.jackson."))
            .map(k -> k + "=" + properties.getProperty(k)).toArray(String[]::new);
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(com.myproject.payment.config.ApiJsonConfig.class)
            .withPropertyValues(values).run(context -> {
                JsonMapper mapper = context.getBean(JsonMapper.class);
                for (String body : new String[]{"{\"studentId\":1.5,\"acceptedTerms\":true}",
                        "{\"studentId\":1,\"acceptedTerms\":true}",
                        "{\"studentId\":\"1\",\"acceptedTerms\":\"true\"}"}) {
                    assertThrows(Exception.class, () -> mapper.readValue(body, PaymentRequest.class));
                }
                assertThrows(Exception.class, () -> mapper.readValue("{\"paymentId\":12,\"code\":123456}", VerifyOtpRequest.class));
                assertEquals("001", mapper.readValue("{\"studentId\":\" 001 \",\"acceptedTerms\":true}", PaymentRequest.class).getStudentId());
            });
    }
}
