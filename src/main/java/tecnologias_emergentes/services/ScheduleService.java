package tecnologias_emergentes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.dtos.ScheduleDTO;
import tecnologias_emergentes.models.Customer;
import tecnologias_emergentes.models.Hospital;
import tecnologias_emergentes.models.Schedule;
import tecnologias_emergentes.repositories.CustomerRepository;
import tecnologias_emergentes.repositories.HospitalRepository;
import tecnologias_emergentes.repositories.ScheduleRepository;
import tecnologias_emergentes.repositories.ScheduleRepository.ScheduleReportProjection;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public ResponseEntity<Page<Schedule>> findAll(Pageable pageable) {
        return ResponseEntity.ok(scheduleRepository.findAll(pageable));
    }

    public ResponseEntity<Schedule> save(ScheduleDTO dto) {
        Hospital hospital = hospitalRepository.findById(dto.hospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital não encontrado."));
        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

        Schedule schedule = ScheduleDTO.mapperToSchedule(dto, hospital, customer);
        return ResponseEntity.status(201).body(scheduleRepository.save(schedule));
    }

    // Retorna o relatório nativo de agendamentos (Requisito 4)
    public ResponseEntity<Page<ScheduleReportProjection>> getSchedulesReport(Pageable pageable) {
        return ResponseEntity.ok(scheduleRepository.findSchedulesReport(pageable));
    }
}