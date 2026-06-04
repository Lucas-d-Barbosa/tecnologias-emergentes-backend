# Documentação — Backend Tecnologias Emergentes

Sistema de gestão de exames laboratoriais e agendamentos hospitalares, com análise
clínica assistida por IA (Groq) para clientes premium. Projeto acadêmico (P.I -
Tecnologias Emergentes).

---

## 1. Visão geral

O backend é uma API REST em **Spring Boot 4.0.6 / Java 21** que modela um pequeno
ecossistema de saúde: clientes (pacientes), seus endereços, hospitais, exames
(hemogramas) e agendamentos de atendimento. O diferencial técnico do projeto está em
três pontos:

1. **Armazenamento semiestruturado**: os resultados de exame são persistidos como
   `JSONB` no PostgreSQL e mapeados para *records* imutáveis do Java 21.
2. **Automação por classe de cliente**: ao cadastrar um cliente, o sistema gera
   automaticamente um hemograma; se o cliente for `PREMIUM`, também cria um
   agendamento no hospital mais próximo.
3. **Laudo clínico por IA**: para clientes `PREMIUM`, o hemograma é enviado à API da
   **Groq** (modelo LLaMA 3.3 70B) que gera uma observação médica em linguagem natural.

### O que o sistema FAZ

- CRUD de endereços, clientes e hospitais (hospital sem update).
- Cadastro de exames manuais e geração automática de hemogramas.
- Geração automática de agendamento para clientes `PREMIUM` no hospital mais próximo.
- Reaproveitamento de endereços (dedup por `street + houseNumber + city`).
- Cálculo do hospital "mais próximo" por distância geográfica simplificada.
- Análise de hemograma via IA (Groq) com tom de atendimento diferenciado por classe.
- Relatórios nativos (SQL puro) de exames normais e de agendamentos.
- Tratamento global de erros com payload padronizado.
- Seed inicial de endereços e hospitais de Juazeiro do Norte na subida da aplicação.

### O que o sistema NÃO FAZ

- **Não tem autenticação/autorização** — todas as rotas são públicas.
- **Não tem validação de entrada** (Bean Validation) — DTOs não usam `@NotNull`,
  `@Email` etc. (a dependência `spring-boot-starter-validation` não está no projeto).
- **Não há update de exame nem de hospital**, e exames/agendamentos não têm
  endpoint de `DELETE`.
- **Não persiste a observação da IA** — o laudo do Groq é gerado on-demand a cada
  chamada do endpoint de hemograma e não é gravado no banco.
- O hemograma automático **não persiste a observação da IA** e não recebe `update`.
- **A "distância" entre endereços não é geográfica real** — é a soma das diferenças
  absolutas de latitude/longitude (distância de Manhattan em graus), sem fórmula de
  Haversine.
- Não há migrations versionadas (Flyway/Liquibase); o schema é gerado pelo Hibernate
  (`ddl-auto: update`).
- Não há paginação/sort customizados além do padrão do Spring Data.
- Não há testes para os controllers nem para a integração com o Groq.

---

## 2. Stack e dependências

| Camada            | Tecnologia                                              |
|-------------------|---------------------------------------------------------|
| Linguagem         | Java 21                                                 |
| Framework         | Spring Boot 4.0.6 (Web, Data JPA)                        |
| Banco             | PostgreSQL (produção/dev), H2 (testes)                  |
| ORM               | Hibernate / JPA                                          |
| Serialização JSON | Jackson (databind/core 2.17.0)                           |
| Boilerplate       | Lombok                                                   |
| IA externa        | Groq API (`llama-3.3-70b-versatile`) via `java.net.http`|
| Build             | Maven (wrapper `mvnw` incluso)                           |

---

## 3. Arquitetura

Arquitetura em camadas clássica do Spring, com fluxo unidirecional
`Controller → Service → Repository → Banco`:

