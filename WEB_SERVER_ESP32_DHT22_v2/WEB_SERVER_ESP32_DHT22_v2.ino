#include <WiFi.h>
#include <DHT.h>
#include <ESPAsyncWebServer.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <esp_task_wdt.h>

//----------------------------------
// Definir credenciais Wi-Fi
//----------------------------------
const char* ssid = "GABRIEL_HOME";
const char* password = "@FlakE2021#";

//----------------------------------
// Configurar IP estático
//----------------------------------
IPAddress local_IP(192, 168, 1, 101);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);
IPAddress primaryDNS(8, 8, 8, 8);
IPAddress secondaryDNS(8, 8, 4, 4);

//----------------------------------
// Configurar sensor DHT
//----------------------------------
#define DHTPIN 18
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

//----------------------------------
// Configurar porta http
//----------------------------------
AsyncWebServer server(80);

//--------------------------------------------------------------
// Configurar URL do servidor de aplicação (java backend)
//--------------------------------------------------------------
const char* postServerUrl = "http://192.168.1.7:8081/api/dht22/post_data";
const char* timeServerUrl = "http://192.168.1.7:8081/api/dht22/now";

//-------------------------------------------------
// Variável para armazenar o minuto do último envio
//-------------------------------------------------
int lastMinuteSent = -1;

//------------------------------------------------
// declaração da função de persistência da Wifi
//------------------------------------------------
void connectWiFi();  

//------------------------------------------------
// Função para obter a data e hora atual
//------------------------------------------------
String getCurrentDateTime(int attempts = 3) {
  HTTPClient http;
  String dateTime = "Erro ao obter data e hora";

  http.begin(timeServerUrl);
  while (attempts-- > 0) {
    int httpResponseCode = http.GET();
    if (httpResponseCode == 200) {
      String payload = http.getString();
      JsonDocument jsonDoc;
      deserializeJson(jsonDoc, payload);
      dateTime = jsonDoc["data_hora"].as<String>();
      break;
    } else {
      delay(1000);
    }
  }
  http.end();
  return dateTime;
}
//-----------------------------------------------------------
// Função para enviar o POST ENDPOINT (persistir os dados)
//-----------------------------------------------------------
void sendPostRequest(float temperatureCelsius, float temperatureFahrenheit, float humidity, String dateTime, int attempts = 3) {
  while (attempts-- > 0) {
    if (WiFi.status() == WL_CONNECTED) {
      HTTPClient http;
      http.begin(postServerUrl);
      http.addHeader("Content-Type", "application/json");

      JsonDocument jsonDoc;
      jsonDoc["temperatura_celsius"] = temperatureCelsius;
      jsonDoc["temperatura_fahrenheit"] = temperatureFahrenheit;
      jsonDoc["umidade"] = humidity;
      jsonDoc["data_hora"] = dateTime;

      String jsonString;
      serializeJson(jsonDoc, jsonString);

      int httpResponseCode = http.POST(jsonString);

      // ➤ TRATAR ERRO -11 AQUI
      if (httpResponseCode == -11) {
        Serial.println("Servidor recusou a conexão (-11). Tentando novamente...");
        http.end();
        delay(1500);
        continue; // <-- volta ao while e tenta novamente
      }

      // SUCESSO REAL = 200 ~ 299
      if (httpResponseCode >= 200 && httpResponseCode < 300) {
        String response = http.getString();
        Serial.println("Requisição Enviada: " + response);
        http.end();
        return;
      } else {
        Serial.println("Falha ao enviar os dados: " + String(httpResponseCode));
      }
      http.end();
    } else {
      Serial.println("Conexão Wi-Fi perdida...");
    }
    delay(2000);
  }
  Serial.println("Falha ao enviar os dados após várias tentativas.");
}

//------------------------------------------------
// Função para tentar ler o sensor várias (3x)
//------------------------------------------------
bool tryReadSensor(float& temperatureCelsius, float& temperatureFahrenheit, float& humidity, bool origem, int attempts = 3) {
  while (attempts-- > 0) {
    temperatureCelsius = dht.readTemperature();
    temperatureFahrenheit = dht.readTemperature(true);
    humidity = dht.readHumidity();

    if (temperatureCelsius == 0 || humidity == 0) {
      Serial.println("Leitura do sensor DHT22 zerada. Executando nova tentativa de leitura... ");
      delay(2000);
      temperatureCelsius = dht.readTemperature();
      temperatureFahrenheit = dht.readTemperature(true);
      humidity = dht.readHumidity();
    }

    if (!isnan(temperatureCelsius) && !isnan(humidity)) {
      return true;
    }

    if (origem) {
      Serial.println("ROTINA - Falha ao ler do sensor DHT22... Tentativas restantes: " + String(attempts));
    } else {
      Serial.println("ENDPOINT - Falha ao ler do sensor DHT22... Tentativas restantes: " + String(attempts));
    }

	 Serial.println("Nova tentativa em 3s...");
    delay(3000);
  }
  return false;
}

