package tecnologias_emergentes.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.geo.Point;


@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Point latitude;

    @Column
    private  Point longitude;

    @Column
    private String city;

    @Column
    private String street;

    @Column
    private Integer houseNumber;
}
