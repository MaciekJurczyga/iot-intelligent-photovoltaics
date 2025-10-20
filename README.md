# ☀️ IoT Intelligent Photovoltaics System

## 🖥️ IoT Server

### 🚀 Running the Application

The application is configured to run in two environments: with Docker (using PostgreSQL) and locally (using an H2 in-memory database).

#### Running with Docker Compose (Recommended)

This method launches the application along with a dedicated PostgreSQL database.

```bash
# Build the images and start the containers
docker-compose up

# If you make changes to the code, rebuild the application image
docker-compose up --build
```
#### Local Development with H2 database
For quick-testing etc. you can build application loccaly, by running IotServerApplication class.
