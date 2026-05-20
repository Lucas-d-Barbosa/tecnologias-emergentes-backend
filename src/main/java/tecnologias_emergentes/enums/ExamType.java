package tecnologias_emergentes.enums;

import lombok.Getter;

@Getter
public enum ExamType {
    HEMOGRAM(1, "Hemogram"),
    BIOCHEMICAL(2, "Biochemical"),
    IMAGING(3, "Imaging");

    private final int cod;
    private final String description;

    ExamType(int cod, String description) {
        this.cod = cod;
        this.description = description;
    }
}