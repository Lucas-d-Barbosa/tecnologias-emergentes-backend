package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.exceptions.ResourceNotFoundException;
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
        if(address.isEmpty()) throw new ResourceNotFoundException("Endereço não encontrado.");
        return ResponseEntity.ok().body(address.get());
    }

    public ResponseEntity<Address> save(AddressDTO addressDTO){
        Address address = mapperToAddress(addressDTO);
        return ResponseEntity.status(201).body(addressRepository.save(address));
    }

    public Address resolveOrCreate(AddressDTO addressDTO) {
        return addressRepository
                .findByStreetAndHouseNumberAndCity(addressDTO.street(), addressDTO.houseNumber(), addressDTO.city())
                .orElseGet(() -> addressRepository.save(mapperToAddress(addressDTO)));
    }

    public ResponseEntity<Address> update(Long id, AddressDTO addressDTO){
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado."));

        // PATCH parcial: so atualiza os campos enviados, preservando os demais.
        // Para requests com o corpo completo o resultado e identico ao anterior.
        if (addressDTO.street() != null) existingAddress.setStreet(addressDTO.street());
        if (addressDTO.city() != null) existingAddress.setCity(addressDTO.city());
        if (addressDTO.houseNumber() != null) existingAddress.setHouseNumber(addressDTO.houseNumber());
        if (addressDTO.latitude() != null) existingAddress.setLatitude(addressDTO.latitude());
        if (addressDTO.longitude() != null) existingAddress.setLongitude(addressDTO.longitude());
        return ResponseEntity.ok().body(addressRepository.save(existingAddress));
    }

    public ResponseEntity<Void> delete(Long id){
        Optional<Address> address = addressRepository.findById(id);
        if(address.isEmpty()) throw new ResourceNotFoundException("Endereço não encontrado.");
        addressRepository.delete(address.get());
        return  ResponseEntity.noContent().build();
    }
}
