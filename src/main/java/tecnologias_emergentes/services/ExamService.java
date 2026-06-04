package tecnologias_emergentes.services;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final CustomerRepository customerRepository;
    private final GroqAnalysisService groqAnalysisService;

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
        ExamData examData = generateRandomHemogramData();

        Exam exam = Exam.builder()
                .customer(customer)
                .type(ExamType.HEMOGRAM)
                .examData(examData)
                .isAbnormal(isHemogramAbnormal(examData))
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
            observation = groqAnalysisService.analyzeHemogram(
                    exam.getExamData(),
                    customer.getCustomerClass().getDescription()
            );
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

    public ResponseEntity<Page<ExamReportProjection>> getNormalExamsReport(Pageable pageable) {
        return ResponseEntity.ok(examRepository.findNormalExamsReport(pageable));
    }

    // Gera sempre um cenario com pelo menos um componente alterado (anormal),
    // sorteando qual eixo do hemograma estara fora da faixa de referencia.
    private ExamData generateRandomHemogramData() {
        double rbc = randomInRange(4.1, 6.0);
        double hemoglobin = randomInRange(12.0, 17.5);
        double wbc = randomInRange(4500, 11000);
        int platelets = ThreadLocalRandom.current().nextInt(150_000, 450_001);

        int riskType = ThreadLocalRandom.current().nextInt(4);
        switch (riskType) {
            case 0 -> {
                rbc = randomInRange(1.8, 3.5);
                hemoglobin = randomInRange(5.0, 10.0);
            }
            case 1 -> wbc = randomInRange(1500, 3500);
            case 2 -> wbc = randomInRange(15000, 25000);
            case 3 -> platelets = ThreadLocalRandom.current().nextInt(20_000, 80_000);
        }

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

    private static final int PLATELETS_REF_MIN = 150_000;
    private static final int PLATELETS_REF_MAX = 450_000;

    // Marca o exame como anormal se qualquer componente sair da sua faixa de referencia.
    private boolean isHemogramAbnormal(ExamData examData) {
        if (examData == null) {
            return false;
        }

        Erythrogram erythrogram = examData.erythrogram();
        Leukogram leukogram = examData.leukogram();

        boolean erythrogramAbnormal = erythrogram != null
                && (isOutsideReference(erythrogram.rbc()) || isOutsideReference(erythrogram.hemoglobin()));
        boolean leukogramAbnormal = leukogram != null && isOutsideReference(leukogram.wbc_total());

        return erythrogramAbnormal || leukogramAbnormal || isPlateletsAbnormal(examData.platelets());
    }

    // Interpreta o campo "ref" no formato "min-max" e verifica se o valor esta fora da faixa.
    private boolean isOutsideReference(ExamComponent component) {
        if (component == null || component.value() == null || component.ref() == null) {
            return false;
        }

        String[] bounds = component.ref().split("-");
        if (bounds.length != 2) {
            return false;
        }

        try {
            double min = Double.parseDouble(bounds[0].trim());
            double max = Double.parseDouble(bounds[1].trim());
            double value = component.value();
            return value < min || value > max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isPlateletsAbnormal(Platelets platelets) {
        if (platelets == null || platelets.count() == null) {
            return false;
        }

        int count = platelets.count();
        return count < PLATELETS_REF_MIN || count > PLATELETS_REF_MAX;
    }
}