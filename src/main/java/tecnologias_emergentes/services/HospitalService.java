package tecnologias_emergentes.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.dtos.HospitalDTO;
import tecnologias_emergentes.exceptions.BusinessRuleException;
import tecnologias_emergentes.exceptions.ResourceNotFoundException;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.repositories.HospitalRepository;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class HospitalService {

    private static final BigDecimal NEARBY_DISTANCE_THRESHOLD = new BigDecimal("0.0100");
    private static final BigDecimal NEARBY_OFFSET = new BigDecimal("0.0010");
    private static final String AUTO_HOSPITAL_NAME = "Hospital Proximo";
    private static final String AUTO_HOSPITAL_TYPE = "Privado";

    private final HospitalRepository hospitalRepository;
    private final AddressService addressService;

    public ResponseEntity<Page<Hospital>> findAll(Pageable pageable) {
        return ResponseEntity.ok(hospitalRepository.findAll(pageable));
    }

    public ResponseEntity<Hospital> findById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital não encontrado."));
        return ResponseEntity.ok(hospital);
    }

    public Hospital findOrCreateNearestHospital(Address referenceAddress) {
        Optional<Hospital> nearest = findNearestHospital(referenceAddress);
        if (nearest.isPresent() && isWithinDistance(nearest.get().getAddress(), referenceAddress)) {
            return nearest.get();
        }

        return createNearbyHospital(referenceAddress);
    }

    public Optional<Hospital> findNearestHospital(Address referenceAddress) {
        List<Hospital> hospitals = hospitalRepository.findAll();
        if (hospitals.isEmpty()) {
            return Optional.empty();
        }

        return hospitals.stream()
            .filter(hospital -> hospital.getAddress() != null)
            .min(Comparator.comparing(hospital -> distanceMetric(hospital.getAddress(), referenceAddress)));
    }

    @Transactional
    public ResponseEntity<Hospital> save(HospitalDTO dto) {
        if (dto.address() == null) {
            throw new BusinessRuleException("Os dados de endereço são obrigatórios para cadastrar um hospital.");
        }

        Address address = addressService.resolveOrCreate(dto.address());

        Hospital hospital = HospitalDTO.mapperToHospital(dto, address);
        return ResponseEntity.status(201).body(hospitalRepository.save(hospital));
    }

    private Hospital createNearbyHospital(Address referenceAddress) {
        Address nearbyAddress = addressService.resolveOrCreate(buildNearbyAddress(referenceAddress));

        Hospital hospital = Hospital.builder()
                .categoryName(AUTO_HOSPITAL_NAME)
                .categoryType(AUTO_HOSPITAL_TYPE)
                .address(nearbyAddress)
                .build();

        return hospitalRepository.save(hospital);
    }

    private AddressDTO buildNearbyAddress(Address referenceAddress) {
        BigDecimal latitude = referenceAddress.getLatitude().add(NEARBY_OFFSET);
        BigDecimal longitude = referenceAddress.getLongitude().add(NEARBY_OFFSET);
        Integer baseNumber = referenceAddress.getHouseNumber() != null ? referenceAddress.getHouseNumber() : 0;
        String baseStreet = referenceAddress.getStreet() != null ? referenceAddress.getStreet() : "Rua";

        return new AddressDTO(
                latitude,
                longitude,
                referenceAddress.getCity(),
                baseStreet + " Proximo",
                baseNumber + 1
        );
    }

    private BigDecimal distanceMetric(Address origin, Address target) {
        BigDecimal latDiff = origin.getLatitude().subtract(target.getLatitude()).abs();
        BigDecimal lonDiff = origin.getLongitude().subtract(target.getLongitude()).abs();
        return latDiff.add(lonDiff);
    }

    private boolean isWithinDistance(Address hospitalAddress, Address referenceAddress) {
        return distanceMetric(hospitalAddress, referenceAddress)
                .compareTo(NEARBY_DISTANCE_THRESHOLD) <= 0;
    }

    public ResponseEntity<Void> delete(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital não encontrado."));
        hospitalRepository.delete(hospital);
        return ResponseEntity.noContent().build();
    }
}