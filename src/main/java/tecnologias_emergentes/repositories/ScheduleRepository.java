package tecnologias_emergentes.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tecnologias_emergentes.models.Schedule;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // Agendamentos em Hospitais Específicos paginado (Requisito 4 do Guia)
    @Query(value = """
        SELECT 
            s.scheduled_at AS scheduledAt, 
            c.name AS patient, 
            h.category_name AS hospital
        FROM schedule s
        JOIN customer c ON c.customer_id = s.customer_id
        JOIN hospital h ON h.hospital_id = s.hospital_id
        ORDER BY s.scheduled_at DESC
        """,
            countQuery = "SELECT count(*) FROM schedule",
            nativeQuery = true)
    Page<ScheduleReportProjection> findSchedulesReport(Pageable pageable);

    Optional<Schedule> findFirstByCustomer_IdOrderByScheduledAtDesc(Long customerId);

    // Projeção para capturar o resultado do relatório de agendamentos
    interface ScheduleReportProjection {
        java.time.OffsetDateTime getScheduledAt();
        String getPatient();
        String getHospital();
    }
}