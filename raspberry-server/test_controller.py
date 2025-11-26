import pytest
from unittest.mock import MagicMock

import controller


@pytest.fixture(autouse=True)
def _isolate_sensor_service(monkeypatch):
    """Replace the real hardware-backed sensor_service with a MagicMock for every test.

    The fixture is autouse so tests don't need to patch explicitly.
    """
    mock = MagicMock()
    monkeypatch.setattr(controller, "sensor_service", mock)
    return mock


@pytest.fixture
def client():
    return controller.app.test_client()


### Tests for /raspberry/distance


def test_distance_success(client, _isolate_sensor_service):
    _isolate_sensor_service.get_distance.return_value = 123.45
    resp = client.get("/raspberry/distance")
    assert resp.status_code == 200
    assert resp.is_json
    assert resp.get_json() == {"distance_cm": 123.45}


def test_distance_zero(client, _isolate_sensor_service):
    _isolate_sensor_service.get_distance.return_value = 0.0
    resp = client.get("/raspberry/distance")
    assert resp.status_code == 200
    assert resp.is_json
    json = resp.get_json()
    assert "distance_cm" in json
    # zero should be preserved
    assert json["distance_cm"] == 0.0


def test_distance_raises_returns_500(client, _isolate_sensor_service):
    _isolate_sensor_service.get_distance.side_effect = Exception("hardware error")
    resp = client.get("/raspberry/distance")
    # controller doesn't catch exceptions, Flask should return a 500
    assert resp.status_code == 500


### Tests for /raspberry/bme280


def test_bme280_success(client, _isolate_sensor_service):
    sample = {"temperature": 22.5, "humidity": 55.0, "pressure": 1000.0}
    _isolate_sensor_service.get_bme280_data.return_value = sample
    resp = client.get("/raspberry/bme280")
    assert resp.status_code == 200
    assert resp.is_json
    data = resp.get_json()
    assert "readings" in data
    assert data["readings"] == sample


def test_bme280_value_types(client, _isolate_sensor_service):
    sample = {"temperature": 18, "humidity": 40.5, "pressure": 1013}
    _isolate_sensor_service.get_bme280_data.return_value = sample
    resp = client.get("/raspberry/bme280")
    assert resp.status_code == 200
    payload = resp.get_json()["readings"]
    # types: temperature/pressure can be int/float, humidity float
    assert isinstance(payload["temperature"], (int, float))
    assert isinstance(payload["humidity"], (int, float))
    assert isinstance(payload["pressure"], (int, float))


def test_bme280_raises_returns_500(client, _isolate_sensor_service):
    _isolate_sensor_service.get_bme280_data.side_effect = RuntimeError("sensor fail")
    resp = client.get("/raspberry/bme280")
    assert resp.status_code == 500


### Tests for /raspberry/bme280/continuous


def test_bme280_continuous_success(client, _isolate_sensor_service):
    readings = [
        {"id": 0, "timestamp": 1.0, "temperature": 20.0, "humidity": 50.0, "pressure": 1005.0},
        {"id": 1, "timestamp": 1.5, "temperature": 20.1, "humidity": 50.1, "pressure": 1005.1},
    ]
    _isolate_sensor_service.get_bme280_continuous.return_value = readings
    resp = client.get("/raspberry/bme280/continuous")
    assert resp.status_code == 200
    assert resp.is_json
    data = resp.get_json()
    assert "readings" in data
    assert isinstance(data["readings"], list)
    assert len(data["readings"]) == 2


def test_bme280_continuous_ids_increment(client, _isolate_sensor_service):
    readings = [
        {"id": 0, "timestamp": 1.0, "temperature": 20.0},
        {"id": 1, "timestamp": 1.5, "temperature": 20.1},
        {"id": 2, "timestamp": 2.0, "temperature": 20.2},
    ]
    _isolate_sensor_service.get_bme280_continuous.return_value = readings
    resp = client.get("/raspberry/bme280/continuous")
    assert resp.status_code == 200
    ids = [r.get("id") for r in resp.get_json()["readings"]]
    assert ids == [0, 1, 2]


def test_bme280_continuous_raises_returns_500(client, _isolate_sensor_service):
    _isolate_sensor_service.get_bme280_continuous.side_effect = Exception("timeout")
    resp = client.get("/raspberry/bme280/continuous")
    assert resp.status_code == 500
