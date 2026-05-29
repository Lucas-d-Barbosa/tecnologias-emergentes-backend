package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tecnologias_emergentes.dtos.AutoScheduleResponseDTO;
import tecnologias_emergentes.dtos.ScheduleDTO;
import tecnologias_emergentes.enums.CustomerClass;
import tecnologias_emergentes.exceptions.ResourceNotFoundException;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.models.Schedule;
import tecnologias_emergentes.repositories.CustomerRepository;
import tecnologias_emergentes.repositories.HospitalRepository;
import tecnologias_emergentes.repositories.ScheduleRepository;
import tecnologias_emergentes.repositories.ScheduleRepository.ScheduleReportProjection;
import java.time.OffsetDateTime;

@Service
public class ScheduleService {

    private static final long DEFAULT_SERVICE_CODE = 1001L;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private CustomerRepository customerRepository;

    public ResponseEntity<Page<Schedule>> findAll(Pageable pageable) {
        return ResponseEntity.ok(scheduleRepository.findAll(pageable));
    }

    public Schedule createAutomaticScheduleForCustomer(Customer customer) {
        if (customer.getCustomerClass() != CustomerClass.PREMIUM) {
            return null;
        }

        Hospital hospital = hospitalService.findOrCreateNearestHospital(customer.getAddress());

        Schedule schedule = Schedule.builder()
                .serviceCode(DEFAULT_SERVICE_CODE)
                .hospital(hospital)
                .customer(customer)
                .scheduledAt(OffsetDateTime.now().plusDays(1))
                .build();

        return scheduleRepository.save(schedule);
    }

    public ResponseEntity<Schedule> save(ScheduleDTO dto) {
        Hospital hospital = hospitalRepository.findById(dto.hospitalId())
            .orElseThrow(() -> new ResourceNotFoundException("Hospital não encontrado."));
        Customer customer = customerRepository.findById(dto.customerId())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        Schedule schedule = ScheduleDTO.mapperToSchedule(dto, hospital, customer);
        return ResponseEntity.status(201).body(scheduleRepository.save(schedule));
    }

    // Retorna o relatório nativo de agendamentos (Requisito 4)
    public ResponseEntity<Page<ScheduleReportProjection>> getSchedulesReport(Pageable pageable) {
        return ResponseEntity.ok(scheduleRepository.findSchedulesReport(pageable));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<AutoScheduleResponseDTO> getLatestScheduleForCustomer(Long customerId) {
        Schedule schedule = scheduleRepository.findFirstByCustomer_IdOrderByScheduledAtDesc(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado para este cliente."));

        return ResponseEntity.ok(AutoScheduleResponseDTO.fromSchedule(schedule));
    }
}