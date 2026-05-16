package tecnologias_emergentes.dtos;

import org.springframework.data.geo.Point;
import tecnologias_emergentes.models.Address;

public record AddressDTO(
        Point latitude,
        Point longitude,
        String city,
        String street,
        Integer houseNumber)
{

    public static Address mapperToAddress(AddressDTO dto){
        return Address.builder()
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .city(dto.city())
                .street(dto.street())
                .houseNumber(dto.houseNumber())
                .build();
    }

    public static AddressDTO mapperToAddressDTO(Address address){
        return new AddressDTO(
                address.getLatitude(),
                address.getLongitude(),
                address.getCity(),
                address.getStreet(),
                address.getHouseNumber()
        );
    }

}