//------------------------------------------------
// Setup
//------------------------------------------------
void setup() {
  Serial.begin(115200);
  dht.begin();

// ---------- WATCHDOG ----------
// Configuração do Watchdog
/*
  esp_task_wdt_config_t wdt_config = {
    .timeout_ms = 480000, // 480 segundos - 8 minutos de inatividade o esp32 é reiniciado pelo watchdog
  };
  esp_task_wdt_init(&wdt_config);
*/
// versao mais antiga tirar o comentário e comentar o trecho acima ou vice-versa
  esp_task_wdt_init(480, true); // timeout em segundos, panic=true
  
  esp_task_wdt_add(NULL);        // adiciona a task principal (loop) ao WDT
// ---------- WATCHDOG ----------

  if (!WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS)) {
    Serial.println("Configuração de IP falhou.");
  }

  WiFi.mode(WIFI_STA);
  WiFi.setHostname("ESP32Server");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Conectando-se ao WiFi...");
  }
  Serial.println("Conectado ao WiFi!");
  Serial.println("Endereço IP: " + WiFi.localIP().toString());
  Serial.println("Hostname: " + String(WiFi.getHostname()));

// ---------- GET ENDPOINT ----------
  server.on("/esp32/api/temperatura", HTTP_GET, [](AsyncWebServerRequest* request) {
    float temperatureCelsius, temperatureFahrenheit, humidity;

    if (tryReadSensor(temperatureCelsius, temperatureFahrenheit, humidity, false)) {
      String dateTime = getCurrentDateTime();

      JsonDocument jsonDoc;
      jsonDoc["temperatura_celsius"] = temperatureCelsius;
      jsonDoc["temperatura_fahrenheit"] = temperatureFahrenheit;
      jsonDoc["umidade"] = humidity;
      jsonDoc["data_hora"] = dateTime;

      String jsonString;
      serializeJson(jsonDoc, jsonString);

      AsyncResponseStream* response = request->beginResponseStream("application/json");
      response->print(jsonString);
      response->addHeader("Access-Control-Allow-Origin", "*");
      response->addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
      response->addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
      request->send(response);
    }
  });
  // ---------- GET ENDPOINT ----------

  server.onNotFound([](AsyncWebServerRequest* request) {
    request->send(404, "text/plain", "Página não encontrada.");
  });

  server.begin();

// -----------------------------------------------------
// Criar task de envio dos dados (POST) a cada minuto
// -----------------------------------------------------
xTaskCreate(
  [](void*) {

    TickType_t xLastWakeTime = xTaskGetTickCount();
    const TickType_t xFrequency = 60000 / portTICK_PERIOD_MS; // 60s = a task é executada uma vez por minuto

    for (;;) {
      // Garantir Wi-Fi conectado
      if (WiFi.status() != WL_CONNECTED) {
        Serial.println("Wi-Fi fora do ar, reconectando...");
        connectWiFi();
        delay(1000);
      }

      // Obter data/hora
      String dateTime = getCurrentDateTime();

      // Obter dados do sensor
      float temperatureCelsius, temperatureFahrenheit, humidity;
      if (tryReadSensor(temperatureCelsius, temperatureFahrenheit, humidity, true)) {
        sendPostRequest(temperatureCelsius, temperatureFahrenheit, humidity, dateTime);
      } else {
        Serial.println("Falha ao ler do sensor DHT22. Tentativas esgotadas.");
      }

      // Aguarda exatamente 60s desde a última execução
      vTaskDelayUntil(&xLastWakeTime, xFrequency);
    }
  },
  "PostTask",  // nome da task
  4096,        // tamanho de stack
  NULL,
  1,           // prioridade
  NULL
);
}

// -------------------
// WI-FI resiliente
// -------------------
void connectWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;

  WiFi.disconnect(true);
  WiFi.mode(WIFI_STA);
  WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS);
  WiFi.setHostname("ESP32Server");
  WiFi.begin(ssid, password);

  Serial.println("Conectando-se ao WiFi...");
  unsigned long startAttemptTime = millis();
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);

    if (millis() - startAttemptTime > 30000) {
      Serial.println("Falha ao conectar Wi-Fi, resetando...");
      ESP.restart();
    }
  }

  Serial.println("\nWi-Fi conectado!");
  Serial.println("Endereço IP: " + WiFi.localIP().toString());
}

void loop() {
  // ---------- Apenas reset do watchdog e execução do servidor ----------
  esp_task_wdt_reset();
  delay(200);
}