```
HTTP
 │
 ▼
Controller   ── recebe DTO, delega ao Service, devolve ResponseEntity
 │
 ▼
Service      ── regras de negócio, orquestração entre entidades, chamadas externas
 │
 ▼
Repository   ── Spring Data JPA (queries derivadas + SQL nativo para relatórios)
 │
 ▼
PostgreSQL   ── tabelas relacionais + coluna JSONB (exam_data)
```

### Pacotes (`src/main/java/tecnologias_emergentes`)

| Pacote          | Responsabilidade                                                          |
|-----------------|---------------------------------------------------------------------------|
| `controllers`   | Endpoints REST. Apenas roteamento; nenhuma regra de negócio.              |
| `services`      | Lógica de negócio e orquestração. Devolvem `ResponseEntity` direto.       |
| `repositories`  | Interfaces Spring Data JPA + projeções de relatório.                      |
| `models`        | Entidades JPA (`Address`, `Customer`, `Exam`, `Hospital`, `Schedule`).    |
| `models.records`| Records imutáveis que descrevem a estrutura do hemograma (JSONB).         |
| `models.converters` | `ExamDataConverter`: serializa/deserializa `ExamData` ↔ JSON.         |
| `dtos`          | Records de entrada/saída + métodos estáticos de mapeamento.               |
| `enums`         | `CustomerClass` (STANDARD/PREMIUM), `ExamType` (HEMOGRAM/BIOCHEMICAL/...).  |
| `exceptions`    | Exceções de domínio + `GlobalExceptionHandler` (`@RestControllerAdvice`). |
| `config`        | CORS, seed de dados, bean do Jackson `ObjectMapper`.                       |

### Observações de design

- **Services retornam `ResponseEntity`**: a camada de serviço já decide o status HTTP
  (201, 200, 204). Isso simplifica os controllers, mas mistura responsabilidades de
  transporte com negócio — um ponto de atenção arquitetural.
- **Mapeamento via métodos estáticos nos DTOs** (`mapperToCustomer`, etc.) em vez de
  uma biblioteca (MapStruct/ModelMapper).
- **Records do Java 21** para o payload JSONB, garantindo imutabilidade dos dados de
  exame.
- **Duas instâncias de `ObjectMapper`**: uma como bean (`JacksonConfig`, usada pelo
  `GroqAnalysisService`) e outra criada manualmente dentro do `ExamDataConverter`
  (pois conversores JPA não são gerenciados pelo Spring).

---

## 4. Modelo de dados

```
Address 1 ──< Customer 1 ──< Exam        (exam_data: JSONB)
   │              │
   │              └──< Schedule >── Hospital >── Address
   └──< Hospital
```

| Entidade   | Tabela     | Campos-chave                                                              |
|------------|------------|---------------------------------------------------------------------------|
| `Address`  | `address`  | `latitude`/`longitude` (DECIMAL 9,6), `city`, `street`, `number`. Único por `(street, number, city)`. |
| `Customer` | `customer` | `name`, `email` (único), `address_id` (FK), `class` (enum STRING).        |
| `Hospital` | `hospital` | `category_name`, `category_type`, `address_id` (FK).                      |
| `Exam`     | `exam`     | `customer_id` (FK), `type` (enum), `order_date`, `exam_data` (JSONB), `is_abnormal`. |
| `Schedule` | `schedule` | `service_code`, `hospital_id` (FK), `customer_id` (FK), `scheduled_at`.   |

### Estrutura do `exam_data` (JSONB)

Mapeada pelos records em `models.records`:

```json
{
  "erythrogram": {
    "rbc":        { "value": 4.8,   "unit": "10^6/µL", "ref": "4.1-6.0" },
    "hemoglobin": { "value": 14.2,  "unit": "g/dL",    "ref": "12.0-17.5" }
  },
  "leukogram": {
    "wbc_total":  { "value": 7200,  "unit": "/µL",     "ref": "4500-11000" }
  },
  "platelets":    { "count": 230000 }
}
```

O DDL de referência (incluindo índice GIN sobre `exam_data` e índices das FKs) está em
`guides/sql_entity.md`. Na prática o schema é gerado pelo Hibernate em runtime.

---

## 5. Rotas da API

