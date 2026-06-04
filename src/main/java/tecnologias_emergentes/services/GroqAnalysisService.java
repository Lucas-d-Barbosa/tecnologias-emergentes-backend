package tecnologias_emergentes.services;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import tecnologias_emergentes.models.records.ExamData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String analyzeHemogram(ExamData examData, String perfilPaciente) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Análise premium indisponível: configure a variável GROQ_API_KEY para habilitar o laudo do Groq.";
        }

        try {
            String diretrizAtendimento = "premium".equalsIgnoreCase(perfilPaciente)
                    ? "O paciente é um cliente VIP de altíssimo poder aquisitivo (plano Premium). Ofereça um atendimento estilo 'concierge médico'. Recomende acompanhamento em clínicas particulares de luxo, exames preventivos de alta tecnologia, nutricionistas renomados e suplementação de ponta importada. O tom deve ser extremamente solícito, exclusivo, deferente e focado em otimização de performance e longevidade."
                    : "O paciente possui o plano básico e baixo poder aquisitivo. O atendimento deve ser estritamente focado no essencial, resolvendo o problema de forma pragmática e mais rápida. Para melhoria de saúde, limite-se a sugerir mudanças gratuitas (como caminhadas na rua e beber água) e alimentação acessível (itens básicos de feira e mercado). Se houver riscos, oriente o paciente a buscar a Unidade Básica de Saúde (UBS) do seu bairro ou um clínico geral do SUS para acompanhamento.";

            String systemPrompt = """
                    Você é um médico hematologista analisando um hemograma. Seu parecer será lido diretamente pelo paciente.
                    
                    DIRETRIZ OBRIGATÓRIA DE ATENDIMENTO:
                    %s
                    
                    REGRAS GERAIS DE COMUNICAÇÃO:
                    1. Escreva em formato de texto normal. É estritamente proibido usar markdown (não use asteriscos, negritos ou hashtags). Evite caracteres especiais.
                    2. Trate o paciente como alguém instruído, mas que não tem conhecimento médico. Não use jargões sem explicá-los de forma didática.
                    3. Se os resultados forem excelentes, parabenize o paciente.
                    4. Se houver resultados preocupantes, explique claramente o que pode estar acontecendo, quais são os valores normais para aquela métrica e explique as consequências da possível doença.
                    5. Forneça dicas de saúde que estejam rigorosamente alinhadas com a 'DIRETRIZ OBRIGATÓRIA DE ATENDIMENTO' descrita acima.
                    """.formatted(diretrizAtendimento);

            String userPrompt = "Analise de forma objetiva e concisa este JSON e entregue a observação clínica em português, citando possíveis pontos de atenção: "
                    + objectMapper.writeValueAsString(examData);

            String requestBody = objectMapper.writeValueAsString(new GroqChatRequest(
                    model,
                    new GroqMessage[]{
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", userPrompt)
                    },
                    0.2 
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(30))
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

    private record GroqChatRequest(String model, GroqMessage[] messages, double temperature) {}
    private record GroqMessage(String role, String content) {}
}