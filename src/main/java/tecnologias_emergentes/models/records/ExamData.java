package tecnologias_emergentes.models.records;

public record ExamData(
        Erythrogram erythrogram,
        Leukogram leukogram,
        Platelets platelets
) {}

record ExamComponent(Double value, String unit, String ref) {}

record Erythrogram(ExamComponent rbc, ExamComponent hemoglobin) {}

record Leukogram(ExamComponent wbc_total) {}

record Platelets(Integer count) {}