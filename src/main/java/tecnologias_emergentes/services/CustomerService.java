package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.CustomerDTO;
import tecnologias_emergentes.dtos.CustomerDTO.*;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.repositories.CustomerRepository;

import java.util.Optional;

import static tecnologias_emergentes.dtos.CustomerDTO.mapperToCustomer;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public ResponseEntity<Page<Customer>> findAll(Pageable pageable){
        return ResponseEntity.ok().body(customerRepository.findAll(pageable));
    }

    public ResponseEntity<Customer> findById(Long id){
        Optional<Customer> customer = customerRepository.findById(id);
        if(customer.isEmpty()) throw new RuntimeException("Customer Not Found!");
        return ResponseEntity.ok().body(customer.get());
    }

    public ResponseEntity<Customer> save(CustomerDTO customerDTO){
        Customer customer = mapperToCustomer(customerDTO);
        return ResponseEntity.status(201).body(customerRepository.save(customer));
    }

    public ResponseEntity<Customer> update(Long id, CustomerDTO customerDTO){
        Optional<Customer> customerToUpdate = customerRepository.findById(id);
        if(customerToUpdate.isEmpty()) throw new RuntimeException("Customer Not Found!");
        Customer customer = customerToUpdate.get();
        customer.setCustomerClass(customerDTO.customerClass());
        customer.setName(customerDTO.name());
        customer.setEmail(customerDTO.email());
        customer.setAddress(customerDTO.address());
        return ResponseEntity.ok().body(customerRepository.save(customer));
    }

    public ResponseEntity<Void> delete(Long id){
       Optional<Customer> customer = customerRepository.findById(id);
       if(customer.isEmpty()) throw new RuntimeException("Customer Not Found!");
       customerRepository.delete(customer.get());
       return ResponseEntity.noContent().build();
    }
}
