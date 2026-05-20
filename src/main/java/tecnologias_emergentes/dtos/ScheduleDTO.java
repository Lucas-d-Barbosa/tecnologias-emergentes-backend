package tecnologias_emergentes.dtos;

import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.models.Schedule;
import java.time.OffsetDateTime;

public record ScheduleDTO(
        Long serviceCode,
        Long hospitalId,
        Long customerId,
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