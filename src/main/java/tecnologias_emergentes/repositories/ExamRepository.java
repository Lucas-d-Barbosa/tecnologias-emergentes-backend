package tecnologias_emergentes.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tecnologias_emergentes.models.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query(value = """
        SELECT 
            c.name AS patient, 
            e.type AS testType, 
            e.order_date AS orderDate,
            e.exam_data->'erythrogram'->'hemoglobin'->>'value' AS hemoglobinResult
        FROM customer c 
        JOIN exam e ON e.customer_id = c.customer_id
        WHERE e.is_abnormal = false
        """,
            countQuery = "SELECT count(*) FROM exam WHERE is_abnormal = false",
            nativeQuery = true)
    Page<ExamReportProjection> findNormalExamsReport(Pageable pageable);

    // Projeção (Interface) para receber o resultado dinâmico do SQL Nativo
    interface ExamReportProjection {
        String getPatient();
        String getTestType();
        java.time.OffsetDateTime getOrderDate();
        String getHemoglobinResult();
    }
}