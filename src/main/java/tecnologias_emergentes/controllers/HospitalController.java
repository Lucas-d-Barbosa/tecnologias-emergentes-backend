package tecnologias_emergentes.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tecnologias_emergentes.dtos.HospitalDTO;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.services.HospitalService;

@RestController
@RequestMapping("/hospital")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    public ResponseEntity<Page<Hospital>> findAll(@PageableDefault(size = 15) Pageable pageable) {
        return hospitalService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> findById(@PathVariable Long id) {
        return hospitalService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Hospital> save(@Valid @RequestBody HospitalDTO hospitalDTO) {
        return hospitalService.save(hospitalDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return hospitalService.delete(id);
    }
}