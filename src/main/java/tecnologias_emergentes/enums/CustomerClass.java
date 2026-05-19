package tecnologias_emergentes.enums;

import lombok.Getter;

@Getter
public enum CustomerClass {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int cod;
    private final String description;

    CustomerClass(int cod){
        this.cod = cod;
        this.description = switch (cod) {
            case 1 -> "Low";
            case 2 -> "Medium";
            case 3 -> "High";
            default -> "unknow";
        };
    }
}
