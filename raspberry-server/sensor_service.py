import time
import RPi.GPIO as GPIO
from smbus2 import SMBus
import bme280

# --- Konfiguracja GPIO dla US-015 ---
TRIG_PIN = 23
ECHO_PIN = 24
GPIO.setmode(GPIO.BCM)
GPIO.setup(TRIG_PIN, GPIO.OUT)
GPIO.setup(ECHO_PIN, GPIO.IN)
GPIO.output(TRIG_PIN, False)

# --- Konfiguracja I2C dla BME280 ---
I2C_PORT = 1
BME280_ADDRESS = 0x76

class SensorService:
    def __init__(self):
        # Inicjalizacja I2C dla BME280
        self.bus = SMBus(I2C_PORT)
        self.calibration_params = bme280.load_calibration_params(self.bus, BME280_ADDRESS)

    def get_distance(self):
        # Wysłanie krótkiego impulsu TRIG
        GPIO.output(TRIG_PIN, True)
        time.sleep(0.00001)
        GPIO.output(TRIG_PIN, False)

        pulse_start = time.time()
        timeout = pulse_start + 0.04  # 40 ms timeout

        # Czekanie na sygnał ECHO = HIGH
        while GPIO.input(ECHO_PIN) == 0 and time.time() < timeout:
            pulse_start = time.time()

        pulse_end = time.time()
        timeout = pulse_end + 0.04

        # Czekanie na sygnał ECHO = LOW
        while GPIO.input(ECHO_PIN) == 1 and time.time() < timeout:
            pulse_end = time.time()

        pulse_duration = pulse_end - pulse_start
        distance = round(pulse_duration * 17150, 2)  # cm
        return distance

    def get_bme280_data(self):
        # Pobranie pojedynczego pomiaru
        data = bme280.sample(self.bus, BME280_ADDRESS, self.calibration_params)
        return {
            "temperature": data.temperature,
            "humidity": data.humidity,
            "pressure": data.pressure
        }

    def close(self):
        # Sprzątanie I2C i GPIO
        self.bus.close()
        GPIO.cleanup()