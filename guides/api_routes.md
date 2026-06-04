# Guia de Rotas da API — Payloads e Retornos

Base URL local: `http://localhost:8080`

Este guia documenta, para cada rota, o **payload de entrada** (quando houver), a
**resposta de sucesso** e as **respostas de erro** mais comuns. Não há autenticação:
todas as rotas são públicas.

---

## Convenções gerais

### Paginação

Todas as listagens (`GET` de coleção) são paginadas pelo Spring Data. Query params
opcionais:

| Param  | Descrição                                   | Exemplo                |
|--------|---------------------------------------------|------------------------|
| `page` | Página (base 0).                            | `?page=0`              |
| `size` | Itens por página (default 10 ou 15).        | `?size=20`             |
| `sort` | Campo e direção.                            | `?sort=name,asc`       |

Formato de resposta paginada (resumido — `pageable`/`sort` omitidos por brevidade):

```json
{
  "content": [ /* itens */ ],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 15,
  "first": true,
  "last": false,
  "numberOfElements": 15,
  "empty": false
}
```

### Enums aceitos

Os valores devem ser enviados **exatamente** com o nome do enum (maiúsculas):

| Enum            | Valores válidos                          |
|-----------------|------------------------------------------|
| `customerClass` | `STANDARD`, `PREMIUM`                     |
| `type` (exame)  | `HEMOGRAM`, `BIOCHEMICAL`, `IMAGING`      |

### Formato padrão de erro

Qualquer erro tratado retorna um `ApiErrorResponse` (campos nulos são omitidos):

```json
{
  "timestamp": "2026-06-04T17:13:00.123-03:00",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente não encontrado.",
  "path": "/customer/99"
}
```

| Situação                                               | HTTP | `message` (exemplo)                                                |
|--------------------------------------------------------|------|--------------------------------------------------------------------|
| Recurso inexistente                                    | 404  | `"Cliente não encontrado."`                                        |
| Regra de negócio violada                               | 400  | `"Os dados de endereço são obrigatórios para cadastrar um cliente."` |
| Falha de validação (Bean Validation)                   | 400  | `"name: O nome é obrigatório; email: O e-mail informado é inválido."` |
| JSON malformado / tipo de parâmetro inválido           | 400  | `"Não foi possível processar a requisição. Verifique o formato dos dados enviados."` |
| Violação de integridade (ex.: e-mail duplicado)        | 409  | `"Não foi possível concluir a operação porque já existe um registro com os mesmos dados ou há vínculo dependente."` |
| Falha em serviço externo (serialização JSONB)          | 503  | `"Não foi possível ler os dados do exame salvos no banco."`        |
| Erro inesperado                                        | 500  | `"Erro interno inesperado. Tente novamente mais tarde."`           |

---

## Address — `/address`

Endereços são **deduplicados** por `street + houseNumber + city`: ao criar cliente ou
hospital com um endereço já existente, o registro é reaproveitado.

| Método | Rota             | Descrição                          |
|--------|------------------|------------------------------------|
| GET    | `/address`       | Lista endereços (size padrão 10).  |
| GET    | `/address/{id}`  | Busca por id.                      |
| POST   | `/address`       | Cria endereço.                     |
| PATCH  | `/address/{id}`  | Atualização **parcial**.           |
| DELETE | `/address/{id}`  | Remove endereço.                   |

### `GET /address` → `200`

