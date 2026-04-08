# LoRa-Based Mesh Communication System

## Overview

This project implements a simple mesh communication network using LoRa (Long Range) radio modules. The system consists of three main components: a **Sender**, a **Relay**, and a **Receiver**. It allows for broadcasting messages across a network of devices, with relay nodes forwarding messages to extend range and ensure delivery while preventing infinite loops through duplicate detection.

The system is designed for Arduino-compatible boards with LoRa modules (e.g., ESP32 with SX1278 LoRa module).

## Features

- **Long-range communication**: Utilizes LoRa technology for reliable communication over distances up to several kilometers.
- **Mesh networking**: Relay nodes forward messages to create a mesh topology.
- **Duplicate prevention**: Relay nodes maintain a history of recent packets to avoid forwarding duplicates or echoes.
- **Flexible message format**: Supports structured messages with packet ID, sender ID, message content, and location data.
- **LED indicators**: Visual feedback for message transmission and reception.
- **Serial monitoring**: Detailed logging via serial output for debugging and monitoring.

## Hardware Requirements

- Arduino-compatible board (e.g., ESP32, Arduino Uno)
- LoRa module (e.g., SX1278/RFM95)
- Antenna suitable for 433MHz frequency
- LED (connected to GPIO 2)
- Connecting wires

### Pin Configuration

- SS (Chip Select): GPIO 5
- RST (Reset): GPIO 14
- DIO0 (Interrupt): GPIO 26
- LED: GPIO 2

## Software Requirements

- Arduino IDE
- LoRa library by Sandeep Mistry (install via Arduino Library Manager)
- SPI library (built-in)

## Installation and Setup

1. **Install Arduino IDE**: Download and install from [arduino.cc](https://www.arduino.cc/en/software).

2. **Install LoRa Library**:
   - Open Arduino IDE
   - Go to Sketch > Include Library > Manage Libraries
   - Search for "LoRa" by Sandeep Mistry
   - Install the library

3. **Upload Code**:
   - Open the respective `.c` file (sender.c, relay.c, receiver.c) in Arduino IDE
   - Select your board and port
   - Upload the code to each device

4. **Hardware Assembly**:
   - Connect the LoRa module to your board using the specified pins
   - Connect an LED to GPIO 2
   - Attach the antenna to the LoRa module

5. **Power On**: Power up all devices. They will initialize and start operating automatically.

## Usage

### Sender Node

- Connect the sender device to a computer via USB
- Open Serial Monitor in Arduino IDE (115200 baud)
- Type messages and press Enter to broadcast them via LoRa
- The LED will blink briefly for each sent message

### Relay Node

- The relay node automatically listens for LoRa packets
- It checks incoming messages against a history of recent packets
- If the message is new, it forwards it; otherwise, it ignores duplicates
- The LED blinks briefly for forwarded messages

### Receiver Node

- The receiver node listens for LoRa packets
- It expects messages in the format: `PKT<id>|ID<name>|MSG<message>|LAT<latitude>|LON<longitude>`
- Upon receiving a valid message, it parses and displays:
  - Packet ID
  - Sender Name
  - Message content
  - Location coordinates
  - Signal strength (RSSI)
- The LED blinks briefly for each new message

## Message Format

Messages should follow this structure for proper parsing by the receiver:

```
PKT<packet_id>|ID<sender_id>|MSG<message_text>|LAT<latitude>|LON<longitude>
```

Example:
```
PKT001|IDAlice|MSGHello World|LAT40.7128|LON-74.0060
```

## Configuration

The following parameters can be adjusted in the code:

- **Frequency**: Set to 433MHz (433E6)
- **Tx Power**: 20 dBm
- **Spreading Factor**: 12 (affects range and data rate)
- **Signal Bandwidth**: 125 kHz
- **History Size**: Relay nodes keep history of 5 recent packets

## Troubleshooting

- **LoRa init failed**: Check wiring connections and power supply
- **No messages received**: Ensure all devices are on the same frequency and within range
- **Duplicates not prevented**: Verify relay nodes are functioning correctly
- **Serial output issues**: Confirm baud rate is set to 115200

## Contributing

Feel free to submit issues, feature requests, or pull requests to improve this project.

## License

This project is open-source. Please check individual file headers for license information.