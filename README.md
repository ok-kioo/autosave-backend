# 🎮 AutoSave Backend

> API backend em Spring Boot para a AutoSave, uma newsletter/clube exclusivo para entusiastas de games, com envio diário de notícias do mundo gamer.

## 📋 Sobre o projeto

AutoSave é a API que sustenta uma newsletter voltada para o público gamer. O sistema permite criar e gerenciar conteúdos de e-mail, organizá-los em campanhas, submetê-las a um fluxo de revisão, e distribuí-las aos assinantes através de planos de assinatura pagos.

Explique de forma simples:

- O que o projeto faz: gerencia usuários, planos de assinatura, contratos, conteúdos e campanhas de e-mail (com fluxo de revisão/aprovação), pagamentos (cartão de crédito e Pix, via Mercado Pago) e o envio efetivo dos e-mails (via Amazon SES);
- Para quem ele foi desenvolvido: para a operação da newsletter AutoSave, atendendo tanto a equipe interna (editores, revisores, administradores) quanto os assinantes finais;
- Qual problema resolve: centraliza a criação de conteúdo, a governança de revisão editorial, a cobrança recorrente de assinaturas e a entrega de e-mails em uma única API robusta e escalável;
- Quais são seus principais diferenciais: arquitetura modular orientada a domínio, padrão *Outbox* para garantir consistência entre banco relacional e mensageria, uso de grafo (Neo4j) para modelar interações dos leitores com as campanhas (visualizações, curtidas e comentários), autenticação via JWT com controle de papéis (roles) e integração nativa com gateway de pagamento.

### 🎯 Objetivos

- Automatizar a criação, revisão e envio de campanhas de newsletter para o público gamer;
- Gerenciar de ponta a ponta o ciclo de assinatura: planos, contratos, cobrança e métodos de pagamento;
- Oferecer uma API segura, documentada e testável para consumo pelo front-end da AutoSave.

---

## ✨ Funcionalidades

- ✅ Autenticação de usuários via JWT (login) e controle de acesso por papéis (`ADMIN`, `EDITOR`, `REVIEWER`, `VIEWER`, `BILLING_MANAGER`)
- ✅ CRUD de usuários (criação, atualização, atualização de papel e exclusão)
- ✅ Gestão de conteúdos de e-mail (criação, atualização, listagem, exclusão)
- ✅ Gestão de campanhas de e-mail, com fluxo de revisão/aprovação (`EmailCampaignReview`)
- ✅ Interações de leitores com as campanhas via Neo4j: registro de visualizações, curtidas e comentários (com respostas em thread)
- ✅ Gestão de planos de assinatura (`SubscriptionPlan`) e de contratos de plano (`PlanContract`), incluindo cancelamento e reembolso
- ✅ Gestão de métodos de pagamento (cartão de crédito e Pix)
- ✅ Integração com o gateway Mercado Pago, incluindo endpoint de *webhook* para eventos de pagamento
- ✅ Padrão *Transactional Outbox* para publicação confiável de eventos (via RabbitMQ) a partir de mudanças de estado no banco
- ✅ Envio de e-mails transacionais/campanhas via Amazon SES
- ✅ Cache com Redis
- ✅ *Rate limiting* nas requisições (Bucket4j)
- ✅ Documentação interativa da API via Swagger/OpenAPI
- ✅ Migrações versionadas de banco de dados (Flyway para PostgreSQL e Neo4j Migrations para o grafo)

---

## 🛠️ Tecnologias utilizadas

### Back-end

