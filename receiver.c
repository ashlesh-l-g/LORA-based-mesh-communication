#include <SPI.h>
#include <LoRa.h>

#define SS 5
#define RST 14
#define DIO0 26
#define LED_PIN 2
String lastPacketId = "";

void setup() {
  Serial.begin(115200);
  delay(2000);

  pinMode(LED_PIN, OUTPUT);

  LoRa.setPins(SS, RST, DIO0);

  if (!LoRa.begin(433E6)) {
    Serial.println("LoRa init failed!");
    while (1);
  }

  LoRa.setTxPower(20);
  LoRa.setSpreadingFactor(12);
  LoRa.setSignalBandwidth(125E3);
  Serial.println("=====================================");
  Serial.println("📡 Receiver Ready and Listening...");
  Serial.println("=====================================\n");
}

void loop() {
  int packetSize = LoRa.parsePacket();

  if (packetSize) {
    String incoming = "";

    while (LoRa.available()) {
      incoming += (char)LoRa.read();
    }

    incoming.trim();
    int firstPipe = incoming.indexOf('|');
    int secondPipe = incoming.indexOf('|', firstPipe + 1);
    int thirdPipe = incoming.indexOf('|', secondPipe + 1);
    int fourthPipe = incoming.indexOf('|', thirdPipe + 1);
    if (firstPipe > 0 && secondPipe > 0 && thirdPipe > 0) {
      String pktChunk = incoming.substring(0, firstPipe);
      String idChunk = incoming.substring(firstPipe + 1, secondPipe);
      String msgChunk = incoming.substring(secondPipe + 1, thirdPipe);
      String latChunk = incoming.substring(thirdPipe + 1, fourthPipe);
      String lonChunk = incoming.substring(fourthPipe + 1);
      String currentPktId = pktChunk.substring(4); 
      String senderId = idChunk.substring(3);      
      String lat = latChunk.substring(4);          
      String lon = lonChunk.substring(4);          
      if (currentPktId != lastPacketId) {
        lastPacketId = currentPktId;
        digitalWrite(LED_PIN, HIGH);
        delay(100);
        digitalWrite(LED_PIN, LOW);
        Serial.println("=====================================");
        Serial.println("📨 NEW MESSAGE RECEIVED!");
        Serial.println("Packet ID   : " + currentPktId);
        Serial.println("Sender Name : " + senderId);
        Serial.println("Message     : " + msgChunk);
        Serial.println("Location    : " + lat + ", " + lon);
        Serial.println("Signal(RSSI): " + String(LoRa.packetRssi()) + " dBm");
        Serial.println("=====================================\n");
      }
    } else {
      Serial.print("Received Unknown Format: ");
      Serial.println(incoming);
    }
  }
}