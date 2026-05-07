package tecnologias_emergentes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String profession;

    private String incomeBracket;

    private String postalCode; // CEP

    @Enumerated(EnumType.STRING)
    private AccessClass accessClass;
}
