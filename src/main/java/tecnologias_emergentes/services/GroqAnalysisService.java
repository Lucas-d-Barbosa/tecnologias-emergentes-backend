package tecnologias_emergentes.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.models.records.ExamData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GroqAnalysisService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${groq.base-url:https://api.groq.com/openai/v1/chat/completions}")
    private String baseUrl;

    public GroqAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String analyzeHemogram(ExamData examData) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Análise premium indisponível: configure a variável GROQ_API_KEY para habilitar o laudo do Groq.";
        }

        try {
            String prompt = "Você é um médico que avalia hemogramas. Analise de forma objetiva e concisa este JSON e entregue a observação clínica em português, citando possíveis pontos de atenção, parabenizando quando o paciente tiver um bom resultado de saúde e dando dicas caso tenha algum risco de passar mal ou de vida, inclusive indicando quais seriam os valores normais e seguros: "
                    + objectMapper.writeValueAsString(examData);

            String requestBody = objectMapper.writeValueAsString(new GroqChatRequest(
                    model,
                    new GroqMessage[]{
                            new GroqMessage("system", "Você é um médico especialista em hemogramas. Seu parecer será lido diretamente pelo paciente, portanto elabore o texto como uma conversa direta com o paciete. Seja objetivo e sucinto. Tenha um tom amigável mas cordial e educado, como um médico bem atencioso e empático. Você não deve responder como markdown, mas como um texto formatado normalmente. Evite caracteres especiais. Trate o cliente como alguém instruído, mas que não tem conhecimento médico. Seja claro e didático, mas sem ser prolixo. Se o exame tiver resultados excelentes, parabenize o paciente. Se tiver algum resultado preocupante, explique de forma clara o que pode estar acontecendo e quais são os valores normais para aquela métrica, dando dicas de como melhorar a saúde caso haja algum risco."),
                            new GroqMessage("user", prompt)
                    },
                    0.2
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Erro ao consultar Groq: HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new IllegalStateException("Groq retornou resposta vazia.");
            }

            return content.asText().trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Análise premium indisponível no momento devido a interrupção da requisição ao Groq.";
        } catch (IOException | RuntimeException e) {
            return "Análise premium indisponível no momento. O exame foi retornado normalmente, mas sem avaliação do Groq.";
        }
    }

    private record GroqChatRequest(String model, GroqMessage[] messages, double temperature) {
    }

    private record GroqMessage(String role, String content) {
    }
}