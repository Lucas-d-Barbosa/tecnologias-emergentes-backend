package tecnologias_emergentes.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tecnologias_emergentes.dtos.AutoScheduleResponseDTO;
import tecnologias_emergentes.dtos.ScheduleDTO;
import tecnologias_emergentes.models.Schedule;
import tecnologias_emergentes.services.ScheduleService;
import tecnologias_emergentes.repositories.ScheduleRepository.ScheduleReportProjection;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<Page<Schedule>> findAll(@PageableDefault(size = 15) Pageable pageable) {
        return scheduleService.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<Schedule> save(@RequestBody ScheduleDTO scheduleDTO) {
        return scheduleService.save(scheduleDTO);
    }

    // Endpoint para retornar o relatório unificado de agendamentos ordenados por data
    @GetMapping("/reports")
    public ResponseEntity<Page<ScheduleReportProjection>> getSchedulesReport(@PageableDefault(size = 15) Pageable pageable) {
        return scheduleService.getSchedulesReport(pageable);
    }

    @GetMapping("/customer/{customerId}/latest")
    public ResponseEntity<AutoScheduleResponseDTO> getLatestScheduleForCustomer(@PathVariable Long customerId) {
        return scheduleService.getLatestScheduleForCustomer(customerId);
    }
}