package tecnologias_emergentes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.dtos.CustomerCreateResponseDTO;
import tecnologias_emergentes.dtos.CustomerDTO;
import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.models.Schedule;
import tecnologias_emergentes.repositories.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private ExamService examService;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void savePremiumIncludesSchedule() {
        AddressDTO addressDTO = new AddressDTO(
                new BigDecimal("-7.237142"),
                new BigDecimal("-39.312403"),
                "Juazeiro do Norte",
                "Av. Padre Cicero",
                2000
        );

        CustomerDTO customerDTO = new CustomerDTO(
                "Hans Oliveira",
                "hans@dev.com",
                CustomerClass.PREMIUM,
                addressDTO
        );

        Address address = Address.builder()
                .id(1L)
                .latitude(addressDTO.latitude())
                .longitude(addressDTO.longitude())
                .city(addressDTO.city())
                .street(addressDTO.street())
                .houseNumber(addressDTO.houseNumber())
                .build();

        Customer savedCustomer = Customer.builder()
                .id(2L)
                .name(customerDTO.name())
                .email(customerDTO.email())
                .customerClass(CustomerClass.PREMIUM)
                .address(address)
                .build();

        Hospital hospital = Hospital.builder()
                .id(3L)
                .categoryName("Hospital Proximo")
                .categoryType("Privado")
                .address(address)
                .build();

        Schedule schedule = Schedule.builder()
                .id(4L)
                .customer(savedCustomer)
                .hospital(hospital)
                .serviceCode(1001L)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .build();

        when(addressService.resolveOrCreate(addressDTO)).thenReturn(address);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(scheduleService.createAutomaticScheduleForCustomer(savedCustomer)).thenReturn(schedule);

        ResponseEntity<CustomerCreateResponseDTO> response = customerService.save(customerDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CustomerCreateResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.autoSchedule()).isNotNull();
        assertThat(body.autoSchedule().hospitalId()).isEqualTo(hospital.getId());

        verify(examService).createAutomaticHemogram(savedCustomer);
        verify(scheduleService).createAutomaticScheduleForCustomer(savedCustomer);
    }

    @Test
    void saveStandardDoesNotCreateSchedule() {
        AddressDTO addressDTO = new AddressDTO(
                new BigDecimal("-7.237142"),
                new BigDecimal("-39.312403"),
                "Juazeiro do Norte",
                "Av. Padre Cicero",
                2000
        );

        CustomerDTO customerDTO = new CustomerDTO(
                "Hans Oliveira",
                "hans@dev.com",
                CustomerClass.STANDARD,
                addressDTO
        );

        Address address = Address.builder()
                .id(1L)
                .latitude(addressDTO.latitude())
                .longitude(addressDTO.longitude())
                .city(addressDTO.city())
                .street(addressDTO.street())
                .houseNumber(addressDTO.houseNumber())
                .build();

        Customer savedCustomer = Customer.builder()
                .id(2L)
                .name(customerDTO.name())
                .email(customerDTO.email())
                .customerClass(CustomerClass.STANDARD)
                .address(address)
                .build();

        when(addressService.resolveOrCreate(addressDTO)).thenReturn(address);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        ResponseEntity<CustomerCreateResponseDTO> response = customerService.save(customerDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CustomerCreateResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.autoSchedule()).isNull();

        verify(examService).createAutomaticHemogram(savedCustomer);
        verify(scheduleService, never()).createAutomaticScheduleForCustomer(any(Customer.class));
    }
}