Base local: `http://localhost:8080`. Todas as listagens são paginadas (Spring Data
`Pageable`: `?page=&size=&sort=`).

### Address — `/address`
| Método | Rota             | Descrição                                  | Status |
|--------|------------------|--------------------------------------------|--------|
| GET    | `/address`       | Lista endereços (size padrão 10).          | 200    |
| GET    | `/address/{id}`  | Busca por id.                              | 200/404|
| POST   | `/address`       | Cria endereço.                             | 201    |
| PATCH  | `/address/{id}`  | Atualiza endereço.                         | 200/404|
| DELETE | `/address/{id}`  | Remove endereço.                           | 204/404|

### Customer — `/customer`
| Método | Rota              | Descrição                                                | Status |
|--------|-------------------|----------------------------------------------------------|--------|
| GET    | `/customer`       | Lista clientes (size padrão 15).                         | 200    |
| GET    | `/customer/{id}`  | Busca por id.                                            | 200/404|
| POST   | `/customer`       | Cria cliente + hemograma automático (+ agendamento se PREMIUM). | 201 |
| PUT    | `/customer/{id}`  | Atualiza cliente.                                        | 200/404|
| DELETE | `/customer/{id}`  | Remove cliente.                                          | 204/404|

O `POST /customer` é o endpoint mais importante — ver lógica detalhada na seção 6.

### Hospital — `/hospital`
| Método | Rota              | Descrição                          | Status |
|--------|-------------------|------------------------------------|--------|
| GET    | `/hospital`       | Lista hospitais (size 15).         | 200    |
| GET    | `/hospital/{id}`  | Busca por id.                      | 200/404|
| POST   | `/hospital`       | Cria hospital (reaproveita endereço).| 201  |
| DELETE | `/hospital/{id}`  | Remove hospital.                   | 204/404|

> Não há `PUT/PATCH` de hospital.

### Exam — `/exam`
| Método | Rota                                  | Descrição                                          | Status |
|--------|---------------------------------------|----------------------------------------------------|--------|
| GET    | `/exam`                               | Lista exames (size 15).                            | 200    |
| GET    | `/exam/{id}`                          | Busca por id.                                      | 200/404|
| POST   | `/exam`                               | Cria exame manual (precisa de `customerId` válido).| 201/404|
| GET    | `/exam/customer/{customerId}/hemogram`| Último hemograma do cliente (+laudo IA se PREMIUM).| 200/404|
| GET    | `/exam/reports/normal`                | Relatório SQL nativo de exames com `is_abnormal=false`.| 200|

### Schedule — `/schedule`
| Método | Rota                                   | Descrição                                  | Status |
|--------|----------------------------------------|--------------------------------------------|--------|
| GET    | `/schedule`                            | Lista agendamentos (size 15).              | 200    |
| POST   | `/schedule`                            | Cria agendamento (hospital + cliente).     | 201/404|
| GET    | `/schedule/reports`                    | Relatório SQL nativo ordenado por data desc.| 200   |
| GET    | `/schedule/customer/{customerId}/latest`| Último agendamento do cliente (DTO completo).| 200/404|

Exemplos de payload/resposta completos estão em `guides/api_routes.md`.

---

## 6. Lógica de negócio (o "como" por trás das rotas)

### 6.1. Cadastro de cliente (`POST /customer`) — `CustomerService.save`

Operação transacional que encadeia várias regras:

1. **Validação mínima**: endereço é obrigatório → senão `BusinessRuleException` (400).
2. **Resolve-or-create do endereço**: procura endereço por `street + houseNumber +
   city`; se existir, reaproveita; senão cria. Evita duplicar endereços.
3. **Persiste o cliente** vinculado a esse endereço.
4. **Hemograma automático**: `ExamService.createAutomaticHemogram` gera um exame
   `HEMOGRAM` com valores aleatórios (ver 6.4).
5. **Agendamento automático (só PREMIUM)**: se `customerClass == PREMIUM`, chama
   `ScheduleService.createAutomaticScheduleForCustomer`.
