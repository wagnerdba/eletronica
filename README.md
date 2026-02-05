# Plataforma IoT para Coleta e Persistência de Dados Ambientais 

A arquitetura apresentada ilustra um sistema de aquisição, processamento e persistência de dados ambientais baseado em IoT, composto por sensor, microcontrolador, backend e banco de dados.

O sensor SHT45 é responsável pela medição de temperatura e umidade do ar. Esse sensor está conectado a um ESP32, que realiza a leitura periódica dos dados ambientais, além de manter informações internas como uptime e data/hora.

O ESP32 atua como um Web Server, expondo os dados coletados por meio de um endpoint HTTP no formato JSON. Esse endpoint é acessado via rede sem fio (Wi-Fi).

No backend, desenvolvido em Java, existe um job agendado que é executado a cada 1 minuto. Esse job realiza uma requisição HTTP GET ao ESP32 para obter os dados mais recentes do sensor. Após a recepção, o backend pode aplicar validações, normalizações e regras de negócio — incluindo mecanismos de fallback, caso o ESP32 esteja indisponível ou retorne dados inconsistentes.

Em seguida, os dados processados são enviados ao banco de dados por meio de uma requisição HTTP POST (ou operação equivalente de persistência). As informações armazenadas incluem:

	Temperatura (°C e °F)

	Umidade do ar

	Data e hora da medição

	UUID de identificação

	Uptime do ESP32

	Indicador de fallback

Esse fluxo garante a desacoplagem entre a camada de coleta (ESP32), a camada de processamento (backend Java) e a camada de persistência (banco de dados), permitindo maior escalabilidade, tolerância a falhas e facilidade de manutenção do sistema.

![Arquitetura IoT](PLATAFORMA_IOT_COLETA_PERSISTENCIA__DADOS_AMBIENTAIS_v001.2026.png)

## Estrutura do Projeto

- **1. Camada de Sensoriamento (Edge Layer)**

    O sensor SHT45 realiza medições de temperatura e umidade relativa do ar, oferecendo alta precisão e estabilidade térmica. Ele se comunica com o microcontrolador por meio de barramento I²C, com leituras periódicas controladas pelo firmware.
	 
	 O ESP32 atua como dispositivo de borda (edge device) e possui múltiplas responsabilidades:
	 
	 Aquisição dos dados do sensor

	 Gerenciamento de uptime

	 Sincronização de data e hora (tipicamente via NTP)

	 Disponibilização dos dados via API REST

	 Operação contínua com suporte a watchdog, prevenindo travamentos

- **2. Exposição dos Dados (Web Server embarcado)**

	O ESP32 executa um Web Server HTTP, expondo um endpoint do tipo GET, que retorna os dados no formato JSON, contendo:
	
		Temperatura em Celsius e Fahrenheit

		Umidade do ar

		Data e hora da medição

		Tempo de atividade do dispositivo (uptime)
	
	Essa abordagem elimina a necessidade de push contínuo, reduzindo consumo de energia e tráfego de rede, além de facilitar testes e diagnósticos manuais.

- **3. Comunicação em Rede**

	Toda a comunicação ocorre via Wi-Fi, utilizando o protocolo HTTP, o que garante:
	
		Simplicidade de integração
	
		Compatibilidade com firewalls e redes locais
	
		Facilidade de expansão para HTTPS, autenticação por token ou API Key

- **4. Camada de Backend (Processamento Central)**

	O backend Java funciona como o núcleo lógico do sistema. Um job agendado, executado a cada 1 minuto, realiza as seguintes etapas:
	
	Requisição HTTP GET ao ESP32
	
		Desserialização do JSON recebido
	
		Validação dos dados (faixa válida, valores nulos, consistência temporal)
	
		Aplicação de regras de negócio
	
		Mecanismo de Fallback
	
	Caso o ESP32 esteja indisponível ou retorne erro:
	
		O backend registra a falha
	
		Marca o registro com o indicador fallback

	Opcionalmente reutiliza o último valor válido conhecido

	Esse mecanismo garante continuidade histórica dos dados, mesmo em cenários de falha intermitente do dispositivo.

- **5. Persistência de Dados (Data Layer)**

	Após o processamento, o backend realiza a persistência no banco de dados, via POST ou acesso direto ao repositório. Cada registro armazena:

		UUID único para rastreabilidade

		Temperatura (°C e °F)

		Umidade do ar

		Data/hora normalizada

		Uptime do ESP32

		Flag de fallback

	Esse modelo favorece:

	Auditoria de dados

	Análises históricas

	Identificação de falhas de comunicação ou hardware

- **6. Boas práticas e pontos fortes da arquitetura**

	✔ Desacoplamento total entre hardware e persistência
	
	✔ Backend como orquestrador, evitando lógica complexa no ESP32
	
	✔ Escalável: múltiplos ESP32 podem ser integrados facilmente
	
	✔ Observabilidade: uptime e fallback ajudam no diagnóstico
	
	✔ Tolerância a falhas com watchdog e fallback lógico
	
	✔ Extensível: fácil inclusão de novos sensores ou métricas
	
	✔ Pronto para dashboards, alertas e integração com sistemas externos

## Possíveis evoluções naturais

	Uso de HTTPS + autenticação
	
	Cache local no ESP32 para o caso de falha

	Envio assíncrono via MQTT

	Alertas automáticos (ex: temperatura fora do limite)

	Monitoramento de saúde do dispositivo (health check)

## Contribuições

1. Faça um fork do projeto.
2. Crie uma nova branch: `git checkout -b minha-nova-feature`.
3. Faça suas alterações e commite elas: `git commit -m 'Adiciona nova feature'`.
4. Envie para o branch original: `git push origin minha-nova-feature`.
5. Crie um pull request.

## Licença

Este projeto está licenciado sob a Licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## Contato

Para mais informações ou suporte, entre em contato com [wagnerdba@gmail.com].