```json
{
  "content": [
    {
      "id": 1,
      "latitude": -7.237142,
      "longitude": -39.312403,
      "city": "Juazeiro do Norte",
      "street": "Av. Padre Cícero",
      "houseNumber": 2000,
      "customers": []
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

### `GET /address/{id}` → `200` / `404`

```json
{
  "id": 1,
  "latitude": -7.237142,
  "longitude": -39.312403,
  "city": "Juazeiro do Norte",
  "street": "Av. Padre Cícero",
  "houseNumber": 2000,
  "customers": []
}
```

### `POST /address` → `201`

Campos: `latitude`, `longitude`, `city`, `street`, `houseNumber`. O campo `houseNumber`
também aceita o alias `number` no JSON.

Body:
```json
{
  "latitude": -7.237142,
  "longitude": -39.312403,
  "city": "Juazeiro do Norte",
  "street": "Av. Padre Cícero",
  "houseNumber": 2000
}
```

Resposta `201`: o endereço criado (mesmo formato do `GET /address/{id}`).

### `PATCH /address/{id}` → `200` / `404`

Atualização **parcial**: envie apenas os campos a alterar. Campos ausentes (`null`) são
preservados.

Body (exemplo, alterando só o número):
```json
{
  "houseNumber": 2050
}
```

Resposta `200`: endereço atualizado.

### `DELETE /address/{id}` → `204` / `404`

Sem corpo. Pode retornar `409` se houver clientes/hospitais vinculados.

---

## Customer — `/customer`

| Método | Rota              | Descrição                                                       |
|--------|-------------------|-----------------------------------------------------------------|
| GET    | `/customer`       | Lista clientes (size padrão 15).                                |
| GET    | `/customer/{id}`  | Busca por id.                                                   |
| POST   | `/customer`       | Cria cliente + hemograma automático (+ agendamento se PREMIUM). |
| PUT    | `/customer/{id}`  | Atualiza cliente (substituição completa).                       |
| DELETE | `/customer/{id}`  | Remove cliente.                                                 |

### `POST /customer` → `201`

Validação de entrada (retorna `400` se violada):

| Campo           | Regra                          | Mensagem                              |
|-----------------|--------------------------------|---------------------------------------|
| `name`          | obrigatório, não vazio         | `"O nome é obrigatório."`             |
| `email`         | obrigatório + formato de e-mail| `"O e-mail informado é inválido."`    |
| `customerClass` | obrigatório (`STANDARD`/`PREMIUM`) | `"A classe do cliente é obrigatória."` |
| `address`       | obrigatório                    | `"Os dados de endereço são obrigatórios."` |

Body:
```json
{
  "name": "Hans Oliveira",
  "email": "hans@dev.com",
  "customerClass": "PREMIUM",
  "address": {
    "latitude": -7.237142,
    "longitude": -39.312403,
    "city": "Juazeiro do Norte",
    "street": "Av. Padre Cícero",
    "houseNumber": 2000
  }
}
```

Resposta `201` — **cliente PREMIUM** (gera agendamento automático):
```json
{
  "id": 1,
  "name": "Hans Oliveira",
  "email": "hans@dev.com",
  "customerClass": "PREMIUM",
  "address": {
    "latitude": -7.237142,
    "longitude": -39.312403,
    "city": "Juazeiro do Norte",
    "street": "Av. Padre Cícero",
    "houseNumber": 2000
  },
  "autoSchedule": {
    "scheduleId": 101,
    "serviceCode": 1001,
    "scheduledAt": "2026-06-05T17:13:00.123-03:00",
    "customerId": 1,
    "customerName": "Hans Oliveira",
    "customerClass": "PREMIUM",
    "customerAddress": {
      "latitude": -7.237142,
      "longitude": -39.312403,
      "city": "Juazeiro do Norte",
      "street": "Av. Padre Cícero",
      "houseNumber": 2000
    },
    "hospitalId": 2,
    "hospitalName": "Hospital Regional do Cariri",
    "hospitalType": "Público",
    "hospitalAddress": {
      "latitude": -7.22984,
      "longitude": -39.29718,
      "city": "Juazeiro do Norte",
      "street": "Rua Catulo da Paixão Cearense",
      "houseNumber": 219
    }
  }
}
```

Resposta `201` — **cliente STANDARD** (`autoSchedule` é omitido por ser `null`):
```json
{
  "id": 2,
  "name": "Maria Souza",
  "email": "maria@dev.com",
  "customerClass": "STANDARD",
  "address": {
    "latitude": -7.229840,
    "longitude": -39.297180,
    "city": "Juazeiro do Norte",
    "street": "Rua São Pedro",
    "houseNumber": 1470
  }
}
```

> Em ambos os casos, um **hemograma automático** é gerado em background (consultável em
> `GET /exam/customer/{id}/hemogram`).

### `GET /customer` / `GET /customer/{id}` → `200` / `404`

Retorna a entidade `Customer` com o endereço embutido:
```json
{
  "id": 1,
  "name": "Hans Oliveira",
  "email": "hans@dev.com",
  "address": {
    "id": 1,
    "latitude": -7.237142,
    "longitude": -39.312403,
    "city": "Juazeiro do Norte",
    "street": "Av. Padre Cícero",
    "houseNumber": 2000
  },
  "customerClass": "PREMIUM"
}
```

### `PUT /customer/{id}` → `200` / `404`

Substituição completa. Mesmo body e mesmas regras de validação do `POST /customer`.
Resposta `200`: cliente atualizado.

### `DELETE /customer/{id}` → `204` / `404`

Sem corpo.

---

## Hospital — `/hospital`

| Método | Rota              | Descrição                              |
|--------|-------------------|----------------------------------------|
| GET    | `/hospital`       | Lista hospitais (size padrão 15).      |
| GET    | `/hospital/{id}`  | Busca por id.                          |
| POST   | `/hospital`       | Cria hospital (reaproveita endereço).  |
| DELETE | `/hospital/{id}`  | Remove hospital.                       |

> Não há `PUT`/`PATCH` de hospital.

### `POST /hospital` → `201`

Validação (retorna `400` se violada):

| Campo          | Regra              | Mensagem                                |
|----------------|--------------------|-----------------------------------------|
| `categoryName` | obrigatório        | `"O nome da categoria é obrigatório."`  |
| `categoryType` | obrigatório        | `"O tipo da categoria é obrigatório."`  |
| `address`      | obrigatório        | `"Os dados de endereço são obrigatórios."` |

Body:
```json
{
  "categoryName": "Hospital Regional do Cariri",
  "categoryType": "Público",
  "address": {
    "latitude": -7.22984,
    "longitude": -39.29718,
    "city": "Juazeiro do Norte",
    "street": "Rua Catulo da Paixão Cearense",
    "houseNumber": 219
  }
}
```

Resposta `201`: hospital criado com o endereço vinculado:
```json
{
  "id": 2,
  "categoryName": "Hospital Regional do Cariri",
  "categoryType": "Público",
  "address": {
    "id": 3,
    "latitude": -7.22984,
    "longitude": -39.29718,
    "city": "Juazeiro do Norte",
    "street": "Rua Catulo da Paixão Cearense",
    "houseNumber": 219
  }
}
```

### `GET /hospital` / `GET /hospital/{id}` → `200` / `404`

Página/objeto com hospitais e endereço embutido (mesmo formato do `201` acima).

### `DELETE /hospital/{id}` → `204` / `404`

Sem corpo.

---

## Exam — `/exam`

| Método | Rota                                  | Descrição                                          |
|--------|---------------------------------------|----------------------------------------------------|
| GET    | `/exam`                               | Lista exames (size padrão 15).                     |
| GET    | `/exam/{id}`                          | Busca por id.                                      |
| POST   | `/exam`                               | Cria exame manual.                                 |
| GET    | `/exam/customer/{customerId}/hemogram`| Último hemograma do cliente (+ laudo IA se PREMIUM).|
| GET    | `/exam/reports/normal`                | Relatório SQL nativo de exames `is_abnormal=false`.|

### Estrutura do `examData` (JSONB)

```json
{
  "erythrogram": {
    "rbc":        { "value": 4.8,  "unit": "10^6/µL", "ref": "4.1-6.0" },
    "hemoglobin": { "value": 14.2, "unit": "g/dL",    "ref": "12.0-17.5" }
  },
  "leukogram": {
    "wbc_total":  { "value": 7200, "unit": "/µL",     "ref": "4500-11000" }
  },
  "platelets":    { "count": 230000 }
}
```

### `POST /exam` → `201` / `404`

Validação (`400` se violada):

| Campo        | Regra                              | Mensagem                              |
|--------------|------------------------------------|---------------------------------------|
| `customerId` | obrigatório (cliente deve existir) | `"O cliente associado é obrigatório."` |
| `type`       | obrigatório                        | `"O tipo de exame é obrigatório."`    |
| `examData`   | opcional                           | —                                     |
| `isAbnormal` | opcional (default `false`)         | —                                     |

Retorna `404` se o `customerId` não existir
(`"Cliente associado não encontrado para gerar o exame."`).

Body:
```json
{
  "customerId": 1,
  "type": "HEMOGRAM",
  "examData": {
    "erythrogram": {
      "rbc": { "value": 4.8, "unit": "10^6/µL", "ref": "4.1-6.0" }
    }
  },
  "isAbnormal": false
}
```

Resposta `201`: o exame criado.

### `GET /exam/customer/{customerId}/hemogram` → `200` / `404`

Retorna o **último** hemograma do cliente.

- `STANDARD`: retorna apenas os dados estruturados; `observation` é omitido.
- `PREMIUM`: consulta o Groq e preenche `observation` com o laudo em texto.

`404` se o cliente ou o hemograma não existirem.

Resposta `200` (PREMIUM):
```json
{
  "examId": 10,
  "customerId": 1,
  "customerClass": "PREMIUM",
  "examData": {
    "erythrogram": {
      "rbc": { "value": 4.8, "unit": "10^6/µL", "ref": "4.1-6.0" },
      "hemoglobin": { "value": 14.2, "unit": "g/dL", "ref": "12.0-17.5" }
    },
    "leukogram": {
      "wbc_total": { "value": 7200, "unit": "/µL", "ref": "4500-11000" }
    },
    "platelets": { "count": 230000 }
  },
  "observation": "Os resultados do seu hemograma estão dentro da normalidade..."
}
```

> Se `GROQ_API_KEY` não estiver configurada, o endpoint PREMIUM ainda retorna o exame e
> preenche `observation` com uma mensagem de indisponibilidade (não quebra a resposta).
> A chamada ao Groq tem timeout de conexão (10s) e de requisição (30s).

### `GET /exam/reports/normal` → `200`

Relatório SQL nativo (projeção) dos exames com `is_abnormal = false`, extraindo a
hemoglobina de dentro do JSONB:
```json
{
  "content": [
    {
      "patient": "Maria Souza",
      "testType": "HEMOGRAM",
      "orderDate": "2026-06-04T17:13:00.123-03:00",
      "hemoglobinResult": "14.2"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 15
}
```

---

## Schedule — `/schedule`

| Método | Rota                                    | Descrição                                  |
|--------|-----------------------------------------|--------------------------------------------|
| GET    | `/schedule`                             | Lista agendamentos (size padrão 15).       |
| POST   | `/schedule`                             | Cria agendamento manual.                   |
| GET    | `/schedule/reports`                     | Relatório SQL nativo ordenado por data desc.|
| GET    | `/schedule/customer/{customerId}/latest`| Último agendamento do cliente (DTO completo).|

### `POST /schedule` → `201` / `404`

Validação (`400` se violada):

| Campo         | Regra       | Mensagem                                |
|---------------|-------------|-----------------------------------------|
| `serviceCode` | obrigatório | `"O código de serviço é obrigatório."`  |
| `hospitalId`  | obrigatório | `"O hospital é obrigatório."`           |
| `customerId`  | obrigatório | `"O cliente é obrigatório."`            |
| `scheduledAt` | obrigatório | `"A data de agendamento é obrigatória."` |

Retorna `404` se `hospitalId` ou `customerId` não existirem.

Body:
```json
{
  "serviceCode": 1001,
  "hospitalId": 2,
  "customerId": 1,
  "scheduledAt": "2026-06-20T10:00:00-03:00"
}
```

Resposta `201`: o agendamento criado.

### `GET /schedule/reports` → `200`

Relatório SQL nativo (projeção), ordenado por `scheduled_at` desc:
```json
{
  "content": [
    {
      "scheduledAt": "2026-06-20T10:00:00-03:00",
      "patient": "Hans Oliveira",
      "hospital": "Hospital Regional do Cariri"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 15
}
```

### `GET /schedule/customer/{customerId}/latest` → `200` / `404`

Último agendamento do cliente, com hospital e endereços resolvidos. `404` se o cliente
não tiver agendamento.

```json
{
  "scheduleId": 101,
  "serviceCode": 1001,
  "scheduledAt": "2026-06-05T17:13:00.123-03:00",
  "customerId": 1,
  "customerName": "Hans Oliveira",
  "customerClass": "PREMIUM",
  "customerAddress": {
    "latitude": -7.237142,
    "longitude": -39.312403,
    "city": "Juazeiro do Norte",
    "street": "Av. Padre Cícero",
    "houseNumber": 2000
  },
  "hospitalId": 2,
  "hospitalName": "Hospital Regional do Cariri",
  "hospitalType": "Público",
  "hospitalAddress": {
    "latitude": -7.22984,
    "longitude": -39.29718,
    "city": "Juazeiro do Norte",
    "street": "Rua Catulo da Paixão Cearense",
    "houseNumber": 219
  }
}
```

---

## Groq (análise por IA)

Não é uma rota — é o serviço externo consumido por
`GET /exam/customer/{customerId}/hemogram` para clientes `PREMIUM`. Configurado por
variáveis de ambiente:

| Variável        | Default                                                      | Obrigatória |
|-----------------|--------------------------------------------------------------|-------------|
| `GROQ_API_KEY`  | *(vazio)*                                                    | Sim (p/ laudo) |
| `GROQ_MODEL`    | `llama-3.3-70b-versatile`                                    | Não         |
| `GROQ_BASE_URL` | `https://api.groq.com/openai/v1/chat/completions`            | Não         |

Sem a chave configurada, o laudo é substituído por uma mensagem de indisponibilidade e
o restante da resposta é entregue normalmente.
</content>
