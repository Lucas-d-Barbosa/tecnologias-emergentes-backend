package tecnologias_emergentes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tecnologias_emergentes.models.Hospital;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}