package tecnologias_emergentes.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tecnologias_emergentes.dtos.ExamDTO;
import tecnologias_emergentes.models.Exam;
import tecnologias_emergentes.services.ExamService;
import tecnologias_emergentes.repositories.ExamRepository.ExamReportProjection;

@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @GetMapping
    public ResponseEntity<Page<Exam>> findAll(@PageableDefault(size = 15) Pageable pageable) {
        return examService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> findById(@PathVariable Long id) {
        return examService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Exam> save(@RequestBody ExamDTO examDTO) {
        return examService.save(examDTO);
    }

    // Endpoint para extrair o relatório nativo direto da estrutura JSONB
    @GetMapping("/reports/normal")
    public ResponseEntity<Page<ExamReportProjection>> getNormalExamsReport(@PageableDefault(size = 15) Pageable pageable) {
        return examService.getNormalExamsReport(pageable);
    }
}