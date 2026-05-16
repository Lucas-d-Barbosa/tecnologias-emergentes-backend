package tecnologias_emergentes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tecnologias_emergentes.models.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
