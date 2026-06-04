package tecnologias_emergentes.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.repositories.HospitalRepository;
import tecnologias_emergentes.services.AddressService;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final AddressService addressService;
    private final HospitalRepository hospitalRepository;

    public DataSeeder(AddressService addressService, HospitalRepository hospitalRepository) {
        this.addressService = addressService;
        this.hospitalRepository = hospitalRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<AddressSeed> addressSeeds = List.of(
                new AddressSeed("Juazeiro do Norte", "Av. Padre Cícero", 2000, new BigDecimal("-7.237142"), new BigDecimal("-39.312403")),
                new AddressSeed("Juazeiro do Norte", "Rua Catulo da Paixão Cearense", 219, new BigDecimal("-7.229840"), new BigDecimal("-39.297180")),
                new AddressSeed("Juazeiro do Norte", "Av. Ailton Gomes", 2100, new BigDecimal("-7.217900"), new BigDecimal("-39.308300")),
                new AddressSeed("Juazeiro do Norte", "Av. Leão Sampaio", 1450, new BigDecimal("-7.245800"), new BigDecimal("-39.318800")),
                new AddressSeed("Juazeiro do Norte", "Rua São Pedro", 1470, new BigDecimal("-7.213600"), new BigDecimal("-39.311500"))
        );

        List<HospitalSeed> hospitalSeeds = List.of(
                new HospitalSeed("Hospital Regional do Cariri", "Público", addressSeeds.get(1)),
                new HospitalSeed("UPA 24h Limoeiro", "Urgência", addressSeeds.get(2)),
                new HospitalSeed("UPA 24h Lagoa Seca", "Urgência", addressSeeds.get(3)),
                new HospitalSeed("Hospital e Maternidade São Lucas", "Privado", addressSeeds.get(4))
        );

        addressSeeds.forEach(seed -> addressService.resolveOrCreate(seed.toDto()));

        hospitalSeeds.forEach(seed -> {
            Address address = addressService.resolveOrCreate(seed.addressSeed().toDto());

            hospitalRepository.findByCategoryNameAndCategoryTypeAndAddressId(
                            seed.categoryName(),
                            seed.categoryType(),
                            address.getId()
                    )
                    .orElseGet(() -> hospitalRepository.save(Hospital.builder()
                            .categoryName(seed.categoryName())
                            .categoryType(seed.categoryType())
                            .address(address)
                            .build()));
        });
    }

    private record AddressSeed(
            String city,
            String street,
            Integer houseNumber,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        AddressDTO toDto() {
            return new AddressDTO(latitude, longitude, city, street, houseNumber);
        }
    }

    private record HospitalSeed(
            String categoryName,
            String categoryType,
            AddressSeed addressSeed
    ) {
    }
}