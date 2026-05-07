package tecnologias_emergentes;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import tecnologias_emergentes.model.User;
import tecnologias_emergentes.model.AccessClass;

@TestConfiguration
public class TestDataFactory {

    @Bean
    public User vipUser() {
        return new User(null, "Dra. Maria Silva", "doctor", ">10000", "01311-000", AccessClass.VIP);
    }

    @Bean
    public User standardUser() {
        return new User(null, "Carlos Pereira", "engineer", "5000-10000", "20040-010", AccessClass.STANDARD);
    }

    @Bean
    public User baseUser() {
        return new User(null, "João Santos", "student", "<2000", "57000-000", AccessClass.BASE);
    }
}
