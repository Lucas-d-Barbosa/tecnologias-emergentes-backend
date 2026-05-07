package tecnologias_emergentes.service;

import org.springframework.stereotype.Service;
import tecnologias_emergentes.model.AccessClass;
import tecnologias_emergentes.model.User;

import java.time.Instant;
import java.util.Map;

@Service
public class AiIntegrationService {

    // Stubbed AI integration that simulates priority and response behavior
    public Map<String, Object> requestDiagnostic(User user, String examData) {
        AccessClass c = user.getAccessClass();
        if (c == AccessClass.VIP) {
            return Map.of(
                    "status", "ok",
                    "diagnosis", "ANALYSIS_INSTANT",
                    "details", "Diagnóstico instantâneo via IA (VIP)",
                    "timestamp", Instant.now().toString()
            );
        } else if (c == AccessClass.BASE) {
            return Map.of(
                    "status", "queued",
                    "diagnosis", null,
                    "details", "Acesso limitado: apenas dados brutos e fila (BASE)",
                    "timestamp", Instant.now().toString()
            );
        } else {
            return Map.of(
                    "status", "delayed",
                    "diagnosis", "ANALYSIS_DELAYED",
                    "details", "Análise padrão com prioridade média",
                    "timestamp", Instant.now().toString()
            );
        }
    }
}
