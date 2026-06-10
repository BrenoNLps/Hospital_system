package br.ifsp.hospital.integration.persistence;

import br.ifsp.hospital.annotation.PersistenceTest;
import br.ifsp.hospital.infrastructure.entity.DoctorEntity;
import br.ifsp.hospital.infrastructure.repository.SpringDataDoctorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@PersistenceTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("Testes de persistência – unicidade de doctor.license")
class DoctorRepositoryPersistenceTest {

    @Autowired
    private SpringDataDoctorRepository doctorRepository;

    @AfterEach
    void tearDown() {
        doctorRepository.deleteAll();
    }

    private DoctorEntity buildDoctor(String license) {
        return DoctorEntity.builder()
                .id(UUID.randomUUID())
                .name("Dr. Teste")
                .specialty("Cardiologia")
                .license(license)
                .build();
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar dois médicos com o mesmo CRM")
    void shouldThrowExceptionWhenLicenseIsDuplicate() {
        String license = "CRM-SP 123456";

        doctorRepository.saveAndFlush(buildDoctor(license));

        assertThatThrownBy(() -> doctorRepository.saveAndFlush(buildDoctor(license)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve persistir dois médicos com CRMs diferentes sem erro")
    void shouldSaveTwoDoctorsWithDifferentLicenses() {
        doctorRepository.saveAndFlush(buildDoctor("CRM-SP 111111"));
        doctorRepository.saveAndFlush(buildDoctor("CRM-SP 222222"));

        assertThat(doctorRepository.findAll()).hasSize(2);
    }
}
