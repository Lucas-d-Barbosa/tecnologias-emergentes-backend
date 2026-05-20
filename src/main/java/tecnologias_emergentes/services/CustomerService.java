package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.CustomerDTO;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.repositories.AddressRepository;
import tecnologias_emergentes.repositories.CustomerRepository;

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

    public ResponseEntity<Customer> save(CustomerDTO customerDTO) {
        // 1. Valida se o ID do endereço foi enviado no JSON
        if (customerDTO.addressId() == null) {
            throw new IllegalArgumentException("O campo 'addressId' é obrigatório.");
        }

        // 2. Busca o endereço correspondente no banco de dados
        Address address = addressRepository.findById(customerDTO.addressId())
                .orElseThrow(() -> new RuntimeException("Address Not Found to associate with customer"));

        // CORREÇÃO AQUI: Passamos o customerDTO E o address (os dois parâmetros exigidos)
        Customer customer = CustomerDTO.mapperToCustomer(customerDTO, address);

        return ResponseEntity.status(201).body(customerRepository.save(customer));
    }

    public ResponseEntity<Customer> update(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer Not Found"));

        if (customerDTO.addressId() != null) {
            Address address = addressRepository.findById(customerDTO.addressId())
                    .orElseThrow(() -> new RuntimeException("Address Not Found"));
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