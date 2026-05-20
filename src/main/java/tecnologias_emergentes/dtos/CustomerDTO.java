package tecnologias_emergentes.dtos;

import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Customer;

public record CustomerDTO(
        String name,
        String email,
        CustomerClass customerClass,
        Long addressId // Certifique-se de que está escrito exatamente assim
) {
    public static Customer mapperToCustomer(CustomerDTO customerDTO, Address address){
        return Customer.builder()
                .name(customerDTO.name())
                .email(customerDTO.email())
                .address(address) // Vincula o objeto Address completo que buscamos no Service
                .customerClass(customerDTO.customerClass()) // Corrigido de .customerClass para .customerClass()
                .build();
    }

    public static CustomerDTO mapperToCustomerDTO(Customer customer){
        return new CustomerDTO(
                customer.getName(),
                customer.getEmail(),
                customer.getCustomerClass(),
                customer.getAddress() != null ? customer.getAddress().getId() : null
        );
    }
}