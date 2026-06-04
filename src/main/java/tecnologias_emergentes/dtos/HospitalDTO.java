package tecnologias_emergentes.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Hospital;

public record HospitalDTO(
        @NotBlank(message = "O nome da categoria é obrigatório.")
        String categoryName,
        @NotBlank(message = "O tipo da categoria é obrigatório.")
        String categoryType,
        @NotNull(message = "Os dados de endereço são obrigatórios.")
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