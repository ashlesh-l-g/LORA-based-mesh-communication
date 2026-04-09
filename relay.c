#include <SPI.h>
#include <LoRa.h>

#define SS 5
#define RST 14
#define DIO0 26
#define LED_PIN 2

const int HISTORY_SIZE = 5;
String packetHistory[HISTORY_SIZE];
int historyIndex = 0;

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

  Serial.println("Relay Node Ready");
}

void loop() {
  int packetSize = LoRa.parsePacket();

  if (packetSize) {
    String msg = "";

    while (LoRa.available()) {
      msg += (char)LoRa.read();
    }

    msg.trim();

    bool isNewMessage = true;
    for (int i = 0; i < HISTORY_SIZE; i++) {
      if (packetHistory[i] == msg) {
        isNewMessage = false; // We found a match, it's an old message/echo
        break;
      }
    }

    if (isNewMessage) {
      packetHistory[historyIndex] = msg;
      historyIndex = (historyIndex + 1) % HISTORY_SIZE; // Loop back to 0 when we hit 5

      digitalWrite(LED_PIN, HIGH);
      delay(80);
      digitalWrite(LED_PIN, LOW);

      Serial.println("Forwarding: " + msg);

      delay(50);

      LoRa.beginPacket();
      LoRa.print(msg);
      LoRa.endPacket();
    } else {
      Serial.println("Ignored duplicate/echo: " + msg);
    }
  }
}