package tecnologias_emergentes.service;

import org.springframework.stereotype.Service;
import tecnologias_emergentes.model.AccessClass;
import tecnologias_emergentes.model.User;

import java.util.Map;

@Service
public class AccessCalculationService {

    private final Map<String, Integer> professionScores = Map.of(
            "doctor", 40,
            "nurse", 25,
            "engineer", 30,
            "teacher", 20,
            "student", 10,
            "other", 15
    );

    private final Map<String, Integer> incomeScores = Map.of(
            ">10000", 40,
            "5000-10000", 30,
            "2000-5000", 20,
            "<2000", 5
    );

    public AccessClass calculate(User user) {
        int prof = professionScores.getOrDefault(user.getProfession().toLowerCase(), 15);
        int income = incomeScores.getOrDefault(user.getIncomeBracket(), 10);
        int postal = postalCodeScore(user.getPostalCode());

        int total = prof + income + postal; // max ~120

        if (total >= 80) return AccessClass.VIP;
        if (total < 50) return AccessClass.BASE;
        return AccessClass.STANDARD;
    }

    private int postalCodeScore(String cep) {
        if (cep == null) return 10;
        // heuristic: postal codes in wealthier areas (stub) -> last digit even
        try {
            char last = cep.trim().charAt(cep.trim().length() - 1);
            if (Character.isDigit(last)) {
                int d = Character.getNumericValue(last);
                return (d % 2 == 0) ? 40 : 10;
            }
        } catch (Exception e) {
            return 10;
        }
        return 10;
    }
}
