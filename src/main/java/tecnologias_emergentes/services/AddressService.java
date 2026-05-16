package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.dtos.AddressDTO.*;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.repositories.AddressRepository;
import java.util.Optional;
import static tecnologias_emergentes.dtos.AddressDTO.mapperToAddress;
@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    public ResponseEntity<Page<Address>> findAll(Pageable page){
        Page<Address> response = addressRepository.findAll(page);
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<Address> findById(Long id){
        Optional<Address> address = addressRepository.findById(id);
        if(address.isEmpty()) throw new RuntimeException("Address Not Found");
        return ResponseEntity.ok().body(address.get());
    }

    public ResponseEntity<Address> save(AddressDTO addressDTO){
        Address address = mapperToAddress(addressDTO);
        return ResponseEntity.status(201).body(addressRepository.save(address));
    }

    public ResponseEntity<Address> update(Long id, AddressDTO address){
        Optional<Address> addressToUpdate = addressRepository.findById(id);
        if(addressToUpdate.isEmpty()) throw new RuntimeException("Address Not Found");
        Address existingAddress = addressToUpdate.get();
        existingAddress.setStreet(address.street());
        existingAddress.setCity(address.city());
        existingAddress.setHouseNumber(address.houseNumber());
        existingAddress.setLatitude(address.latitude());
        existingAddress.setLongitude(address.longitude());
        return ResponseEntity.ok().body(addressRepository.save(existingAddress));
    }

    public ResponseEntity<Void> delete(Long id){
        Optional<Address> address = addressRepository.findById(id);
        if(address.isEmpty()) throw new RuntimeException("Address Not Found");
        addressRepository.delete(address.get());
        return  ResponseEntity.noContent().build();
    }
}
