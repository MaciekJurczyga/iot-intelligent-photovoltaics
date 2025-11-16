from flask import Flask, jsonify
from sensor_service import SensorService

app = Flask(__name__)
sensor_service = SensorService()

BASE_PATH = "/raspberry"

@app.route(f'{BASE_PATH}/test', methods=['GET'])
def test():
    return jsonify({"message": "Hello from Raspberry Pi!"})

@app.route(f'{BASE_PATH}/distance', methods=['GET'])
def distance():
    dist = sensor_service.get_distance()
    return jsonify({"distance_cm": dist})

@app.route(f'{BASE_PATH}/bme280', methods=['GET'])
def bme280_data():
    readings = sensor_service.get_bme280_data()
    return jsonify({"readings": readings})

@app.route(f'{BASE_PATH}/bme280/contionowus', methods=['GET'])
def bme280_continuous():
    readings = sensor_service.get_bme280_continuous()
    return jsonify({"readings": readings})

if __name__ == "__main__":
    try:
        app.run(host='0.0.0.0', port=5000)
    finally:
        sensor_service.close()
