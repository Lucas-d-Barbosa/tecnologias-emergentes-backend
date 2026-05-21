package tecnologias_emergentes.dtos;

import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Hospital;

public record HospitalDTO(
        String categoryName,
        String categoryType,
    AddressDTO address
) {
    public static Hospital mapperToHospital(HospitalDTO dto, Address address) {
        return Hospital.builder()
                .categoryName(dto.categoryName())
                .categoryType(dto.categoryType())
                .address(address)
                .build();
    }

    public static HospitalDTO mapperToHospitalDTO(Hospital hospital){
        return new HospitalDTO(
                hospital.getCategoryName(),
                hospital.getCategoryType(),
                hospital.getAddress() != null ? AddressDTO.mapperToAddressDTO(hospital.getAddress()) : null
        );
    }
}