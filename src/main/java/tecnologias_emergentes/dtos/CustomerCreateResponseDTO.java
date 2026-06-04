package tecnologias_emergentes.dtos;

import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Schedule;

public record CustomerCreateResponseDTO(
        Long id,
        String name,
        String email,
        CustomerClass customerClass,
        AddressDTO address,
        AutoScheduleResponseDTO autoSchedule
) {
    public static CustomerCreateResponseDTO from(Customer customer, Schedule schedule) {
        AutoScheduleResponseDTO scheduleResponse = schedule != null
                ? AutoScheduleResponseDTO.fromSchedule(schedule)
                : null;

        return new CustomerCreateResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getCustomerClass(),
                customer.getAddress() != null ? AddressDTO.mapperToAddressDTO(customer.getAddress()) : null,
                scheduleResponse
        );
    }
}
