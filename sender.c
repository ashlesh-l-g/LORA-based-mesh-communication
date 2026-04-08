#include <SPI.h>
#include <LoRa.h>
#define SS 5
#define RST 14
#define DIO0 26
#define LED_PIN 2
int packetID = 0;
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
  Serial.println("USB Sender Ready");
}

void loop() {
  if (Serial.available()) {
    String msg = Serial.readStringUntil('\n');
    msg.trim();

    if (msg.length() > 0) {
      digitalWrite(LED_PIN, HIGH);
      delay(100);
      digitalWrite(LED_PIN, LOW);
      Serial.println("RECEIVED FROM USB: " + msg);
      LoRa.beginPacket();
      LoRa.print(msg);
      LoRa.endPacket();
      
      Serial.println("Message broadcasted via LoRa!");
    }
  }
}