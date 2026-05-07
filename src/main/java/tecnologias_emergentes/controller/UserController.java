package tecnologias_emergentes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tecnologias_emergentes.model.User;
import tecnologias_emergentes.repository.UserRepository;
import tecnologias_emergentes.service.AccessCalculationService;
import tecnologias_emergentes.service.AiIntegrationService;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final AccessCalculationService calcService;
    private final AiIntegrationService aiService;

    public UserController(UserRepository userRepository, AccessCalculationService calcService, AiIntegrationService aiService) {
        this.userRepository = userRepository;
        this.calcService = calcService;
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // compute initial access class
        user.setAccessClass(calcService.calculate(user));
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/access")
    public ResponseEntity<?> getAccess(@PathVariable Long id) {
        Optional<User> u = userRepository.findById(id);
        if (u.isEmpty()) return ResponseEntity.notFound().build();
        User user = u.get();
        user.setAccessClass(calcService.calculate(user));
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/diagnose")
    public ResponseEntity<?> diagnose(@PathVariable Long id, @RequestBody String examData) {
        Optional<User> u = userRepository.findById(id);
        if (u.isEmpty()) return ResponseEntity.notFound().build();
        User user = u.get();
        user.setAccessClass(calcService.calculate(user));
        return ResponseEntity.ok(aiService.requestDiagnostic(user, examData));
    }
}
