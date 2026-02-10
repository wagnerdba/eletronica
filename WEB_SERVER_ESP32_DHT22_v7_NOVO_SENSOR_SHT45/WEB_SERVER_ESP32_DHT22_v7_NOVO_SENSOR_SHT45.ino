#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <esp_task_wdt.h>
#include <esp_timer.h>
#include <Wire.h>
#include <Adafruit_SHT4x.h>

#define SCL_PIN 21
#define SDA_PIN 22

Adafruit_SHT4x sht4 = Adafruit_SHT4x();

//----------------------------------
// Definir credenciais Wi-Fi
//----------------------------------
const char *ssid = "GABRIEL_HOME";
const char *password = "@FlakE2021#";

//----------------------------------
// Configurar IP estático
//----------------------------------
IPAddress local_IP(192, 168, 1, 101);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);
IPAddress primaryDNS(8, 8, 8, 8);
IPAddress secondaryDNS(8, 8, 4, 4);

//----------------------------------
// Configurar porta http
//----------------------------------
AsyncWebServer server(80);

//-------------------------------------------------
// Declaração antecipada
//------------------------------------------------
void connectWiFi();

//------------------------------------------------
String getCurrentDateTime(int attempts = 4)
{
  String dateTime = "❌ Erro ao obter data e hora...";

  while (attempts-- > 0)
  {
    struct tm timeinfo;
    if (getLocalTime(&timeinfo))
    {
      char buffer[20];
      strftime(buffer, sizeof(buffer), "%Y-%m-%d %H:%M:%S", &timeinfo);
      dateTime = String(buffer);
      break;
    }
    delay(1000);
  }
  return dateTime;
}

//------------------------------------------------
bool tryReadSensor(float &temperatureCelsius, float &temperatureFahrenheit, float &humidity, bool origem, int attempts = 4)
{
  while (attempts-- > 0)
  {
    sensors_event_t humidityEvent, tempEvent;
    sht4.getEvent(&humidityEvent, &tempEvent);

    temperatureCelsius = tempEvent.temperature;
    humidity = humidityEvent.relative_humidity;

    if (isnan(temperatureCelsius) || isnan(humidity)) {
      if (origem)
        Serial.println("❌ Falha ao ler SHT45... Tentativas restantes: " + String(attempts));
      delay(2000);
      continue;
    }

    temperatureFahrenheit = temperatureCelsius * 1.8 + 32.0;
    return true;
  }

  return false;
}

//------------------------------------------------
String getUptime() {
  uint64_t us = esp_timer_get_time();
  uint64_t s = us / 1000000;

  uint32_t sec = s % 60;
  uint32_t min = (s / 60) % 60;
  uint32_t hr  = (s / 3600) % 24;
  uint32_t days = s / 86400;

  char buffer[32];
  sprintf(buffer, "%u:%02u:%02u:%02u", days, hr, min, sec);
  return String(buffer);
}

//------------------------------------------------
void setup()
{
  Serial.begin(115200);

  Wire.begin(SDA_PIN, SCL_PIN);

  if (!sht4.begin()) {
    Serial.println("❌ SHT45 não encontrado");
    while (1);
  }

  sht4.setPrecision(SHT4X_HIGH_PRECISION);
  sht4.setHeater(SHT4X_NO_HEATER);

  Serial.println("✅ SHT45 iniciado");

  // ---------- WATCHDOG ----------
 
  esp_task_wdt_config_t wdt_config = {
    .timeout_ms = 240000,
  };

  /* esp_task_wdt_init(&wdt_config); */
  
  esp_task_wdt_add(NULL);

  connectWiFi();

  server.on("/esp32/api/temperatura", HTTP_GET, [](AsyncWebServerRequest *request) {
    float temperatureCelsius, temperatureFahrenheit, humidity;

    if (tryReadSensor(temperatureCelsius, temperatureFahrenheit, humidity, false)) {
      String dateTime = getCurrentDateTime();
      String upTime = getUptime();
      String sensorIp = WiFi.localIP().toString();

      Serial.println("✅ [ESP32] Dados coletados com sucesso");
      Serial.print("  Data/Hora: "); Serial.println(dateTime);
      Serial.print("  Temperatura (Cº): "); Serial.println(temperatureCelsius);
      Serial.print("  Temperatura (Fº): "); Serial.println(temperatureFahrenheit);
      Serial.print("  Umidade (%): "); Serial.println(humidity);
		  Serial.print("  Uptime: ");  Serial.println(upTime);
      Serial.print("  IP Sensor: "); Serial.println(sensorIp);
      
      JsonDocument jsonDoc;
      jsonDoc["temperatura_celsius"] = temperatureCelsius;
      jsonDoc["temperatura_fahrenheit"] = temperatureFahrenheit;
      jsonDoc["umidade"] = humidity;
      jsonDoc["data_hora"] = dateTime;
      jsonDoc["uptime"] = upTime;
      jsonDoc["sensor_ip"] = sensorIp;

      String jsonString;
      serializeJson(jsonDoc, jsonString);

      AsyncResponseStream *response = request->beginResponseStream("application/json");
      response->print(jsonString);
      response->addHeader("Access-Control-Allow-Origin", "*");
      response->addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
      response->addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
      request->send(response);
    }
  });

  server.onNotFound([](AsyncWebServerRequest *request) {
    request->send(404, "text/plain", "❌ Página não encontrada...");
  });

  server.begin();

  configTime(-3 * 3600, 3600, "pool.ntp.org", "time.nist.gov");
}

// -------------------
// Wi-Fi resiliente
// -------------------
void connectWiFi()
{
  if (WiFi.status() == WL_CONNECTED)
    return;

  WiFi.persistent(true);
  WiFi.setAutoReconnect(true);

  if (!WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS))
    Serial.println("❌ Falha ao configurar IP...");

  WiFi.mode(WIFI_STA);
  WiFi.setHostname("ESP32WEBSERVER");
  WiFi.begin(ssid, password);
  
  Serial.println();
  Serial.println("⌛ Conectando-se à rede");
  unsigned long startAttemptTime = millis();

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    if (millis() - startAttemptTime > 30000)
    {
      Serial.println("❌ WiFi não conectou — Reiniciando ESP...");
      ESP.restart();
    }
  }
  
  Serial.println("🌐 Conexão estabelecida");
	Serial.println("🧿 Endereço IP: " + WiFi.localIP().toString());
	Serial.println("🛜 Hostname: " + String(WiFi.getHostname()));
}

//------------------------------------------------
void loop()
{
  esp_task_wdt_reset();
  connectWiFi();
  delay(200);
}
