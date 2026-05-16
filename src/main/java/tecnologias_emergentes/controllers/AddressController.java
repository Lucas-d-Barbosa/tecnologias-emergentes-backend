package tecnologias_emergentes.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tecnologias_emergentes.dtos.AddressDTO;
import tecnologias_emergentes.models.Address;
import tecnologias_emergentes.services.AddressService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
@RestController
@RequestMapping("address")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<Page<Address>> findAll(@PageableDefault(size = 10) Pageable pageable){
        return addressService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> findById(@PathVariable Long id) {
        return addressService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Address> save(@RequestBody AddressDTO addressDTO){
        return addressService.save(addressDTO);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Address> update(@PathVariable Long id, @RequestBody AddressDTO addressDTO){
        return addressService.update(id, addressDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        return addressService.delete(id);
    }

}
