package tecnologias_emergentes.dtos;

import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Customer;

public record CustomerDTO(
        String name,
        String email,
        CustomerClass customerClass,
        Address address
) {
    public static Customer mapperToCustomer(CustomerDTO customerDTO){
        return Customer
                .builder()
                .name(customerDTO.name())
                .email(customerDTO.email())
                .address(customerDTO.address)
                .customerClass(customerDTO.customerClass)
                .build();
    }

    public static CustomerDTO mapperToCustomerDTO(Customer customer){
        return new CustomerDTO(
                customer.getName(),
                customer.getEmail(),
                customer.getCustomerClass(),
                customer.getAddress()
        );
    }
}

