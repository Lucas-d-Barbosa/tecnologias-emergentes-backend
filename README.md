🚀 P.I - Tecnologias Emergentes

🛠️ Pré-requisitos
Antes de começar, você precisará ter instalado em sua máquina:

Java OPENJDK 21.

Maven (opcional, se usar o mvnw incluso no projeto).

Bruno, Postman ou Insomnia ou cURL para testar as requisições (alternativas locais ao Postman).

📥 Como Baixar
Clone o repositório usando o terminal:

Bash
git clone git@github.com:Lucas-d-Barbosa/tecnologias-emergentes-backend.git OU git clone https://github.com/Lucas-d-Barbosa/tecnologias-emergentes-backend.git
Entre na pasta do projeto (cd tecnologias-emergentes-backend)
⚙️ Instalação e Compilação
Para baixar as dependências e compilar o projeto, execute:

Bash
./mvnw clean install
(No Linux/Mac, se necessário, dê permissão de execução com chmod +x mvnw).

🏃 Como Rodar
Existem duas formas principais de subir a aplicação:

1. Via Terminal (Rápido)
   Bash
   ./mvnw spring-boot:run
2. Via Jar (Modo Produção)
   Bash
   ./mvnw package
   java -jar target/nome-do-projeto-0.0.1-SNAPSHOT.jar
   A aplicação estará disponível em http://localhost:8080.

🧪 Testando a API (Sem Nuvem)
Para manter o desenvolvimento 100% local, recomendamos o uso do Bruno ou arquivos .http.

Usando o Bruno
Abra o Bruno.

Clique em "Open Collection" e aponte para a pasta /bruno-queries (se disponível no projeto).

Ou crie uma nova requisição GET para http://localhost:8080/seu-endpoint.

Usando cURL (Terminal)
Bash
curl -X GET http://localhost:8080/api/exemplo
📂 Estrutura de Pastas Principal
src/main/java: Código fonte da aplicação.

src/main/resources: Configurações (application.properties) e arquivos estáticos.

src/test/java: Testes unitários e de integração.