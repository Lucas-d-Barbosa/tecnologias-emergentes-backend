package tecnologias_emergentes.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import tecnologias_emergentes.models.Address;
import java.math.BigDecimal;

public record AddressDTO(
        BigDecimal latitude,
        BigDecimal longitude,
        String city,
        String street,
    @JsonAlias({"number"})
        Integer houseNumber) {

    public static Address mapperToAddress(AddressDTO dto) {
        return Address.builder()
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .city(dto.city())
                .street(dto.street())
                .houseNumber(dto.houseNumber())
                .build();
    }

    public static AddressDTO mapperToAddressDTO(Address address) {
        return new AddressDTO(
                address.getLatitude(),
                address.getLongitude(),
                address.getCity(),
                address.getStreet(),
                address.getHouseNumber()
        );
    }
}