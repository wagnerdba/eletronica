#include <WiFi.h>
#include <DHT.h>
#include <ESPAsyncWebServer.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// Definir credenciais Wi-Fi
const char* ssid = "GABRIEL_HOME";
const char* password = "@FlakE2021#";

// Configurar IP estático
IPAddress local_IP(192, 168, 1, 100);  // Endereço IP desejado para o ESP32
IPAddress gateway(192, 168, 1, 1);     // Gateway (normalmente o IP do roteador)
IPAddress subnet(255, 255, 255, 0);    // Máscara de sub-rede
IPAddress primaryDNS(8, 8, 8, 8);      // Servidor DNS primário (opcional)
IPAddress secondaryDNS(8, 8, 4, 4);    // Servidor DNS secundário (opcional)

// Configurar DHT
#define DHTPIN 18      // Pino onde o DHT22 está conectado
#define DHTTYPE DHT22  // Definindo o tipo de sensor como DHT22
DHT dht(DHTPIN, DHTTYPE);

// Configurar porta
AsyncWebServer server(80);

// Configurar URL do servidor externo destino dos dados
const char* postServerUrl = "http://192.168.1.14:8081/api/dht22/post_data";
const char* timeServerUrl = "http://192.168.1.14:8081/api/dht22/now"; // Substitua pelo seu endpoint que retorna a data e hora

// Criar variável para armazenar o tempo do último envio de dados
unsigned long lastPostTime = 0;

// Função para obter a data e hora atual
String getCurrentDateTime() {
    HTTPClient http;
    http.begin(timeServerUrl);
    int httpResponseCode = http.GET();

    if (httpResponseCode == 200) {
        String payload = http.getString();
        DynamicJsonDocument jsonDoc(1024);
        deserializeJson(jsonDoc, payload);
        return jsonDoc["data_hora"].as<String>(); // Acesse o campo de data e hora
    } else {
        Serial.println("Falha ao obter data e hora do servidor: " + String(httpResponseCode));
        return "Erro ao obter data e hora";
    }
}

void setup() {
    // Iniciar comunicação serial
    Serial.begin(115200);

    // Iniciar sensor DHT
    dht.begin();

    // Conectar a rede Wi-Fi
    if (!WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS)) {
        Serial.println("Configuração de IP falhou.");
    }

    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.println("Conectando-se ao WiFi...");
    }
    Serial.println("Conectado ao WiFi!");
    Serial.print("Endereço IP: ");
    Serial.println(WiFi.localIP());

    // Configurar o endpoint "/esp32/api/temperatura" para GET
    server.on("/esp32/api/temperatura", HTTP_GET, [](AsyncWebServerRequest* request) {
        float temperatureCelsius = dht.readTemperature();         // Temperatura em Celsius
        float temperatureFahrenheit = dht.readTemperature(true);  // Temperatura em Fahrenheit
        float humidity = dht.readHumidity();

        // Verificar se a leitura falhou
        if (isnan(temperatureCelsius) || isnan(humidity)) {
            request->send(500, "application/json", "{\"erro\": \"Falha ao ler do sensor DHT22\"}");
            return;
        }

        // Obter data e hora atual do endpoint
        String dateTime = getCurrentDateTime();

        // Criar objeto JSON com os dados
        DynamicJsonDocument jsonDoc(1024);
        jsonDoc["temperatura_celsius"] = temperatureCelsius;
        jsonDoc["temperatura_fahrenheit"] = temperatureFahrenheit;
        jsonDoc["umidade"] = humidity;
        jsonDoc["data_hora"] = dateTime;  // Adicionando data e hora

        String jsonString;
        serializeJson(jsonDoc, jsonString);

        // Adicionar cabeçalhos CORS
        AsyncResponseStream *response = request->beginResponseStream("application/json");
        response->print(jsonString);
        response->addHeader("Access-Control-Allow-Origin", "*");  // Permite acesso de qualquer origem
        response->addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response->addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        request->send(response);
    });

    // Configurar endpoint para permitir CORS
    server.onNotFound([](AsyncWebServerRequest* request) {
        AsyncResponseStream *response = request->beginResponseStream("text/plain");
        response->print("Página não encontrada.");
        response->addHeader("Access-Control-Allow-Origin", "*");  // Permite acesso de qualquer origem
        request->send(response);
    });

    // Iniciar servidor
    server.begin();
}

void loop() {
    // Verificar se passou um minuto desde o último envio
    unsigned long currentMillis = millis();
    
    if (currentMillis - lastPostTime >= 60000) {  // 60000 ms = 1 minuto
        // Atualizar o tempo do último envio
        lastPostTime = currentMillis;

        // Obter os dados do sensor
        float temperatureCelsius = dht.readTemperature();         // Temperatura em Celsius
        float temperatureFahrenheit = dht.readTemperature(true);  // Temperatura em Fahrenheit
        float humidity = dht.readHumidity();

        // Verificar se a leitura falhou
        if (isnan(temperatureCelsius) || isnan(humidity)) {
            Serial.println("Falha ao ler do sensor DHT22");
            return;
        }

        // Obter data e hora atual do endpoint
        String dateTime = getCurrentDateTime();

        // Enviar dados para o servidor externo via POST
        sendPostRequest(temperatureCelsius, temperatureFahrenheit, humidity, dateTime);
    }
}

void sendPostRequest(float temperatureCelsius, float temperatureFahrenheit, float humidity, String dateTime) {
    if (WiFi.status() == WL_CONNECTED) {
        HTTPClient http;
        http.begin(postServerUrl);
        http.addHeader("Content-Type", "application/json");

        // Criando um objeto JSON com os dados
        DynamicJsonDocument jsonDoc(1024);
        jsonDoc["temperatura_celsius"] = temperatureCelsius;
        jsonDoc["temperatura_fahrenheit"] = temperatureFahrenheit;
        jsonDoc["umidade"] = humidity;
        jsonDoc["data_hora"] = dateTime;

        String jsonString;
        serializeJson(jsonDoc, jsonString);

        // Executar o POST
        int httpResponseCode = http.POST(jsonString);

        if (httpResponseCode > 0) {
            // Adicionando mensagem de sucesso
            String response = http.getString();
            Serial.print("Requisição Enviada: ");
            Serial.println(response);

        } else {
            // Adicionar mensagem de erro
            Serial.print("Falha ao enviar os dados. Verifique a conexão ou o servidor...");
            Serial.println(httpResponseCode);
        }

        http.end();
    } else {
        Serial.println("Conexão Wi-Fi perdida...");
    }
}