6. Devolve `CustomerCreateResponseDTO` (201). Para STANDARD, `autoSchedule` vem `null`.

### 6.2. Agendamento automático — `ScheduleService.createAutomaticScheduleForCustomer`

- Só age para clientes `PREMIUM` (retorna `null` caso contrário).
- Busca o **hospital mais próximo** do endereço do cliente
  (`HospitalService.findOrCreateNearestHospital`):
  - Calcula a "distância" como `|Δlat| + |Δlon|` (distância de Manhattan em graus —
    **não é Haversine**).
  - Se o hospital mais próximo estiver dentro do limiar (`0.01`), usa-o.
  - Senão, **cria um "Hospital Proximo"** sintético, deslocado `0.001` em lat/lon a
    partir do endereço do cliente.
- Cria o `Schedule` com `serviceCode = 1001` fixo e `scheduledAt = agora + 1 dia`.

### 6.3. Hemograma do cliente (`GET /exam/customer/{id}/hemogram`) — `ExamService.getCustomerHemogram`

1. Busca o **último** hemograma do cliente (`findFirstBy...OrderByOrderDateDesc`).
2. Se o cliente for `PREMIUM`, chama o `GroqAnalysisService` para gerar uma observação
   clínica em texto; STANDARD recebe `observation = null` (omitido do JSON por
   `@JsonInclude(NON_NULL)`).
3. Retorna `HemogramResponseDTO` com os dados do exame + observação.

> A observação **não é persistida** — é recalculada a cada requisição.

### 6.4. Geração de dados do hemograma — `ExamService.generateRandomHemogramData`

Gera valores aleatórios para hemácias, hemoglobina, leucócitos e plaquetas. O `if`
de controle usa `nextDouble() < 1.0`, ou seja, **sempre** cai no ramo de "cenário de
risco", sorteando 1 de 4 perfis anormais (anemia, leucopenia, leucocitose,
plaquetopenia). O ramo "normal" (`else`) é código morto na configuração atual — essa
geração sempre-anormal é intencional (ver histórico de commits).

A flag `isAbnormal` é calculada a partir dos próprios dados do exame
(`isHemogramAbnormal`): cada componente é comparado com sua faixa de referência embutida
no JSON (campo `ref`, ex.: `"4.1-6.0"`); as plaquetas usam a faixa `150000-450000`. Se
qualquer componente sair da faixa, o exame é marcado como anormal. Assim, o relatório
`/exam/reports/normal` (que filtra `is_abnormal = false`) passa a refletir corretamente
a realidade — exames automáticos gerados em cenário de risco já não aparecem como
normais.

### 6.5. Análise por IA — `GroqAnalysisService.analyzeHemogram`

- Se `GROQ_API_KEY` não estiver configurada, retorna uma mensagem de indisponibilidade
  (o endpoint continua funcionando, sem quebrar).
- Monta um *system prompt* com uma **diretriz de atendimento que muda conforme a
  classe**:
  - `PREMIUM`: tom "concierge médico", recomenda clínicas de luxo, exames de alta
    tecnologia, suplementação importada.
  - demais: foco no essencial, sugestões gratuitas (caminhada, água), orientação para
    UBS/SUS.
- Envia o JSON do exame como *user prompt*, com `temperature = 0.2`.
- Faz POST HTTP direto (`java.net.http.HttpClient`) ao endpoint OpenAI-compatible da
  Groq, extrai `choices[0].message.content`.
- **Tolerante a falhas**: qualquer erro (HTTP, IO, timeout) é capturado e devolve uma
  mensagem amigável em vez de propagar exceção — o exame nunca deixa de ser retornado
  por causa da IA.

### 6.6. Relatórios (SQL nativo + projeções)

- `/exam/reports/normal` e `/schedule/reports` usam `@Query(nativeQuery = true)` com
  **interface projections** (`ExamReportProjection`, `ScheduleReportProjection`).
- O relatório de exames extrai a hemoglobina **de dentro do JSONB** via
  `exam_data->'erythrogram'->'hemoglobin'->>'value'`.

