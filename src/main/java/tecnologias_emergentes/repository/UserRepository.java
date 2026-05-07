package tecnologias_emergentes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tecnologias_emergentes.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
