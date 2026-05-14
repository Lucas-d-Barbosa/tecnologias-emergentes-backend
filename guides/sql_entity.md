# Guia de Implementação: Sistema de Gestão de Exames (PostgreSQL)

Este guia detalha a criação de um banco de dados relacional preparado para alta performance, utilizando tipos avançados e suporte a dados semiestruturados via **JSONB**.

---

## 1. DDL: Tipos e Tabelas

### 1.1. Tipos Enumerados (Enums)
Utilizamos Enums para garantir a integridade dos dados e evitar "strings mágicas" no banco.

```sql
CREATE TYPE exam_type AS ENUM ('Hemogram', 'Biochemical', 'Imaging');
CREATE TYPE class_type AS ENUM ('Standard', 'Premium');
```

### 1.2. Estrutura das Tabelas

```sql
-- 1. Address: Armazenamento de localização com precisão decimal
CREATE TABLE address (
    address_id BIGSERIAL PRIMARY KEY,
    latitude   DECIMAL(9,6) NOT NULL, -- Precisão para GPS
    longitude  DECIMAL(9,6) NOT NULL,
    city       VARCHAR(120) NOT NULL,
    street     VARCHAR(120) NOT NULL,
    number     INTEGER NOT NULL
);

-- 2. Customer: Dados do paciente vinculados ao plano (Class)
CREATE TABLE customer (
    customer_id BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    address_id  BIGINT NOT NULL REFERENCES address(address_id) 
                ON UPDATE CASCADE ON DELETE RESTRICT,
    class       class_type NOT NULL
);

-- 3. Exam: Armazenamento de resultados em JSONB (Compatível com Java 21 Records)
CREATE TABLE exam (
    exam_id     BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(customer_id) 
                ON UPDATE CASCADE ON DELETE CASCADE,
    type        exam_type NOT NULL,
    order_date  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    exam_data   JSONB, -- Estrutura esperada: erythrogram, leukogram, platelets
    is_abnormal BOOLEAN DEFAULT FALSE
);

-- 4. Hospital: Unidades de atendimento
CREATE TABLE hospital (
    hospital_id   BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(120) NOT NULL,
    category_type VARCHAR(60)  NOT NULL,
    address_id    BIGINT NOT NULL REFERENCES address(address_id) 
                  ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 5. Schedule: Tabela de agendamentos (Junção)
CREATE TABLE schedule (
    schedule_id  BIGSERIAL PRIMARY KEY,
    service_code BIGINT NOT NULL, -- ID do serviço ou categoria interna
    hospital_id  BIGINT NOT NULL REFERENCES hospital(hospital_id) 
                 ON UPDATE CASCADE ON DELETE CASCADE,
    customer_id  BIGINT NOT NULL REFERENCES customer(customer_id) 
                 ON UPDATE CASCADE ON DELETE CASCADE,
    scheduled_at TIMESTAMPTZ NOT NULL
);
```

### 1.3. Índices de Performance
Fundamentais para otimizar os `JOINs` e buscas dentro do campo JSON.

```sql
CREATE INDEX idx_customer_address   ON customer(address_id);
CREATE INDEX idx_exam_customer      ON exam(customer_id);
CREATE INDEX idx_hospital_address   ON hospital(address_id);
CREATE INDEX idx_schedule_hospital  ON schedule(hospital_id);
CREATE INDEX idx_schedule_customer  ON schedule(customer_id);

-- Índice GIN para buscas rápidas dentro dos resultados dos exames
CREATE INDEX idx_exam_data_gin      ON exam USING GIN (exam_data);
```

---

## 2. Ordem de Execução Sugerida

1. **Tipos:** `exam_type`, `class_type`.
2. **Tabelas Independentes:** `address`.
3. **Dependência Nível 1:** `customer`, `hospital`.
4. **Eventos/Resultados:** `exam`, `schedule`.
5. **Índices.**

---

## 3. Scripts de Exemplo (DML)

### Inserts de Teste

```sql
-- Endereço
INSERT INTO address (latitude, longitude, city, street, number)
VALUES (-7.237142, -39.412403, 'Juazeiro do Norte', 'Av. Padre Cícero', 2000);

-- Cliente
INSERT INTO customer (name, email, address_id, class)
VALUES ('Hans Oliveira', 'hans@dev.com', 1, 'Premium');

-- Exame (Hemograma completo no formato JSONB)
INSERT INTO exam (customer_id, type, exam_data, is_abnormal)
VALUES (1, 'Hemogram', '{
  "erythrogram": {
    "rbc": {"value": 4.8, "unit": "10^6/µL", "ref": "4.5-5.9"},
    "hemoglobin": {"value": 14.2, "unit": "g/dL", "ref": "13.5-17.5"}
  },
  "leukogram": {
    "wbc_total": {"value": 6500, "unit": "/µL", "ref": "4000-11000"}
  },
  "platelets": {"count": 250000}
}', false);
```

---

## 4. Consultas Úteis

### Relatório de Exames por Cliente (SQL Nativo)
```sql
SELECT 
    c.name AS patient, 
    e.type AS test_type, 
    e.order_date,
    e.exam_data->'erythrogram'->'hemoglobin'->>'value' AS hemoglobin_result
FROM customer c 
JOIN exam e ON e.customer_id = c.customer_id
WHERE e.is_abnormal = false;
```

---

## 5. Boas Práticas de Manutenção

* **Transações:** Sempre utilize `BEGIN;` e `COMMIT;` ao rodar alterações de schema em produção.
* **Monitoramento:** Use `EXPLAIN ANALYZE` em consultas que envolvam o campo `exam_data`.
* **Backup:** Realize o `pg_dump` focando especificamente no schema e nos tipos ENUM.
```