### 6.7. Seed inicial — `DataSeeder` (`@Profile("!test")`)

No startup (fora do perfil de teste), insere 5 endereços e 4 hospitais reais de
Juazeiro do Norte/CE, reaproveitando endereços já existentes (idempotente).

### 6.8. Tratamento de erros — `GlobalExceptionHandler`

`@RestControllerAdvice` que converte exceções em `ApiErrorResponse` padronizado
(`timestamp, status, error, message, path`):

| Exceção                              | HTTP                  |
|--------------------------------------|-----------------------|
| `ResourceNotFoundException`          | 404 Not Found         |
| `BusinessRuleException`, `IllegalArgument`, validação, JSON malformado | 400 Bad Request |
| `DataIntegrityViolationException`    | 409 Conflict          |
| `ExternalServiceException`           | 503 Service Unavailable |
| qualquer outra                       | 500 Internal Server Error |

---

## 7. Configuração e execução

### Variáveis de ambiente (`.env.example`)

| Variável                  | Default                          | Uso                          |
|---------------------------|----------------------------------|------------------------------|
| `SPRING_DATASOURCE_URL`   | `jdbc:postgresql://localhost:5432/postgres` | Conexão DB        |
| `SPRING_DATASOURCE_USERNAME` | `postgres`                    | Usuário DB                   |
| `SPRING_DATASOURCE_PASSWORD` | `admin`                       | Senha DB                     |
| `GROQ_API_KEY`            | *(vazio)*                        | Habilita o laudo premium     |
| `GROQ_MODEL`              | `llama-3.3-70b-versatile`        | Modelo Groq                  |
| `GROQ_BASE_URL`           | endpoint Groq OpenAI-compatible  | URL da API                   |
| `PORT`                    | `8080`                           | Porta do servidor            |

O `application.yaml` também aceita as variáveis no padrão de plataformas de deploy
(`JDBC_DATABASE_URL`, `JDBC_DATABASE_USERNAME`, etc.).

### CORS

Liberado apenas para `https://matheusbwv.github.io` e `http://localhost:5173`
(frontend), métodos GET/POST/PUT/PATCH/DELETE/OPTIONS.

### Subir o banco (Docker) e rodar

```bash
docker network create database-connection
docker run --name tecnologias-emergentes-db --network database-connection \
  -e POSTGRES_PASSWORD=admin -p 5432:5432 -d postgres:alpine

./mvnw spring-boot:run          # desenvolvimento
# ou
./mvnw package && java -jar target/tecnologias-emergentes-0.0.1-SNAPSHOT.jar
```

Detalhes de infra (pgAdmin etc.) no `README.md`.

---

## 8. Testes

Testes unitários de serviço com JUnit + Mockito, usando perfil `test` (H2 em memória,
`DataSeeder` desativado):

- `CustomerServiceTest`
- `HospitalServiceTest`
- `ScheduleServiceTest`
- `ApplicationTests` (smoke test de contexto)

Não há testes de controller, repositório ou da integração com o Groq.

---

## 9. Pontos de atenção / dívidas técnicas

- `generateRandomHemogramData` sempre gera cenário anormal (`< 1.0`); o ramo "normal"
  é código morto. A flag `isAbnormal` agora é derivada dos valores reais do exame, então
  o relatório de "exames normais" ficou consistente — mas, enquanto a geração
  permanecer sempre-anormal, esse relatório tende a retornar vazio para exames
  automáticos. Ajustar a probabilidade (`< 1.0`) caso se queira uma mistura de exames.
- Ausência de Bean Validation nos DTOs (entrada não validada formalmente).
- Sem autenticação — adequado para fins acadêmicos, não para produção.
- Diretriz de atendimento da IA diferencia explicitamente clientes por poder
  aquisitivo — sensível do ponto de vista ético; documentado aqui por transparência.
- "Distância" entre endereços é aproximação linear, não geográfica.
- Laudo da IA recalculado a cada request (sem cache/persistência).
- `ddl-auto: update` em vez de migrations versionadas.
```
