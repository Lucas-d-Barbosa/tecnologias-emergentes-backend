# Tecnologias Emergentes - Backend

Este repositório contém o backend da prova de conceito que investiga a estratificação algorítmica no acesso a ferramentas de IA na saúde.

Visão rápida
- O sistema calcula um `accessClass` para cada usuário com base em três pilares: profissão, renda e CEP.
- `VIP` (Classe B+): diagnóstico instantâneo via IA, agendamento automático e prioridade.
- `BASE` (Classe C-): acesso limitado, só dados brutos, fila por ordem de chegada.
- O backend expõe endpoints para criação de usuários, cálculo de acesso e chamada à integração de IA (stub).

Arquivos principais
- `Dockerfile` - build multi-stage do aplicativo.
- `docker/postgres/Dockerfile` + `docker/postgres/init/*.sql` - imagem Postgres customizada que aplica schema e seeds automaticamente.
- `docker-compose.yml` - orquestra `db` (Postgres custom) e `app`.
- `.devcontainer/` - configuração para abrir o projeto no Dev Container do VS Code.
- `src/...` - código fonte Spring Boot (models, serviços, controllers).

Rodando com Docker Compose (recomendado)

1. Construa e suba os serviços (construirá a imagem do Postgres, aplicará migrations/seeds e depois subirá o app):

```bash
docker-compose up --build
```

2. Verifique logs e saúde dos serviços. A API ficará disponível em `http://localhost:8080`.

Como as migrations e seeds funcionam
- Ao construir a imagem `db` a pasta `docker/postgres/init` é copiada para `/docker-entrypoint-initdb.d/` no container Postgres. Scripts SQL ali colocados são executados automaticamente na inicialização da base quando o volume é criado pela primeira vez.
- Arquivos incluídos:
	- `01_schema.sql` — cria a tabela `users` compatível com a entidade JPA.
	- `02_seeds.sql` — insere exemplos realistas (VIP, STANDARD, BASE) para testes.

Conferir dados (exemplo com `psql`):

```bash
docker exec -it <db_container_name> psql -U postgres -d tecnologias -c "SELECT id,name,profession,income_bracket,postal_code,access_class FROM users;"
```

Factory de testes
- Foi adicionada uma factory simples em `src/test/java/tecnologias_emergentes/TestDataFactory.java` que expõe beans de usuários de exemplo para uso em testes de integração.

Credenciais e variáveis importantes
- Credencial HTTP Basic padrão: `admin` / `admin` (configuráveis via `app.security.*` ou variáveis de ambiente).
- `SPRING_DATASOURCE_URL` — JDBC do Postgres (ex: `jdbc:postgresql://db:5432/tecnologias`).
- `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` — credenciais do banco.

Executar localmente sem Docker
- Build (se tiver Maven instalado):

```bash
mvn -DskipTests package
java -jar target/*.jar
```

Endpoints principais
- `POST /api/users` — cria `User` e calcula `accessClass`.
- `GET /api/users/{id}/access` — recalcula e retorna o usuário com a classe de acesso.
- `POST /api/users/{id}/diagnose` — chama o stub de IA que simula comportamento por `accessClass`.

Motivação e propósito

Este backend existe para demonstrar, de forma prática e observável, como decisões de design e políticas de acesso a ferramentas de IA podem atuar como mecanismos de estratificação social. A implementação inclui um stub de integração de IA que trata usuários de acordo com sua `accessClass`, provendo evidência técnica para a discussão crítica: a tecnologia por si só não garante equidade — as políticas de acesso sim.

Próximos passos sugeridos
- Adicionar Flyway/Liquibase para versionamento de migrations.
- Substituir o stub por integração real com provedor de IA, com filas e SLAs diferenciados.
- Adotar JWT/OAuth2 e criar testes de integração que validem fluxos por `accessClass`.
