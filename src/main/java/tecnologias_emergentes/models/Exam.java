package tecnologias_emergentes.models;

import jakarta.persistence.*;
import lombok.*;
import tecnologias_emergentes.enums.ExamType;
import tecnologias_emergentes.models.converters.ExamDataConverter;
import tecnologias_emergentes.models.records.ExamData;

import java.time.OffsetDateTime;

@Entity
@Table(name = "exam")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ExamType type;

    @Builder.Default
    @Column(name = "order_date", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime orderDate = OffsetDateTime.now();

    @Convert(converter = ExamDataConverter.class)
    @Column(name = "exam_data", columnDefinition = "jsonb")
    @org.hibernate.annotations.ColumnTransformer(write = "?::jsonb")
    private ExamData examData;

    @Builder.Default
    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false;
}