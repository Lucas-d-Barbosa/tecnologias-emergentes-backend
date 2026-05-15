# 🚀 P.I - Tecnologias Emergentes

## 📋 Pré-requisitos

Antes de começar, você precisará ter instalado em sua máquina:

* Java OpenJDK 21
* Docker & Docker Desktop (para o banco de dados)
* Maven (opcional, se usar o mvnw incluso no projeto)
* Bruno, Postman ou Insomnia para testar as requisições

## 🗄️ Infraestrutura (Banco de Dados)

Para facilitar o desenvolvimento local, utilizamos Docker para subir o PostgreSQL e o pgAdmin4.

### 1. Criar Rede e Subir Containers

Execute os comandos abaixo no terminal:

docker network create database-connection

docker run --name tecnologias-emergentes-db --network database-connection -e POSTGRES_PASSWORD=admin -p 5432:5432 -d postgres:alpine

docker run --name pgadmin4 --network database-connection -e 'PGADMIN_DEFAULT_EMAIL=admin@teste.com' -e 'PGADMIN_DEFAULT_PASSWORD=admin' -p 80:80 -d dpage/pgadmin4

### 2. Acessar o pgAdmin

1. Acesse http://localhost:80 no seu navegador.
2. Login: admin@teste.com | Senha: admin.
3. Ao adicionar um novo servidor (Add New Server), use na aba Connection:
* Host name/address: tecnologias-emergentes-db
* Port: 5432
* Username: postgres
* Password: admin



---

## 📥 Como Baixar

Clone o repositório usando o terminal:
git clone [https://github.com/Lucas-d-Barbosa/tecnologias-emergentes-backend.git]()
cd tecnologias-emergentes-backend

## 🛠️ Instalação e Compilação

Para baixar as dependências e compilar o projeto:
./mvnw clean install
(No Linux/Mac, se necessário, dê permissão de execução com chmod +x mvnw).

## 🏃 Como Rodar

A aplicação estará disponível em http://localhost:8080.

1. Via Terminal (Rápido)
   ./mvnw spring-boot:run
2. Via Jar (Modo Produção)
   ./mvnw package
   java -jar target/nome-do-projeto-0.0.1-SNAPSHOT.jar

## 🧪 Testando a API (Sem Nuvem)

Recomendamos o uso do Bruno ou arquivos .http para manter o desenvolvimento local.

### Usando o Bruno

1. Abra o Bruno.
2. Clique em "Open Collection" e aponte para a pasta /bruno-queries (se disponível no projeto).
3. Ou crie uma nova requisição GET para http://localhost:8080/seu-endpoint.

### Usando cURL (Terminal)

curl -X GET http://localhost:8080/api/exemplo

## 📂 Estrutura de Pastas Principal

* src/main/java: Código fonte da aplicação.
* src/main/resources: Configurações (application.properties) e arquivos estáticos.
* src/test/java: Testes unitários e de integração.