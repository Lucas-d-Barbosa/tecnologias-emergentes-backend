package tecnologias_emergentes.dtos;

import jakarta.validation.constraints.NotNull;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.models.Schedule;
import java.time.OffsetDateTime;

public record ScheduleDTO(
        @NotNull(message = "O código de serviço é obrigatório.")
        Long serviceCode,
        @NotNull(message = "O hospital é obrigatório.")
        Long hospitalId,
        @NotNull(message = "O cliente é obrigatório.")
        Long customerId,
        @NotNull(message = "A data de agendamento é obrigatória.")
        OffsetDateTime scheduledAt
) {
    public static Schedule mapperToSchedule(ScheduleDTO dto, Hospital hospital, Customer customer) {
        return Schedule.builder()
                .serviceCode(dto.serviceCode())
                .hospital(hospital)
                .customer(customer)
                .scheduledAt(dto.scheduledAt())
                .build();
    }
}