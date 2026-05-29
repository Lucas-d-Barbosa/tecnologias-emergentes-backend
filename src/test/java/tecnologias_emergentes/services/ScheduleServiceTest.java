package tecnologias_emergentes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.models.Schedule;
import tecnologias_emergentes.repositories.CustomerRepository;
import tecnologias_emergentes.repositories.HospitalRepository;
import tecnologias_emergentes.repositories.ScheduleRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private HospitalService hospitalService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void createAutomaticScheduleForCustomerReturnsNullForStandard() {
        Customer customer = Customer.builder()
                .id(1L)
                .customerClass(CustomerClass.STANDARD)
                .build();

        Schedule result = scheduleService.createAutomaticScheduleForCustomer(customer);

        assertThat(result).isNull();
        verifyNoInteractions(hospitalService);
        verifyNoInteractions(scheduleRepository);
    }

    @Test
    void createAutomaticScheduleForCustomerCreatesForPremium() {
        Address address = Address.builder()
                .latitude(new BigDecimal("-7.2300"))
                .longitude(new BigDecimal("-39.3100"))
                .city("Juazeiro do Norte")
                .street("Rua A")
                .houseNumber(10)
                .build();

        Customer customer = Customer.builder()
                .id(1L)
                .customerClass(CustomerClass.PREMIUM)
                .address(address)
                .build();

        Hospital hospital = Hospital.builder()
                .id(2L)
                .address(address)
                .build();

        when(hospitalService.findOrCreateNearestHospital(address)).thenReturn(hospital);

        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
        when(scheduleRepository.save(scheduleCaptor.capture())).thenAnswer(invocation -> {
            Schedule saved = scheduleCaptor.getValue();
            saved.setId(10L);
            return saved;
        });

        OffsetDateTime start = OffsetDateTime.now();

        Schedule result = scheduleService.createAutomaticScheduleForCustomer(customer);

        assertThat(result).isNotNull();
        Schedule saved = scheduleCaptor.getValue();
        assertThat(saved.getServiceCode()).isEqualTo(1001L);
        assertThat(saved.getHospital()).isEqualTo(hospital);
        assertThat(saved.getCustomer()).isEqualTo(customer);
        assertThat(saved.getScheduledAt()).isAfter(start);
    }
}
