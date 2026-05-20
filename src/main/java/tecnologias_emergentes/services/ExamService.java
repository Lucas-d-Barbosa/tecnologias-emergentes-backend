package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.ExamDTO;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Exam;
import tecnologias_emergentes.repositories.CustomerRepository;
import tecnologias_emergentes.repositories.ExamRepository;
import tecnologias_emergentes.repositories.ExamRepository.ExamReportProjection;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public ResponseEntity<Page<Exam>> findAll(Pageable pageable) {
        return ResponseEntity.ok(examRepository.findAll(pageable));
    }

    public ResponseEntity<Exam> findById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exame não encontrado."));
        return ResponseEntity.ok(exam);
    }

    public ResponseEntity<Exam> save(ExamDTO dto) {
        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new RuntimeException("Paciente associado não encontrado."));

        Exam exam = ExamDTO.mapperToExam(dto, customer);
        return ResponseEntity.status(201).body(examRepository.save(exam));
    }

    // Retorna o relatório nativo direto do banco (Requisito 4)
    public ResponseEntity<Page<ExamReportProjection>> getNormalExamsReport(Pageable pageable) {
        return ResponseEntity.ok(examRepository.findNormalExamsReport(pageable));
    }
}