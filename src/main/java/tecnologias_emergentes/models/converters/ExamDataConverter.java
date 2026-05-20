package tecnologias_emergentes.models.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tecnologias_emergentes.models.records.ExamData;

@Converter
public class ExamDataConverter implements AttributeConverter<ExamData, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ExamData attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao converter ExamData para String JSON", e);
        }
    }

    @Override
    public ExamData convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return null;
            }
            return objectMapper.readValue(dbData, ExamData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao ler String JSON para ExamData", e);
        }
    }
}