package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.dtos.CustomerDTO;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.repositories.AddressRepository;
import tecnologias_emergentes.repositories.CustomerRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    public ResponseEntity<Page<Customer>> findAll(Pageable page) {
        return ResponseEntity.ok(customerRepository.findAll(page));
    }

    public ResponseEntity<Customer> findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Not Found"));
        return ResponseEntity.ok(customer);
    }

    @Transactional
    public ResponseEntity<Customer> save(CustomerDTO customerDTO) {
        if (customerDTO.address() == null) {
            throw new IllegalArgumentException("Os dados de endereço são obrigatórios.");
        }

        AddressDTO addrDto = customerDTO.address();

        Address address = addressRepository
                .findByStreetAndHouseNumberAndCity(addrDto.street(), addrDto.houseNumber(), addrDto.city())
                .orElseGet(() -> {
                    Address newAddress = AddressDTO.mapperToAddress(addrDto);
                    return addressRepository.save(newAddress);
                });

        Customer customer = CustomerDTO.mapperToCustomer(customerDTO, address);

        return ResponseEntity.status(201).body(customerRepository.save(customer));
    }

    @Transactional
    public ResponseEntity<Customer> update(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Not Found"));

        if (customerDTO.address() != null) {
            AddressDTO addrDto = customerDTO.address();
            
            Address address = addressRepository
                    .findByStreetAndHouseNumberAndCity(addrDto.street(), addrDto.houseNumber(), addrDto.city())
                    .orElseGet(() -> addressRepository.save(AddressDTO.mapperToAddress(addrDto)));
            
            existingCustomer.setAddress(address);
        }

        existingCustomer.setName(customerDTO.name());
        existingCustomer.setEmail(customerDTO.email());
        existingCustomer.setCustomerClass(customerDTO.customerClass());

        return ResponseEntity.ok(customerRepository.save(existingCustomer));
    }

    public ResponseEntity<Void> delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Not Found"));
        customerRepository.delete(customer);
        return ResponseEntity.noContent().build();
    }
}