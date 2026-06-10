package br.ifsp.hospital.integration.persistence;

import br.ifsp.hospital.annotation.PersistenceTest;
import br.ifsp.hospital.domain.model.InsuranceType;
import br.ifsp.hospital.infrastructure.entity.PatientEntity;
import br.ifsp.hospital.infrastructure.repository.SpringDataPatientRepository;
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
@DisplayName("Testes de persistência – unicidade de patient.document")
class PatientRepositoryPersistenceTest {

    @Autowired
    private SpringDataPatientRepository patientRepository;

    @AfterEach
    void tearDown() {
        patientRepository.deleteAll();
    }

    private PatientEntity buildPatient(String document) {
        return PatientEntity.builder()
                .id(UUID.randomUUID())
                .name("Paciente Teste")
                .document(document)
                .insuranceType(InsuranceType.BASIC)
                .build();
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar dois pacientes com o mesmo documento")
    void shouldThrowExceptionWhenDocumentIsDuplicate() {
        String document = "111.222.333-44";

        patientRepository.saveAndFlush(buildPatient(document));

        assertThatThrownBy(() -> patientRepository.saveAndFlush(buildPatient(document)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve persistir dois pacientes com documentos diferentes sem erro")
    void shouldSaveTwoPatientsWithDifferentDocuments() {
        patientRepository.saveAndFlush(buildPatient("111.111.111-11"));
        patientRepository.saveAndFlush(buildPatient("222.222.222-22"));

        assertThat(patientRepository.findAll()).hasSize(2);
    }
}
