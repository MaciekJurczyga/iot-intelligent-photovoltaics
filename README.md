# ☀️ IoT Intelligent Photovoltaics System

## 🖥️ IoT Server

### 🚀 Running the Server with Docker

To build the Docker image and run the server locally, follow these steps:

```bash
# Build the Docker image
docker build -t iot-server .

# Run the container
docker run -p 8080:8080 iot-server
