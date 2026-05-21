package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.HospitalDTO;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.repositories.HospitalRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AddressService addressService;

    public ResponseEntity<Page<Hospital>> findAll(Pageable pageable) {
        return ResponseEntity.ok(hospitalRepository.findAll(pageable));
    }

    public ResponseEntity<Hospital> findById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital não encontrado."));
        return ResponseEntity.ok(hospital);
    }

    @Transactional
    public ResponseEntity<Hospital> save(HospitalDTO dto) {
        if (dto.address() == null) {
            throw new IllegalArgumentException("Os dados de endereço são obrigatórios.");
        }

        Address address = addressService.resolveOrCreate(dto.address());

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