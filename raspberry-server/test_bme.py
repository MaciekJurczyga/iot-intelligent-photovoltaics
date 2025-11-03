import smbus2
import bme280

bus = smbus2.SMBus(1)
address = 0x76  # or 0x77 depending on your sensor (check with i2cdetect)

calibration_params = bme280.load_calibration_params(bus, address)
data = bme280.sample(bus, address, calibration_params)

print(f"Temperature: {data.temperature:.2f} °C")
print(f"Pressure: {data.pressure:.2f} hPa")
print(f"Humidity: {data.humidity:.2f} %")
