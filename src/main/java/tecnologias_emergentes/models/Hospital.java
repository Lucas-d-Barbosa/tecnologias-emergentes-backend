package tecnologias_emergentes.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hospital")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long id;

    @Column(name = "category_name", nullable = false, length = 120)
    private String categoryName;

    @Column(name = "category_type", nullable = false, length = 60)
    private String categoryType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
}