#include <WiFi.h>
#include <WebServer.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <ESPmDNS.h>

#define ONE_WIRE_BUS 4

// ===== WIFI =====
const char* ssid = "GABRIEL_HOME";
const char* password = "@FlakE2021#";

// ===== IP FIXO =====
IPAddress local_IP(192, 168, 1, 104);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);
IPAddress primaryDNS(8, 8, 8, 8);
IPAddress secondaryDNS(8, 8, 4, 4);

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);
WebServer server(80);

float temperatura = 0.0;

// ===== HTML =====
const char HTML_PAGE[] PROGMEM = R"rawliteral(
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Temperatura da Piscina</title>

<link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@600;800&display=swap" rel="stylesheet">

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }

body {
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    font-family: 'Orbitron', sans-serif;
    background: url('https://images.unsplash.com/photo-1575429198097-0414ec08e8cd?q=80&w=2000&auto=format&fit=crop') no-repeat center center/cover;
    position: relative;
    overflow: hidden;
    padding: 20px;
}

body::before {
    content: "";
    position: absolute;
    inset: 0;
    background: rgba(0, 120, 180, 0.35);
    backdrop-filter: blur(4px);
}

.card {
    position: relative;
    background: rgba(5, 171, 236, 0.85);
    backdrop-filter: blur(15px);
    border-radius: 35px;
    padding: clamp(30px, 4vw, 100px);
    width: min(90vw, 1200px);
    text-align: center;
    box-shadow:
        0 30px 80px rgba(0, 140, 255, 0.4),
        inset 0 0 25px rgba(255,255,255,0.6);
    border: 1px solid rgba(255,255,255,0.9);
}

.titulo {
    font-size: clamp(32px, 6vw, 80px);
    letter-spacing: 4px;
    color: #0b72f8;
    -webkit-text-stroke: 0.5px #000000;
    margin-bottom: clamp(10px, 2vw, 30px);
}

.subtitulo {
    font-size: clamp(16px, 2.5vw, 40px);
    letter-spacing: 3px;
    color: #0b72f8;
    -webkit-text-stroke: 0.5px #000000;
    margin-bottom: clamp(20px, 3vw, 40px);
}

.temperatura {
    font-size: clamp(70px, 10vw, 170px);
    font-weight: 800;
    color: #0c09ad;
    -webkit-text-stroke: 2px #ffffff;
    text-shadow:
        0 5px 20px rgba(0,150,255,0.6),
        0 0 50px rgba(0,200,255,0.5);
}

.unidade {
    font-size: clamp(25px, 3vw, 60px);
    vertical-align: super;
}

.subinfo {
    margin-top: clamp(15px, 2vw, 30px);
    font-size: clamp(8px, 2vw, 20px);
    color: #0c09ad;
}
</style>
</head>
<body>

<div class="card">
    <div class="titulo">🌊 PISCINA</div>
    <div class="subtitulo">MONITORAMENTO DA TEMPERATURA<br>DA ÁGUA EM TEMPO REAL</div>
    <div class="temperatura" id="temp">--<span class="unidade">°C</span></div>
    <div class="subinfo" id="contador">15s</div>
</div>

<script>
const intervaloAtualizacao = 15;
let segundosRestantes = intervaloAtualizacao;

const tempEl = document.getElementById("temp");
const contadorEl = document.getElementById("contador");

async function buscarTemperatura() {
    try {
        const response = await fetch("/temperatura");
        const json = await response.json();

        tempEl.innerHTML =
            json.temperatura_celsius.toFixed(2) +
            '<span class="unidade">°C</span>';

    } catch (erro) {
        tempEl.innerHTML =
            "--<span class='unidade'>°C</span>";
    }

    segundosRestantes = intervaloAtualizacao;
}

function atualizarContador() {
    contadorEl.textContent = segundosRestantes + "s";
    segundosRestantes--;

    if (segundosRestantes < 0) {
        buscarTemperatura();
    }
}

buscarTemperatura();
setInterval(atualizarContador, 1000);
</script>

</body>
</html>
)rawliteral";

// ===== ROTAS =====
void handleRoot() {
  server.send_P(200, "text/html", HTML_PAGE);
}

void handleTemperatura() {
  sensors.requestTemperatures();
  temperatura = sensors.getTempCByIndex(0);

  String json = "{";
  json += "\"temperatura_celsius\":";
  json += String(temperatura, 2);
  json += "}";

  server.send(200, "application/json", json);
}

void setup() {
  Serial.begin(115200);

  sensors.begin();

  WiFi.mode(WIFI_STA);
  WiFi.setHostname("ESP32-Piscina");

  if (!WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS)) {
    Serial.println("Falha ao configurar IP fixo");
  }

  WiFi.begin(ssid, password);

  Serial.print("Conectando");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nConectado!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  if (MDNS.begin("piscina")) {
    Serial.println("mDNS iniciado: http://piscina.local");
  }

  server.on("/", handleRoot);
  server.on("/temperatura", HTTP_GET, handleTemperatura);
  server.begin();
}

void loop() {
  server.handleClient();
}