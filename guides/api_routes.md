# Rotas da API

Base URL local: `http://localhost:8080`

## Address

### `GET /address`
Lista os endereços cadastrados.

Query params opcionais: `page`, `size`, `sort`.

Resposta `200`:
```json
{
  "content": [
    {
      "id": 1,
      "latitude": -7.237142,
      "longitude": -39.412403,
      "city": "Juazeiro do Norte",
      "street": "Av. Padre Cícero",
      "houseNumber": 2000,
      "customers": []
    }
  ],
  "pageable": {}
}
```

### `GET /address/{id}`
Busca um endereço pelo id.

Resposta `200`:
```json
{
  "id": 1,
  "latitude": -7.237142,
  "longitude": -39.412403,
  "city": "Juazeiro do Norte",
  "street": "Av. Padre Cícero",
  "houseNumber": 2000,
  "customers": []
}
```

### `POST /address`
Cria um endereço.

Body:
```json
{
  "latitude": -7.237142,
  "longitude": -39.412403,
  "city": "Juazeiro do Norte",
  "street": "Av. Padre Cícero",
  "houseNumber": 2000
}
```

Resposta `201`:
```json
{
  "id": 1,
  "latitude": -7.237142,
  "longitude": -39.412403,
  "city": "Juazeiro do Norte",
  "street": "Av. Padre Cícero",
  "houseNumber": 2000,
  "customers": []
}
```

### `PATCH /address/{id}`
Atualiza um endereço existente.

Body igual ao `POST /address`.

Resposta `200`: endereço atualizado.

### `DELETE /address/{id}`
Remove um endereço.

Resposta `204`: sem conteúdo.

## Customer

### `GET /customer`
Lista clientes.

Resposta `200`: página com clientes e endereço embutido.

### `GET /customer/{id}`
Busca cliente por id.

### `POST /customer`
Cria cliente e reaproveita endereço se ele já existir por `street + houseNumber + city`.

Body:
```json
{
  "name": "Hans Oliveira",
  "email": "hans@dev.com",
  "customerClass": "Premium",
  "address": {
    "latitude": -7.237142,
    "longitude": -39.412403,
    "city": "Juazeiro do Norte",
    "street": "Av. Padre Cícero",
    "houseNumber": 2000
  }
}
```

Resposta `201`: cliente criado com o endereço vinculado.

### `PUT /customer/{id}`
Atualiza cliente.

Body igual ao `POST /customer`.

Resposta `200`: cliente atualizado.

### `DELETE /customer/{id}`
Remove cliente.

Resposta `204`: sem conteúdo.

## Hospital

### `GET /hospital`
Lista hospitais.

Resposta `200`: página com hospitais e endereço embutido.

### `GET /hospital/{id}`
Busca hospital por id.

### `POST /hospital`
Cria hospital e reaproveita endereço se ele já existir por `street + houseNumber + city`.

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

Resposta `201`: hospital criado com o endereço vinculado.

### `DELETE /hospital/{id}`
Remove hospital.

Resposta `204`: sem conteúdo.

## Exam

### `GET /exam`
Lista exames.

### `GET /exam/{id}`
Busca exame por id.

### `POST /exam`
Cria exame.

Body:
```json
{
  "customerId": 1,
  "type": "Hemogram",
  "examData": {
    "erythrogram": {
      "rbc": { "value": 4.8, "unit": "10^6/µL", "ref": "4.5-5.9" }
    }
  },
  "isAbnormal": false
}
```

### `GET /exam/reports/normal`
Relatório de exames normais.

## Schedule

### `GET /schedule`
Lista agendamentos.

### `POST /schedule`
Cria agendamento.

Body:
```json
{
  "serviceCode": 1001,
  "hospitalId": 1,
  "customerId": 1,
  "scheduledAt": "2026-05-20T10:00:00-03:00"
}
```

### `GET /schedule/reports`
Relatório de agendamentos em ordem de data.