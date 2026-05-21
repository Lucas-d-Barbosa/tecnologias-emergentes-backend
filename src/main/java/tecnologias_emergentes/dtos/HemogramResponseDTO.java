package tecnologias_emergentes.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.models.records.ExamData;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HemogramResponseDTO(
        Long examId,
        Long customerId,
        CustomerClass customerClass,
        ExamData examData,
        String observation
) {
}