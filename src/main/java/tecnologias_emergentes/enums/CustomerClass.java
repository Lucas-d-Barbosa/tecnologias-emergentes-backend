package tecnologias_emergentes.enums;

import lombok.Getter;

@Getter
public enum CustomerClass {
    STANDARD(1, "Standard"),
    PREMIUM(2, "Premium");

    private final int cod;
    private final String description;

    CustomerClass(int cod, String description) {
        this.cod = cod;
        this.description = description;
    }
}