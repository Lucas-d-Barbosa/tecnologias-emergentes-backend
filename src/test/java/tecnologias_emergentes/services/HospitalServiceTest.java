package tecnologias_emergentes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.repositories.HospitalRepository;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private HospitalService hospitalService;

    @Test
    void findOrCreateNearestHospitalReturnsExistingWhenNear() {
        Address reference = Address.builder()
                .latitude(new BigDecimal("-7.2300"))
                .longitude(new BigDecimal("-39.3100"))
                .city("Juazeiro do Norte")
                .street("Rua A")
                .houseNumber(10)
                .build();

        Address hospitalAddress = Address.builder()
                .latitude(new BigDecimal("-7.2310"))
                .longitude(new BigDecimal("-39.3110"))
                .build();

        Hospital hospital = Hospital.builder()
                .id(1L)
                .address(hospitalAddress)
                .build();

        when(hospitalRepository.findAll()).thenReturn(List.of(hospital));

        Hospital result = hospitalService.findOrCreateNearestHospital(reference);

        assertThat(result).isSameAs(hospital);
        verify(addressService, never()).resolveOrCreate(any(AddressDTO.class));
        verify(hospitalRepository, never()).save(any(Hospital.class));
    }

    @Test
    void findOrCreateNearestHospitalCreatesWhenFar() {
        Address reference = Address.builder()
                .latitude(new BigDecimal("-7.2300"))
                .longitude(new BigDecimal("-39.3100"))
                .city("Juazeiro do Norte")
                .street("Rua A")
                .houseNumber(10)
                .build();

        Address farAddress = Address.builder()
                .latitude(new BigDecimal("-7.5000"))
                .longitude(new BigDecimal("-39.6000"))
                .build();

        Hospital farHospital = Hospital.builder()
                .id(2L)
                .address(farAddress)
                .build();

        Address createdAddress = Address.builder()
                .id(3L)
                .latitude(new BigDecimal("-7.2290"))
                .longitude(new BigDecimal("-39.3090"))
                .city("Juazeiro do Norte")
                .street("Rua A Proximo")
                .houseNumber(11)
                .build();

        Hospital createdHospital = Hospital.builder()
                .id(4L)
                .categoryName("Hospital Proximo")
                .categoryType("Privado")
                .address(createdAddress)
                .build();

        when(hospitalRepository.findAll()).thenReturn(List.of(farHospital));
        when(addressService.resolveOrCreate(any(AddressDTO.class))).thenReturn(createdAddress);
        when(hospitalRepository.save(any(Hospital.class))).thenReturn(createdHospital);

        Hospital result = hospitalService.findOrCreateNearestHospital(reference);

        assertThat(result).isEqualTo(createdHospital);

        ArgumentCaptor<AddressDTO> addressCaptor = ArgumentCaptor.forClass(AddressDTO.class);
        verify(addressService).resolveOrCreate(addressCaptor.capture());

        AddressDTO dto = addressCaptor.getValue();
        assertThat(dto.latitude()).isEqualByComparingTo(new BigDecimal("-7.2290"));
        assertThat(dto.longitude()).isEqualByComparingTo(new BigDecimal("-39.3090"));
        assertThat(dto.street()).isEqualTo("Rua A Proximo");
        assertThat(dto.houseNumber()).isEqualTo(11);
    }
}
