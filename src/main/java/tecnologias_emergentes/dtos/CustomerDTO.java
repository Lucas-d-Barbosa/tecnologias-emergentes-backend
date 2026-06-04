package tecnologias_emergentes.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.models.Customer;

public record CustomerDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        String email,
        @NotNull(message = "A classe do cliente é obrigatória.")
        CustomerClass customerClass,
        @NotNull(message = "Os dados de endereço são obrigatórios.")
        AddressDTO address
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
                customer.getAddress() != null ? AddressDTO.mapperToAddressDTO(customer.getAddress()) : null
        );
    }
}