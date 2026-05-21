package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.dtos.CustomerDTO;
import tecnologias_emergentes.exceptions.BusinessRuleException;
import tecnologias_emergentes.exceptions.ResourceNotFoundException;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.repositories.CustomerRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ExamService examService;

    public ResponseEntity<Page<Customer>> findAll(Pageable page) {
        return ResponseEntity.ok(customerRepository.findAll(page));
    }

    public ResponseEntity<Customer> findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
        return ResponseEntity.ok(customer);
    }

    @Transactional
    public ResponseEntity<Customer> save(CustomerDTO customerDTO) {
        if (customerDTO.address() == null) {
            throw new BusinessRuleException("Os dados de endereço são obrigatórios para cadastrar um cliente.");
        }

        AddressDTO addrDto = customerDTO.address();
        Address address = addressService.resolveOrCreate(addrDto);

        Customer customer = CustomerDTO.mapperToCustomer(customerDTO, address);

        Customer savedCustomer = customerRepository.save(customer);
        examService.createAutomaticHemogram(savedCustomer);

        return ResponseEntity.status(201).body(savedCustomer);
    }

    @Transactional
    public ResponseEntity<Customer> update(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        if (customerDTO.address() != null) {
            AddressDTO addrDto = customerDTO.address();

            Address address = addressService.resolveOrCreate(addrDto);
            
            existingCustomer.setAddress(address);
        }

        existingCustomer.setName(customerDTO.name());
        existingCustomer.setEmail(customerDTO.email());
        existingCustomer.setCustomerClass(customerDTO.customerClass());

        return ResponseEntity.ok(customerRepository.save(existingCustomer));
    }

    public ResponseEntity<Void> delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
        customerRepository.delete(customer);
        return ResponseEntity.noContent().build();
    }
}