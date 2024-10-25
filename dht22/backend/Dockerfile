# Primeiro estágio: Construção da aplicação
FROM maven:3-amazoncorretto-21-alpine AS build

# Definir e sincronizar fuso horário com o host
RUN apk add --no-cache tzdata
VOLUME ["/etc/localtime:/etc/localtime:ro", "/etc/timezone:/etc/timezone:ro"]

# Copiar os arquivos de código-fonte e o pom.xml para o contêiner
COPY src /app/src
COPY pom.xml /app

# Definir o diretório de trabalho
WORKDIR /app

# Executar a compilação do Maven
RUN mvn clean install

# Segundo estágio: Execução da aplicação
FROM openjdk:21-jdk-slim

# Atualizar pacotes e instalar bash e tzdata
RUN apt-get update && apt-get install -y bash tzdata bsdmainutils && apt-get clean

# Definir o diretório de trabalho
WORKDIR /app

# Definir o fuso horário para o horário de Brasília
RUN ln -sf /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime && echo "America/Sao_Paulo" > /etc/timezone

# Expor a porta 8080
EXPOSE 8080

# Copiar o JAR gerado no estágio de construção para o segundo estágio
COPY --from=build /app/target/climatempo-1.0.1.jar /app/app.jar

# Adicionar um script de inicialização (opcional)
COPY start-app.sh /app/start-app.sh

# Tornar o script executável (opcional)
RUN chmod +x /app/start-app.sh

# Definir o bash como o ponto de entrada
ENTRYPOINT ["/bin/bash"]

# Comando padrão para rodar a aplicação quando o contêiner for iniciado
#ENTRYPOINT ["java", "-jar", "app.jar"]
# Definir o script de inicialização como o ponto de entrada
#ENTRYPOINT ["sh", "/app/start-app.sh"]