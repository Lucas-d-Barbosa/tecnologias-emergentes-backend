package tecnologias_emergentes.dtos;

import java.time.OffsetDateTime;
import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.models.Schedule;

public record AutoScheduleResponseDTO(
        Long scheduleId,
        Long serviceCode,
        OffsetDateTime scheduledAt,
        Long customerId,
        String customerName,
        CustomerClass customerClass,
        AddressDTO customerAddress,
        Long hospitalId,
        String hospitalName,
        String hospitalType,
        AddressDTO hospitalAddress
) {
    public static AutoScheduleResponseDTO fromSchedule(Schedule schedule) {
        return new AutoScheduleResponseDTO(
                schedule.getId(),
                schedule.getServiceCode(),
                schedule.getScheduledAt(),
                schedule.getCustomer().getId(),
                schedule.getCustomer().getName(),
                schedule.getCustomer().getCustomerClass(),
                schedule.getCustomer().getAddress() != null
                        ? AddressDTO.mapperToAddressDTO(schedule.getCustomer().getAddress())
                        : null,
                schedule.getHospital().getId(),
                schedule.getHospital().getCategoryName(),
                schedule.getHospital().getCategoryType(),
                schedule.getHospital().getAddress() != null
                        ? AddressDTO.mapperToAddressDTO(schedule.getHospital().getAddress())
                        : null
        );
    }
}
