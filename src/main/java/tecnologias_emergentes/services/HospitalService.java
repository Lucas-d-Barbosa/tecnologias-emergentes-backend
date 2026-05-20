package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.HospitalDTO;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.repositories.AddressRepository;
import tecnologias_emergentes.repositories.HospitalRepository;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AddressRepository addressRepository;

    public ResponseEntity<Page<Hospital>> findAll(Pageable pageable) {
        return ResponseEntity.ok(hospitalRepository.findAll(pageable));
    }

    public ResponseEntity<Hospital> findById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital não encontrado."));
        return ResponseEntity.ok(hospital);
    }

    public ResponseEntity<Hospital> save(HospitalDTO dto) {
        Address address = addressRepository.findById(dto.addressId())
                .orElseThrow(() -> new RuntimeException("Endereço associado não encontrado."));

        Hospital hospital = HospitalDTO.mapperToHospital(dto, address);
        return ResponseEntity.status(201).body(hospitalRepository.save(hospital));
    }

    public ResponseEntity<Void> delete(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital não encontrado."));
        hospitalRepository.delete(hospital);
        return ResponseEntity.noContent().build();
    }
}