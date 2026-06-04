package tecnologias_emergentes.models.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tecnologias_emergentes.exceptions.ExternalServiceException;
import tecnologias_emergentes.models.records.ExamData;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Converter
public class ExamDataConverter implements AttributeConverter<ExamData, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ExamData attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (JacksonException e) {
            throw new ExternalServiceException("Não foi possível converter os dados do exame para JSON.", e);
        }
    }

    @Override
    public ExamData convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return null;
            }
            return objectMapper.readValue(dbData, ExamData.class);
        } catch (JacksonException e) {
            throw new ExternalServiceException("Não foi possível ler os dados do exame salvos no banco.", e);
        }
    }
}