package br.ifsp.hospital.annotation;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Tag("IntegrationTest")
public @interface IntegrationTest {
}