- [Java 23](https://www.oracle.com/java/)
- [Spring Boot 3.5.4](https://spring.io/projects/spring-boot)
- [Spring Web (MVC)](https://spring.io/projects/spring-framework)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data Neo4j](https://spring.io/projects/spring-data-neo4j)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Spring AMQP](https://spring.io/projects/spring-amqp) (RabbitMQ)
- [Spring Mail](https://docs.spring.io/spring-framework/reference/integration/email.html)
- [JJWT (JSON Web Token)](https://github.com/jwtk/jjwt)
- [Bucket4j](https://bucket4j.com/) (rate limiting)
- [Springdoc OpenAPI (Swagger)](https://springdoc.org/)
- [Lombok](https://projectlombok.org/)
- [AWS SDK for Java – SES](https://docs.aws.amazon.com/ses/)
- [SDK Mercado Pago (Java)](https://github.com/mercadopago/sdk-java)
- [Unirest Java](https://kong.github.io/unirest-java/)

### Banco de dados

- PostgreSQL (dados relacionais: usuários, planos, contratos, pagamentos, conteúdo)
- Neo4j (grafo de interações: visualizações, curtidas e comentários das campanhas)
- Redis (cache e controle de acesso)

### Ferramentas

- Git
- GitHub
- GitHub Actions (CI com Maven e análise estática via SonarCloud)
- Docker / Docker Compose
- Maven (com Maven Wrapper — `mvnw`)

---

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- [Git](https://git-scm.com/)
- [JDK 23](https://jdk.java.net/23/)
- Maven `>= 3.9` (opcional, o projeto inclui o Maven Wrapper `mvnw`)
- [Docker](https://www.docker.com/) e Docker Compose (para subir PostgreSQL, Neo4j, Redis e RabbitMQ localmente)

Verifique as versões:

```
git --version
java --version
./mvnw --version
docker --version
```

---

## 🚀 Instalação

### 1. Clone o repositório

```
git clone https://github.com/ok-kioo/autosave-backend.git
```

### 2. Entre na pasta

```
cd autosave-backend
```

### 3. Configure as variáveis de ambiente

Crie um arquivo `.env` baseado no exemplo:

```
cp .env.example .env
```

Preencha as variáveis necessárias (veja a lista completa abaixo em [Variáveis de ambiente](#variáveis-de-ambiente)).

### 4. Suba a infraestrutura com Docker Compose

O projeto depende de PostgreSQL, Neo4j, Redis e RabbitMQ. Suba os serviços de desenvolvimento com:

```
docker compose -f docker-compose-dev.yaml up -d
```

### 5. Execute o projeto

Usando o Maven Wrapper:

```
./mvnw spring-boot:run
```

O projeto estará disponível em:

```
http://localhost:8080
```

As migrações do PostgreSQL (Flyway) e do Neo4j são aplicadas automaticamente na inicialização.

#### Variáveis de ambiente

| Variável | Descrição |
| --- | --- |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_URL` | Credenciais e URL de conexão do PostgreSQL |
| `API_ACCESS_TOKEN_REDIS` | Token de acesso relacionado ao Redis |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Conexão com o Redis |
| `NEO4J_USER` / `NEO4J_PASSWORD` / `NEO4J_URI` | Conexão com o Neo4j |
| `JWT_TOKEN_SECRET` / `JWT_TOKEN_EXPIRATION` | Segredo e tempo de expiração do token JWT |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` / `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | Conexão com o RabbitMQ |
| `SMTP_MAIL` / `AWS_REGION` | Configuração de envio de e-mails via Amazon SES |
| `MP_ACCESS_TOKEN` / `MP_PUBLIC_KEY` / `MP_USER_ID` / `MP_USER_LOGIN` / `MP_PASSWORD` | Credenciais de integração com o Mercado Pago |
| `BACKEND_URL` / `FRONTEND_URL` | URLs do back-end e do front-end da aplicação |

---

## 📖 Como usar

Depois de subir a infraestrutura e a aplicação, a API estará disponível em `http://localhost:8080`.

### Documentação da API (Swagger)

```
http://localhost:8080/swagger-ui/index.html
```

### Exemplo — autenticação

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "usuario@exemplo.com",
  "password": "sua-senha"
}
```

A resposta traz o usuário autenticado e o token JWT, que deve ser enviado no header `Authorization` das demais requisições.

Os principais grupos de endpoints disponíveis são:

- `/auth` — autenticação
- `/users` — gestão de usuários
- `/email/content` — conteúdos de e-mail
- `/email/campaign` — campanhas de e-mail e revisões
- `/email/campaign/node` — interações (visualizações, curtidas, comentários)
- `/subscription` — planos de assinatura
- `/contract` — contratos de plano
- `/payment/methods` — métodos de pagamento
- `/payload` — payloads de pagamento
- `/webhooks/mercadopago` — webhook do Mercado Pago

---

## 🧪 Testes

Execute os testes com:

```
./mvnw test
```

Para gerar o pacote executando os testes:

```
./mvnw clean install
```

A esteira de CI (GitHub Actions) executa automaticamente o build e os testes a cada push/PR para a branch `development`, além de rodar análise estática de código via SonarCloud.

---

## 🐳 Docker

O projeto utiliza Docker Compose para orquestrar sua infraestrutura de desenvolvimento (PostgreSQL, Neo4j, Redis e RabbitMQ).

### Executar os serviços de infraestrutura

```
docker compose -f docker-compose-dev.yaml up -d
```

Para interromper:

```
docker compose -f docker-compose-dev.yaml down
```

> A aplicação Spring Boot em si é executada localmente via `./mvnw spring-boot:run` (ou pelo `.jar` gerado); o `docker-compose-dev.yaml` fornecido não inclui um serviço para a aplicação.

---

## 📁 Estrutura do projeto

```
autosave-backend/
├── src/
│   ├── main/
│   │   ├── java/com/signature/autosave/
│   │   │   ├── controller/       # Controllers REST (auth, user, email, contract, node, payment, subscription, webhook)
│   │   │   ├── infra/            # Configurações, filtros, componentes de infraestrutura (JWT, cache, e-mail, gateway de pagamento)
│   │   │   ├── modules/          # Domínio da aplicação, organizado por módulo:
│   │   │   │   ├── auth/         #   - autenticação
│   │   │   │   ├── user/         #   - usuários
│   │   │   │   ├── contract/     #   - contratos de plano
│   │   │   │   ├── email/        #   - conteúdo e campanhas de e-mail (+ nós de interação)
│   │   │   │   ├── outbox/       #   - padrão Transactional Outbox
│   │   │   │   ├── payment/      #   - métodos de pagamento e payloads
│   │   │   │   └── subscription/ #   - planos de assinatura
│   │   │   └── AutosaveApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/postgres/migrations/   # Migrações Flyway (PostgreSQL)
│   │       ├── db/neo4j/migrations/      # Migrações Neo4j
│   │       └── templates/                # Templates de e-mail
│   └── test/                     # Testes unitários dos serviços
├── .github/workflows/            # Pipelines de CI (Maven, Sonar, Release)
├── .env.example
├── docker-compose-dev.yaml
├── pom.xml
└── README.md
```

Cada módulo de domínio segue, em geral, a mesma organização interna: `domain/entity`, `domain/enums`, `domain/repository`, `dto`, `service` (e, quando aplicável, `builder` e `service/events`).

---

## 📄 Licença

Este projeto está licenciado sob a MIT License.

Consulte o arquivo [LICENSE](https://github.com/ok-kioo/autosave-backend/blob/main/LICENSE) para obter o texto completo da licença.

---

## 🤝 Contribuindo

Contribuições são bem-vindas!

- siga o padrão descrito em [CONTRIBUTING.md](https://github.com/ok-kioo/autosave-backend/blob/main/CONTRIBUTING.md);
- utilize Conventional Commits;
- concorde com o licenciamento MIT para suas contribuições.

## Convenção de commits

Este projeto utiliza [Conventional Commits](https://www.conventionalcommits.org/).

---

## ⭐ Apoie o projeto

Se este projeto foi útil para você, considere:

- ⭐ Dar uma estrela no repositório;
- 🐛 Reportar problemas;
- 💡 Sugerir melhorias;
- 🤝 Contribuir com código;
- 📢 Compartilhar o projeto.

**Obrigado por apoiar o projeto! ❤️**

---

## 📞 Suporte

Encontrou um problema?

Abra uma Issue descrevendo:

1. O problema encontrado;
2. Como reproduzi-lo;
3. O comportamento esperado;
4. O comportamento atual;
5. Logs ou mensagens de erro;
6. Sistema operacional, versão do JDK e do Docker;
7. Versão do projeto.

---

## 📚 Documentação

A documentação do projeto pode ser organizada nos seguintes recursos:

- [Documentação principal (este README)](https://github.com/ok-kioo/autosave-backend/blob/main/README.md)
- Documentação interativa da API (Swagger UI), disponível em `/swagger-ui/index.html` após executar o projeto localmente

---

Desenvolvido com ❤️ pela equipe **ok-kioo**

[⭐ Star este projeto](https://github.com/ok-kioo/autosave-backend)