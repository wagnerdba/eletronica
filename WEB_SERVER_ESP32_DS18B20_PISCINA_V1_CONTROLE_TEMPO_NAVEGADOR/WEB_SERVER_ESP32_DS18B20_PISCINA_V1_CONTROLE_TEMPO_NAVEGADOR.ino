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
IPAddress local_IP(192,168,1,104);
IPAddress gateway(192,168,1,1);
IPAddress subnet(255,255,255,0);
IPAddress primaryDNS(8,8,8,8);
IPAddress secondaryDNS(8,8,4,4);

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);
WebServer server(80);

float temperatura = 0.0;

// ========================= HTML =========================
const char HTML_PAGE[] PROGMEM = R"rawliteral(
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Piscina</title>

<style>
*{margin:0;padding:0;box-sizing:border-box;}

body{
  min-height:100vh;
  display:flex;
  justify-content:center;
  align-items:center;
  font-family:Arial,Helvetica,sans-serif;
  background:linear-gradient(135deg,#e0f4ff,#bfe8ff);
  padding:20px;
}

.card{
  background:rgba(255,255,255,0.95);
  border-radius:32px;
  padding:clamp(30px,4vw,60px);
  width:90%;
  max-width:680px;
  text-align:center;
  box-shadow:
    0 25px 70px rgba(0,120,200,0.25),
    0 5px 20px rgba(0,0,0,0.05);
}

.titulo{
  font-size:clamp(32px,5vw,56px);
  font-weight:800;
  color:#0b72f8;
  text-shadow:0 4px 15px rgba(0,120,255,0.25);
  margin-bottom:8px;
}

.subtitulo{
  font-size:clamp(16px,2vw,22px);
  color:#147df5;
  margin-bottom:35px;
  opacity:0.9;
}

.temp-wrapper{
  display:flex;
  justify-content:center;
  align-items:flex-start;
  gap:8px;
}

.temperatura{
  font-size:clamp(70px,10vw,140px);
  font-weight:900;
  color:#003c91;
  line-height:1;
  text-shadow:
    0 8px 25px rgba(0,120,255,0.25),
    0 0 40px rgba(0,180,255,0.3);
}

.unidade{
  font-size:clamp(24px,3vw,45px);
  font-weight:bold;
  color:#005eff;
  margin-top:12px;
}

.subinfo{
  margin-top:25px;
  font-size:clamp(14px,2vw,18px);
  color:#004ea8;
  opacity:0.8;
}

@media (max-height:500px){
  .card{
    padding:25px 20px;
  }
  .temperatura{
    font-size:clamp(60px,12vh,110px);
  }
}
</style>
</head>
<body>

<div class="card">
  <div class="titulo">🌊 PISCINA</div>
  <div class="subtitulo">Temperatura da água</div>

  <div class="temp-wrapper">
    <div class="temperatura" id="temp">--</div>
    <div class="unidade">°C</div>
  </div>

  <div class="subinfo" id="contador">5s</div>
</div>

<script>
const intervaloAtualizacao = 5;
let segundosRestantes = intervaloAtualizacao;

const tempEl = document.getElementById("temp");
const contadorEl = document.getElementById("contador");

async function buscarTemperatura(){
  try{
    const response = await fetch("/temperatura",{cache:"no-store"});
    const json = await response.json();
    tempEl.textContent = json.temperatura_celsius.toFixed(2);
  }catch(e){
    tempEl.textContent="--";
  }
}

function atualizarContador(){

  contadorEl.textContent = segundosRestantes + "s";

  if(segundosRestantes === 0){
    buscarTemperatura();
    segundosRestantes = intervaloAtualizacao;
  } else {
    segundosRestantes--;
  }
}
setInterval(atualizarContador,1000);
</script>

</body>
</html>
)rawliteral";
// ========================================================

void handleRoot(){
  server.send_P(200,"text/html",HTML_PAGE);
  Serial.println("200 OK");
}

void handleTemperatura(){
  sensors.requestTemperatures();
  temperatura = sensors.getTempCByIndex(0);

  String json = "{\"temperatura_celsius\":";
  json += String(temperatura,2);
  json += "}";

  server.send(200,"application/json",json);
}

void setup(){
  Serial.begin(115200);
  sensors.begin();

  WiFi.mode(WIFI_STA);
  WiFi.setHostname("ESP32-Piscina");

  WiFi.config(local_IP,gateway,subnet,primaryDNS,secondaryDNS);
  WiFi.begin(ssid,password);

  while(WiFi.status()!=WL_CONNECTED){
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nConectado!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  if(MDNS.begin("piscina")){
    Serial.println("mDNS ativo: http://piscina.local");
  }

  server.on("/",handleRoot);
  server.on("/temperatura",handleTemperatura);
  server.begin();
}

void loop(){
  server.handleClient();
}