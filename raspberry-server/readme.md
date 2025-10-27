# Raspberry Pi Sensor REST API

The project runs a REST server on Raspberry Pi 3 B+, which enables reading data from sensors:

- **BME280** – temperature, humidity and pressure sensor (I2C)
- **US-015** – ultrasonic distance sensor (GPIO TRIG/ECHO)

## Project structure

#### projekt_raspberry

- controller.py
- sensor_service.py
- requirements.txt
- README.md

## Installation

1. Clone the repository or copy the files to Raspberry Pi.

2. Recommended: create a virtual environment (`venv`) to isolate packages:

```bash
python3 -m venv venv
```

```bash
source venv/bin/activate
```

3. Install required packages:

```bash
pip install -r requirements.txt
```

4. Starting the server

```bash
python3 controller.py
```

The server will start on port 5000 and will be available for devices on the same local network.

## REST Endpoints

#### Main path: /raspberry

Endpoint Method Description

- /raspberry/test GET Server test, returns "Hello from Raspberry Pi!"
- /raspberry/distance GET Distance reading from US-015 sensor in cm
- /raspberry/bme280 GET Data reading from BME280 for 1 minute (temperature, humidity, pressure)

## Testing endpoints

Browser: enter in the address bar:

```
http://<IP_RaspberryPi>:5000/raspberry/test
```

curl in terminal:

```
curl http://<IP_RaspberryPi>:5000/raspberry/test
```

```
curl http://<IP_RaspberryPi>:5000/raspberry/distance
```

```
curl http://<IP_RaspberryPi>:5000/raspberry/bme280
```

Notes

If you want the server to be accessible only locally on Raspberry Pi, change:

```
app.run(host='0.0.0.0', port=5000)
```

to:

```
app.run(host='127.0.0.1', port=5000)
```

To check the IP address of Raspberry Pi on the local network:

```
hostname -I
```

Endpoints work on the local network, so your laptop/phone must be connected to the same WiFi network.
