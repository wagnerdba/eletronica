# Sensor DHT22 ClimaTempo

Este projeto é uma aplicação backend em Java que utiliza o sensor DHT22 para coletar dados meteorológicos. A aplicação é desenvolvida usando as tecnologias Jakarta EE, Spring Boot, Spring Data JPA e Spring MVC. O projeto também utiliza o Lombok para simplificação do código.

## Estrutura do Projeto

- **Configurações do Spring MVC:**
    - A classe `WebConfig` configurada permite CORS para todos os endpoints.
- **Controladores:**
    - `SensorController`: Responsável por gerenciar as requisições relacionadas ao sensor DHT22.
- **DTOs:**
    - `SensorDataCountDTO`, `SensorDataCurrentDateTestDTO`, `SensorDataDTO`, `SensorDataHoraDTO`, `SensorDataStatisticsDTO`: Representações de dados transferidos entre client e servidor.
- **Mapper:**
    - `SensorDataMapper`: Mapeamento entre entidades e DTOs.
- **Modelo:**
    - `SensorData`: Entidade que representa os dados do sensor.
- **Repositórios:**
    - `SensorDataRepository` e `SensorDataRepositoryCustom`: Interfaces de acesso aos dados armazenados.
- **Serviços:**
    - `SensorService`: Lógica de negócios sobre os dados do sensor.

## Pré-requisitos

- Java 21
- Maven 3.x
- Docker (opcional, para execução em container)

## Configuração

### Banco de Dados

O projeto inclui um script SQL `DDL.sql` para criar as tabelas necessárias no banco de dados.

### Configuração do Spring

O arquivo `application.properties` contém as configurações do banco de dados e outras propriedades do Spring Boot.

### Docker

Você pode construir a imagem Docker da aplicação usando o Dockerfile incluído no projeto.

```bash
docker build -t sensor-dht22 .
```

## Compilação e Execução

Você pode compilar e executar a aplicação usando Maven:

```bash
mvn clean install
mvn spring-boot:run
```

Ou usar o script `start-app.sh` para iniciar a aplicação:

```bash
./start-app.sh
```

## Endpoints

- **GET** `/sensor/data/count` : Retorna a contagem de registros de dados do sensor.
- **GET** `/sensor/data/current` : Retorna os dados atuais do sensor.
- **POST** `/sensor/data` : Adiciona um novo registro de dados do sensor.

## Testes

Os testes unitários e de integração estão incluídos no projeto. As classes de teste estão localizadas no diretório `src/test`.

Você pode executar os testes usando Maven:

```bash
mvn test
```

## Contribuições

1. Faça um fork do projeto.
2. Crie uma nova branch: `git checkout -b minha-nova-feature`.
3. Faça suas alterações e commite elas: `git commit -m 'Adiciona nova feature'`.
4. Envie para o branch original: `git push origin minha-nova-feature`.
5. Crie um pull request.

## Licença

Este projeto está licenciado sob a Licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## Contato

Para mais informações ou suporte, entre em contato com [seu-email@dominio.com].