package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.ExamDTO;
import tecnologias_emergentes.dtos.HemogramResponseDTO;
import tecnologias_emergentes.exceptions.ResourceNotFoundException;
import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.enums.ExamType;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Exam;
import tecnologias_emergentes.models.records.ExamComponent;
import tecnologias_emergentes.models.records.ExamData;
import tecnologias_emergentes.models.records.Erythrogram;
import tecnologias_emergentes.models.records.Leukogram;
import tecnologias_emergentes.models.records.Platelets;
import tecnologias_emergentes.repositories.CustomerRepository;
import tecnologias_emergentes.repositories.ExamRepository;
import tecnologias_emergentes.repositories.ExamRepository.ExamReportProjection;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private GroqAnalysisService groqAnalysisService;

    public ResponseEntity<Page<Exam>> findAll(Pageable pageable) {
        return ResponseEntity.ok(examRepository.findAll(pageable));
    }

    public ResponseEntity<Exam> findById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame não encontrado."));
        return ResponseEntity.ok(exam);
    }

    public ResponseEntity<Exam> save(ExamDTO dto) {
        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente associado não encontrado para gerar o exame."));

        Exam exam = ExamDTO.mapperToExam(dto, customer);
        return ResponseEntity.status(201).body(examRepository.save(exam));
    }

    public Exam createAutomaticHemogram(Customer customer) {
        Exam exam = Exam.builder()
                .customer(customer)
                .type(ExamType.HEMOGRAM)
                .examData(generateRandomHemogramData())
                .isAbnormal(false)
                .build();

        return examRepository.save(exam);
    }

    public ResponseEntity<HemogramResponseDTO> getCustomerHemogram(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        Exam exam = examRepository.findFirstByCustomer_IdAndTypeOrderByOrderDateDesc(customerId, ExamType.HEMOGRAM)
            .orElseThrow(() -> new ResourceNotFoundException("Hemograma não encontrado para este cliente."));

        String observation = null;
        if (customer.getCustomerClass() == CustomerClass.PREMIUM) {
            observation = groqAnalysisService.analyzeHemogram(exam.getExamData());
        }

        HemogramResponseDTO response = new HemogramResponseDTO(
                exam.getId(),
                customer.getId(),
                customer.getCustomerClass(),
                exam.getExamData(),
                observation
        );

        return ResponseEntity.ok(response);
    }

    // Retorna o relatório nativo direto do banco (Requisito 4)
    public ResponseEntity<Page<ExamReportProjection>> getNormalExamsReport(Pageable pageable) {
        return ResponseEntity.ok(examRepository.findNormalExamsReport(pageable));
    }

    private ExamData generateRandomHemogramData() {
        double rbc = randomInRange(4.1, 6.0);
        double hemoglobin = randomInRange(12.0, 17.5);
        double wbc = randomInRange(4500, 11000);
        int platelets = ThreadLocalRandom.current().nextInt(150_000, 450_001);

        return new ExamData(
                new Erythrogram(
                        new ExamComponent(rbc, "10^6/µL", "4.1-6.0"),
                        new ExamComponent(hemoglobin, "g/dL", "12.0-17.5")
                ),
                new Leukogram(
                        new ExamComponent(wbc, "/µL", "4500-11000")
                ),
                new Platelets(platelets)
        );
    }

    private double randomInRange(double min, double max) {
        return Math.round((ThreadLocalRandom.current().nextDouble(min, max) * 10.0)) / 10.0;
    }
}