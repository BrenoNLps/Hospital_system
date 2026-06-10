package br.ifsp.hospital.integration.persistence;

import br.ifsp.hospital.annotation.PersistenceTest;
import br.ifsp.hospital.domain.model.AppointmentStatus;
import br.ifsp.hospital.domain.model.InsuranceType;
import br.ifsp.hospital.infrastructure.entity.DoctorEntity;
import br.ifsp.hospital.infrastructure.entity.MedicalAppointmentEntity;
import br.ifsp.hospital.infrastructure.entity.PatientEntity;
import br.ifsp.hospital.infrastructure.repository.SpringDataAppointmentRepository;
import br.ifsp.hospital.infrastructure.repository.SpringDataDoctorRepository;
import br.ifsp.hospital.infrastructure.repository.SpringDataPatientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@PersistenceTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("Testes de persistência – findByPatientIdAndStatus")
class AppointmentRepositoryPersistenceTest {

    @Autowired
    private SpringDataAppointmentRepository appointmentRepository;

    @Autowired
    private SpringDataPatientRepository patientRepository;

    @Autowired
    private SpringDataDoctorRepository doctorRepository;

    @AfterEach
    void tearDown() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
    }

    private PatientEntity savedPatient() {
        return patientRepository.save(PatientEntity.builder()
                .id(UUID.randomUUID())
                .name("Paciente Teste")
                .document(UUID.randomUUID().toString())
                .insuranceType(InsuranceType.BASIC)
                .build());
    }

    private DoctorEntity savedDoctor() {
        return doctorRepository.save(DoctorEntity.builder()
                .id(UUID.randomUUID())
                .name("Dr. Teste")
                .specialty("Cardiologia")
                .license("CRM-SP " + UUID.randomUUID().toString().substring(0, 6))
                .build());
    }

    private MedicalAppointmentEntity savedAppointment(PatientEntity patient, DoctorEntity doctor, AppointmentStatus status) {
        return appointmentRepository.save(MedicalAppointmentEntity.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .doctor(doctor)
                .status(status)
                .createdAt(LocalDateTime.now())
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .rescheduleCount(0)
                .build());
    }

    @Test
    @DisplayName("Deve retornar atendimentos quando patientId e status correspondem")
    void shouldReturnAppointmentsWhenPatientIdAndStatusMatch() {
        PatientEntity patient = savedPatient();
        DoctorEntity doctor = savedDoctor();
        MedicalAppointmentEntity appointment = savedAppointment(patient, doctor, AppointmentStatus.OPEN);

        List<MedicalAppointmentEntity> result = appointmentRepository
                .findByPatientIdAndStatus(patient.getId(), AppointmentStatus.OPEN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(appointment.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando patientId não existe")
    void shouldReturnEmptyListWhenPatientIdDoesNotExist() {
        List<MedicalAppointmentEntity> result = appointmentRepository
                .findByPatientIdAndStatus(UUID.randomUUID(), AppointmentStatus.OPEN);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando patientId é correto mas status é diferente")
    void shouldReturnEmptyListWhenStatusDoesNotMatch() {
        PatientEntity patient = savedPatient();
        savedAppointment(patient, savedDoctor(), AppointmentStatus.OPEN);

        List<MedicalAppointmentEntity> result = appointmentRepository
                .findByPatientIdAndStatus(patient.getId(), AppointmentStatus.CLOSED);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar apenas os atendimentos com o status solicitado quando paciente tem múltiplos status")
    void shouldReturnOnlyMatchingStatusWhenPatientHasMultipleAppointments() {
        PatientEntity patient = savedPatient();
        MedicalAppointmentEntity openAppointment = savedAppointment(patient, savedDoctor(), AppointmentStatus.OPEN);
        savedAppointment(patient, savedDoctor(), AppointmentStatus.CLOSED);

        List<MedicalAppointmentEntity> result = appointmentRepository
                .findByPatientIdAndStatus(patient.getId(), AppointmentStatus.OPEN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(openAppointment.getId());
    }

}
