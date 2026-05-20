package tecnologias_emergentes.dtos;

import tecnologias_emergentes.enums.ExamType;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Exam;
import tecnologias_emergentes.models.records.ExamData;
import java.time.OffsetDateTime;

public record ExamDTO(
        Long customerId,
        ExamType type,
        ExamData examData,
        Boolean isAbnormal
) {
    public static Exam mapperToExam(ExamDTO dto, Customer customer) {
        return Exam.builder()
                .customer(customer)
                .type(dto.type())
                .examData(dto.examData())
                .isAbnormal(dto.isAbnormal() != null ? dto.isAbnormal() : false)
                .orderDate(OffsetDateTime.now())
                .build();
    }
}