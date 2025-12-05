# Dokumentacja Projektu IoT - System Zarządzania Energią

## Spis Treści

- [1. Hardware](#1-hardware)
  - [1.1 Raspberry Pi - System Czujników](#11-raspberry-pi---system-czujników)
  - [1.2 Infrastruktura i Konfiguracja Systemowa](#12-infrastruktura-i-konfiguracja-systemowa)
- [2. Backend](#2-backend)
  - [2.1 IoT Server - Integracja i Komunikacja](#21-iot-server---integracja-i-komunikacja)
  - [2.2 System Optymalizacji Energii](#22-system-optymalizacji-energii)
- [3. Frontend](#3-frontend)
- [4. Podsumowanie](#4-podsumowanie)

---

## 1. Hardware

### 1.1 Raspberry Pi - System Czujników

**Autor:** Adrian Listkiewicz

Część odpowiedzialna za obsługę czujników podłączonych do Raspberry Pi 3 B+ z wykorzystaniem REST API opartego na Flask, magistrali GPIO oraz I2C.

#### Etapy Realizacji

1. Konfiguracja środowiska Python na Raspberry Pi
2. Podłączenie i kalibracja czujnika ultradźwiękowego US-015
3. Konfiguracja komunikacji I2C z czujnikiem BME280
4. Implementacja REST API z wykorzystaniem Flask
5. Przygotowanie testów jednostkowych
6. Weryfikacja działania czujników w trybie ciągłym

#### Komponenty Sprzętowe

- **Raspberry Pi 3B+**
- **Płytka stykowa (Breadboard)**
- **BME280** - Czujnik temperatury, wilgotności i ciśnienia (I2C)
- **US-015** - Ultradźwiękowy czujnik odległości (GPIO)
- **Rezystory**: 300Ω oraz 470Ω (dzielnik napięcia)

#### Schematy Połączeń

**A. BME280 (I2C)**  
Zasilanie 3.3V. Podłączenie bezpośrednie.

| Pin RPi 3B+ | Funkcja      | Pin BME280 |
| ----------- | ------------ | ---------- |
| Pin 1       | 3.3V Power   | VIN / VCC  |
| Pin 9       | GND          | GND        |
| Pin 3       | GPIO 2 (SDA) | SDA        |
| Pin 5       | GPIO 3 (SCL) | SCL        |

**B. US-015 (GPIO + Dzielnik Napięcia)**  
Sensor zasilany 5V. Pin Echo wymaga obniżenia napięcia do 3.3V.

Konfiguracja dzielnika:

- R1 (300Ω): Pomiędzy pinem Echo czujnika a wejściem RPi
- R2 (470Ω): Pomiędzy wejściem RPi a masą
- Wynik: Napięcie na wejściu RPi ≈ 3.05V

| Pin RPi 3B+ | Funkcja       | Pin US-015            |
| ----------- | ------------- | --------------------- |
| Pin 2       | 5V Power      | VCC                   |
| Pin 6       | GND           | GND                   |
| Pin 16      | GPIO 23 (Out) | Trig                  |
| Pin 18      | GPIO 24 (In)  | Echo (przez dzielnik) |

#### REST API

**Endpointy:**

##### GET /raspberry/test

Sprawdzenie poprawności połączenia z serwerem.

**Output:**

```json
{
  "message": "Hello from Raspberry Pi!"
}
```

##### GET /raspberry/distance

Pobranie aktualnej odległości z czujnika ultradźwiękowego US-015.

**Output:**

```json
{
  "distance_cm": 123.45
}
```

##### GET /raspberry/bme280

Pojedynczy odczyt z czujnika BME280: temperatura, wilgotność, ciśnienie.

**Output:**

```json
{
  "readings": {
    "temperature": 22.5,
    "humidity": 55.0,
    "pressure": 1000.0
  }
}
```

##### GET /raspberry/bme280/continuous

Zbiera serię odczytów z BME280 w trybie ciągłym przez określony czas.

**Output:**

```json
{
  "readings": [
    {
      "id": 0,
      "timestamp": 1234567890.123,
      "temperature": 22.5,
      "humidity": 55.0,
      "pressure": 1000.1
    }
  ]
}
```

#### Konfiguracja Czujników

**US-015 (GPIO)**

- TRIG_PIN: 23 (BCM)
- ECHO_PIN: 24 (BCM)
- Tryb numeracji: BCM

**BME280 (I2C)**

- Port I2C: 1
- Adres: 0x76/0x77

#### Logika Pomiaru

**get_distance()**

- Wysyłanie impulsu 10 μs na pin TRIG
- Pomiar czasu trwania sygnału na ECHO
- Obliczenie odległości: `distance = pulse_duration * 17150`
- Obsługa timeoutu (40 ms)

**get_bme280_data()**

- Odczyt pojedynczego pomiaru środowiskowego
- Zwraca temperaturę, wilgotność i ciśnienie

**get_bme280_continuous()**

- Odczyty ciągłe w określonym przedziale czasu
- Parametry domyślne: duration=10s, interval=0.5s
- Zwraca listę pomiarów z ID, timestampem i danymi

#### Testy

**test_controller.py**  
Testy jednostkowe dla kontrolera Flask z wykorzystaniem pytest i mockowania.

**Testy dla `/raspberry/distance`:**

- `test_distance_success` - Poprawny odczyt z czujnika
- `test_distance_zero` - Obsługa wartości 0.0
- `test_distance_raises_returns_500` - Symulacja wyjątku sprzętowego

**Testy dla `/raspberry/bme280`:**

- `test_bme280_success` - Poprawne dane środowiskowe
- `test_bme280_value_types` - Weryfikacja typów wartości
- `test_bme280_raises_returns_500` - Obsługa błędów czujnika

**Testy dla `/raspberry/bme280/continuous`:**

- `test_bme280_continuous_success` - Zwracanie listy odczytów
- `test_bme280_continuous_ids_increment` - Rosnące ID pomiarów
- `test_bme280_continuous_raises_returns_500` - Obsługa błędów

#### Zależności

```txt
Flask - REST API
RPi.GPIO - Sterowanie pinami GPIO
smbus2 - Komunikacja I2C
bme280 - Obsługa czujnika BME280
pytest - Testy jednostkowe
```

**Instalacja:**

```bash
pip install -r requirements.txt
```

---

### 1.2 Infrastruktura i Konfiguracja Systemowa

**Autor:** Jan Guz

Konfiguracja środowiska serwerowego, integracja urządzeń IoT oraz setup sieci lokalnej i zdalnej.

#### Etapy Realizacji

1. Instalacja i konfiguracja Ubuntu Server
2. Setup środowiska Docker
3. Instalacja i konfiguracja Home Assistant
4. Integracja urządzeń IoT (Falownik, Klimatyzator, Smart Plug, Licznik)
5. Konfiguracja sieci lokalnej i VPN
6. Przygotowanie skryptów i automatyzacji w HA
7. Testy systemu
8. Podłączenie i testy Raspberry Pi onsite

#### Hardware

**Lokalny serwer Linux**

- Główny serwer z Home Assistant, Backend i Frontend
- System: Ubuntu Server
- Zdalny dostęp: SSH
- Kontenery dla poszczególnych elementów systemu

**Falownik Deye**

- Konfiguracja panelu dostępowego falownika w sieci lokalnej
- Ustawienie stałego adresu IP 

**Klimatyzator MDV**

- Konfiguracja trybów pracy
- Podłączenie do Wi-Fi

**Smart Gniazdko Smart Plug +**

- Konfiguracja zarządzania przez API producenta

#### Home Assistant

**Instalacja**

- Deployment na Ubuntu Server z wykorzystaniem Docker
- Konfiguracja port-forwarding dla dostępu w sieci lokalnej

**HACS (Home Assistant Community Store)**

- Rozszerzenie dostępnych integracji z urządzeniami IoT poprzez instalację HACS

**Integracje Urządzeń**

- **Falownik** - Solarman Integration
- **Klimatyzator** - Midea AC Lan
- **Smart Wtyczka** - Tuya
- **Licznik Tauron** - Tauron AMIPlus
- **Powiadomienia Mobilne** - Mobile App

**Aplikacja Mobilna HA**

- Instalacja na telefonie
- Konfiguracja powiadomień systemowych

**Skrypty i Automatyzacje**

- Przygotowanie skryptów wywoływanych przez Backend, np:
  - uruchamianie trybu klimatyzacji
  - pobieranie danych z API Tauron
  - wysyłanie powiadomień na urządzenie mobilne

**Dashboardy**

- Dashboard wyświetlający dane z urządzeń IoT
- Panel energii z danymi z falownika i licznika Tauron
- Pusty panel webpage (do embeddowania naszego frontendu)

**Selekcja Encji**

- Wybór encji wystawianych przez urządzenia w HA potrzebnych do obsługi logiki przez backend
- Dane o energii i stanach urządzeń
- Przełączniki on/off

#### Konfiguracja Sieciowa

**Adresacja IP**

- Ustawienie statycznych adresów IP dla urządzeń (gdzie możliwe)

**Serwer DHCP**

- Konfiguracja dla urządzeń bez możliwości statycznego IP
- Wymiana urządzeń sieciowych - router ISP nie miał możliwości ręcznych ustawień DHCP

**VPN (Tailscale)**

- Stworzenie wirtualnej sieci prywatnej
- Zdalny dostęp do systemu
- Wykorzystanie w developmencie i aplikacji mobilnej

**IP Plan**

- Dokumentacja sieciowa
- Mapowanie adresów lokalnych i VPN
- Lista haseł dostępowych do urządzeń

#### Testy Systemu

- Weryfikacja włączania/wyłączania urządzeń przez logikę backendu
- Sprawdzenie systemu powiadomień, czy działa w sieci lokalnej oraz przez VPN
- Testy działania czujników Raspberry Pi - weryfikacja, czy pomiary sa zgodne ze stanem faktycznym
- Test utraty zasilania w domu - sprawdzenie, czy system uruchamia się bez problemów po stracie napięcia. 

---

## 2. Backend

### 2.1 IoT Server - Integracja i Komunikacja

**Autor:** Maciej Jurczyga

Główna jednostka logiczna systemu odpowiedzialna za integrację danych, komunikację z Home Assistant oraz zbieranie pomiarów z Raspberry Pi. Serwer zrealizowany w Java 17 z wykorzystaniem Spring Boot oraz PostgreSQL.

#### Etapy Realizacji

1. Setup projektu Spring Boot z Gradle
2. Implementacja HomeAssistantClient (WebClient)
3. Implementacja RaspberryClient (HttpClient)
4. Konfiguracja połączenia z bazą danych PostgreSQL
5. Przygotowanie Dockerfile (multi-stage build)
6. Konfiguracja docker-compose.yml
7. Implementacja testów jednostkowych (JUnit 5 + Mockito)
8. Setup CI/CD (GitHub Actions)
9. Konfiguracja CodeQL (analiza bezpieczeństwa)
10. Dokumentacja API

#### Architektura

**Warstwa Integracyjna (Clients)**  
Moduły odpowiedzialne za komunikację z zewnętrznymi urządzeniami i serwisami, izolujące logikę HTTP od reszty aplikacji.

#### HomeAssistantClient.java

Klient dwukierunkowej komunikacji z API Home Assistant wykorzystujący asynchroniczny WebClient (Spring WebFlux).

**Konfiguracja:**

- Technologia: Spring WebClient (Reactive)
- Encje: Zdefiniowane jako stałe (np. `switch.smart_plug_socket_1`, `sensor.solarman_daily_production`)

**Główne Metody:**

##### getEntityState(String entityId)

- Pobiera aktualny stan dowolnej encji z Home Assistant
- Input: ID encji (String)
- Logika: GET na `/api/states/{id}`, mapowanie na `HomeAssistantStateResponse`
- Obsługa błędów: Przechwytuje `WebClientResponseException` i puste odpowiedzi (fail-safe)

##### callService(String domain, String service, String entityId)

- Wywołuje akcję w Home Assistant
- Input: domena (np. "switch"), usługa (np. "turn_on"), ID encji
- Logika: POST na `/api/services/{domain}/{service}` z payloadem JSON

##### turnOnSmartPlug() / turnOffSmartPlug()

- Sterowanie inteligentnym gniazdkiem
- Output: String z potwierdzeniem lub błędem

##### getPvProductionTotalDaily()

- Pobiera dzienną produkcję fotowoltaiki
- Output: String (np. "10.2")

#### RaspberryClient.java

Klient HTTP dedykowany do komunikacji z podsystemem czujników Raspberry Pi wykorzystujący standardowy `java.net.http.HttpClient`.

**Konfiguracja:**

- Host: `http://100.80.187.125:5000`
- Base Path: `/raspberry`

**Metody:**

##### test()

- Sprawdzenie łączności z serwerem czujników
- Endpoint: GET `/raspberry/test`

##### getDistance()

- Pobiera odczyt z czujnika ultradźwiękowego
- Endpoint: GET `/raspberry/distance`
- Output: JSON String `{"distance_cm": float}`

##### getBme280Continuous()

- Inicjuje pobieranie serii danych środowiskowych
- Endpoint: GET `/raspberry/bme280/continuous`
- Output: JSON String z listą pomiarów

#### Infrastruktura Docker

**Dockerfile (Server)**  
Wielowarstwowy plik budowania obrazu (Multi-stage build):

1. **Build Stage:**

   - Obraz: `gradle:8.10.2-jdk17`
   - Kopiuje kod, pobiera zależności i buduje plik .jar

2. **Runtime Stage:**
   - Obraz: `eclipse-temurin:17-jdk-alpine`
   - Uruchamia zbudowaną aplikację

**Dockerfile (Database)**

- Obraz bazowy: `postgres:16-alpine`
- Inicjalizacja: Kopiuje skrypty SQL z `init/` do `/docker-entrypoint-initdb.d/`
- Automatyczne utworzenie schematu przy pierwszym uruchomieniu

**docker-compose.yml**  
Orkiestrator usług definiujący zależności między aplikacją a bazą danych.

**Usługi:**

- **iot-server:**

  - Mapowanie portów: 8080:8080
  - Zmienne środowiskowe: Konfiguracja JDBC (URL, User, Pass)
  - Zależność: `depends_on: db`

- **iot-db:**
  - Baza PostgreSQL
  - Mapowanie portów: 5432:5432

#### Testy (Unit & Integration)

Zestaw testów jednostkowych opartych na JUnit 5 oraz Mockito, weryfikujący logikę klientów HTTP bez połączenia z fizycznymi urządzeniami.

**RaspberryClientTest.java**

- `setUp()`: Inicjalizacja mocka HttpClient i HttpResponse
- `test_getDistance`: Symuluje odpowiedź JSON i weryfikuje zwracane dane
- `test_getBme280Continuous`: Weryfikuje obsługę tablic w JSON

**HomeAssistantClientTest.java**  
Zaawansowane testy reaktywnego klienta WebClient.

**Scenariusze testowe:**

- Obsługa sukcesu: Mockowanie łańcucha wywołań WebClient
- Obsługa błędów (404/500): Symulacja `WebClientResponseException`
- Puste odpowiedzi: Weryfikacja zachowania przy `Mono.empty()`

#### CI/CD (GitHub Actions)

**build-and-test.yml**  
Workflow uruchamiany przy każdym Push/Pull Request na gałąź main.

1. Checkout: Pobranie kodu
2. Setup: Instalacja JDK 17 (Temurin) oraz Gradle
3. Testy: Uruchomienie `./gradlew test`
4. Wstrzykiwanie sekretów: `HOME_ASSISTANT_URL`, `HOME_ASSISTANT_TOKEN`

**codeql.yml**  
Workflow analizy bezpieczeństwa kodu (SAST).

1. Inicjalizacja CodeQL: Konfiguracja dla języka Java
2. Build: Budowanie projektu bez testów (`-x test`)
3. Analiza: Skanowanie w poszukiwaniu luk bezpieczeństwa

#### Stos Technologiczny

- **Język:** Java 17
- **Framework:** Spring Boot 3.x
- **Build Tool:** Gradle
- **Baza danych:** PostgreSQL 16 (H2 do testów in-memory)
- **Konteneryzacja:** Docker & Docker Compose

**Zależności (build.gradle):**

- `spring-boot-starter-web` (REST API)
- `spring-boot-starter-webflux` (WebClient dla HA)
- `spring-boot-starter-data-jpa` (Persystencja danych)
- `lombok` (Redukcja boilerplate code)
- `junit-jupiter`, `mockito` (Testy)

**Uruchomienie lokalne:**

```bash
docker-compose up --build
```

---

### 2.2 System Optymalizacji Energii

**Autor:** Szymon Wątroba

Moduł odpowiedzialny za inteligentne zarządzanie urządzeniami w celu maksymalizacji wykorzystania energii z paneli fotowoltaicznych. System analizuje dostępną nadwyżkę energii i automatycznie steruje urządzeniami według zdefiniowanych priorytetów.

#### Etapy Realizacji

1. Analiza API Home Assistant
2. Przygotowanie środowiska Spring Boot
3. Konfiguracja VPN do zdalnego dostępu HA
4. Implementacja HomeAssistantClient
5. Konfiguracja poboru urządzeń
6. Kontroler smart plug
7. Implementacja logiki priorytetów (MAX_USAGE/COMFORT)
8. Wdrożenie skryptu na serwer
9. Dostosowanie do różnych mocy urządzeń
10. Testy jednostkowe 
11. Tryb CUSTOM z API do zarządzania priorytetami
12. Testy dla custom mode 
13. Dokumentacja API

#### Architektura Systemu

**Komponenty Główne**

##### PriorityCalculator

Rdzeń logiki decyzyjnej odpowiedzialny za kalkulację priorytetów urządzeń w oparciu o:

- Nadwyżkę energii z PV
- Tryb operacyjny (MAX_USAGE/COMFORT/CUSTOM)
- Stan urządzeń i temperatury

##### SystemStateService

Agregacja stanu systemu pobierająca dane z Home Assistant:

- Produkcja PV (`sensor.solarman_total_ac_output_power_active`)
- Temperatury (wewnętrzna/zewnętrzna)
- Stany urządzeń (AC, ładowarka EV, zmywarka, gniazdko)
- Wykrywanie obecności (`device_tracker.iphone_jan`)

##### DeviceExecutorService

Wykonanie akcji, implementuje sterowanie urządzeniami:

- Smart plug: W pełni zaimplementowane (`turnOnSmartPlug()` / `turnOffSmartPlug()`)
- EV charger, AC, Dishwasher: TODO (logika gotowa, brak integracji z HA)

##### EnergyManagementService

Orkiestrator z automatyczną pętlą sterowania co 30 sekund (`@Scheduled`):

1. Pobranie stanu systemu
2. Kalkulacja decyzji
3. Wykonanie akcji

#### Tryby Operacyjne

##### MAX_USAGE (Nikt w domu)

Maksymalizacja wykorzystania PV. Priorytet:

1. **EV Charger** - Dynamiczna regulacja mocy (1.4-7.4kW)
2. **AC/Heating** - Tylko przy nadwyżce energii
3. **Dishwasher** - 1.8kW
4. **Smart Plug** - 0.5kW

##### COMFORT (Ktoś w domu)

Priorytet komfort. Kolejność:

1. **AC/Heating** - Niezależnie od nadwyżki (22°C ±0.5°C histereza)
2. **EV Charger** - Wykorzystuje pozostałą nadwyżkę
3. **Dishwasher**
4. **Smart Plug**

##### CUSTOM (Użytkownik)

Tryb z customowymi priorytetami definiowanymi przez użytkownika (1-4).

#### Kluczowe Algorytmy

**Kalkulacja Nadwyżki**

```
Surplus = PV_Production - (House_Base + AC + EV + SmartPlug)
```

**Buffer Bezpieczeństwa**  
Każde urządzenie wymaga nadwyżki + 200W buffera przed włączeniem:

```
if (surplus >= device_power + 200W) then turn_on
```

**Dynamiczna Regulacja EV**

```
totalAvailable = surplus + currentEvPower - buffer(200W)
targetPower = clamp(totalAvailable, evMin(1400W), evMax(7400W))
```

**Histereza Temperaturowa**  
Zapobiega cyklowaniu AC:

- Włącz chłodzenie: temp > 22.5°C
- Włącz ogrzewanie: temp < 21.5°C
- Zakres bezczynności: 21.5-22.5°C

#### REST API

**Base URL:** `/api/v1`

**Energy Management**

##### GET /energy/state

Aktualny stan systemu.

##### GET /energy/decision

Kalkulacja bez wykonania (dry run).

##### POST /energy/control

Manualne uruchomienie pętli.

**Priority Configuration**

##### GET /priority/config

Aktualna konfiguracja priorytetów.

##### POST /priority/custom

Ustawienie customowych priorytetów.

**Przykład:**

```bash
curl -X POST http://localhost:8080/api/v1/priority/custom \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "priorities": {
      "EV_CHARGER": 1,
      "AC_CLIMATE": 2,
      "DISHWASHER": 3,
      "SMART_PLUG": 4
    }
  }'
```

##### POST /priority/custom/toggle?enabled=true

Włącz/wyłącz tryb CUSTOM.

##### GET /priority/devices

Lista dostępnych urządzeń.

#### Konfiguracja (application.yml)

```yaml
iot:
  devices:
    # PV System
    pv-max-production: 6000.0

    # Climate
    ac-cooling-power: 2000.0
    target-temperature: 22.0
    temperature-hysteresis: 0.5

    # EV Charger
    ev-min-power: 1400.0
    ev-max-power: 7400.0

    # Others
    dishwasher-power: 1800.0
    smart-plug-power: 500.0
    surplus-buffer: 200.0

    # Custom priorities
    custom-priority-enabled: false
    custom-priority-order:
      - EV_CHARGER
      - AC_CLIMATE
      - DISHWASHER
      - SMART_PLUG
```

#### Testy

**Test Coverage:**

**PriorityCalculatorTest**

- Weryfikacja trybów MAX_USAGE i COMFORT
- Logika ładowania EV (limity mocy, stan baterii)
- Sterowanie AC (temperatura, histereza)
- Zarządzanie nadwyżką

**PriorityCalculatorCustomModeTest**

- Aktywacja trybu CUSTOM
- Kolejność przetwarzania urządzeń
- Override detekcji obecności
- Przypadki brzegowe (puste listy, nieprawidłowe nazwy)

**PriorityConfigControllerTest**

- Walidacja JSON (brakujące urządzenia, duplikaty, nieprawidłowe wartości)
- Konwersja priorytetów na kolejność
- Obsługa błędów API

**Pozostałe testy:**

- SystemStateServiceTest
- EnergyManagementServiceTest
- DeviceExecutorServiceTest

**Uruchomienie:**

```bash
./gradlew test
```

#### Model Danych

**DeviceDecision**

```json
{
  "mode": "CUSTOM",
  "availableSurplus": 1700.0,
  "explanation": "Custom priority mode...",
  "actions": [
    {
      "device": "EV_CHARGER",
      "action": "SET_POWER",
      "targetPower": 3000.0,
      "reason": "Charging with 3000W (60% charged)"
    }
  ]
}
```

**SystemState**  
Agreguje dane z Home Assistant:

- `anyoneHome`, `currentPvProduction`, `currentHouseConsumption`
- `indoorTemperature`, `outdoorTemperature`
- `evConnected`, `evChargePercentage`, `evChargingPower`
- `acOn`, `acPowerUsage`
- `dishwasherReady`, `smartPlugOn`

**Metody pomocnicze:**

- `getTotalConsumption()` - Suma zużycia wszystkich urządzeń
- `getAvailableSurplus()` - Dostępna nadwyżka do wykorzystania

#### Automatyczna Pętla

System automatycznie uruchamia się co 30 sekund po starcie aplikacji (`initialDelay=5s`).

#### Kluczowe Decyzje Projektowe

- **Separacja odpowiedzialności:** Oddzielenie zbierania danych, kalkulacji i wykonania
- **Testowalność:** Mockowanie HomeAssistantClient umożliwia testy bez fizycznych urządzeń
- **Elastyczność:** Łatwe dodawanie nowych urządzeń lub modyfikacja priorytetów
- **Bezpieczeństwo:** Buffer 200W zapobiega pobieraniu z sieci przy włączaniu urządzeń
- **Komfort użytkownika:** Histereza temperaturowa minimalizuje cyklowanie AC

---

## 3. Frontend

**Autor:** Szymon Burliga

Aplikacja Single Page Application (SPA) napisana w React + TypeScript do wizualizacji i sterowania systemem IoT.

#### Etapy Realizacji

1. Setup projektu React z TypeScript
2. Konfiguracja proxy do Home Assistant
3. Implementacja DashboardView (aktualne odczyty)
4. Implementacja DevicesView (sterowanie urządzeniami)
5. Implementacja ChartsView (wykresy historyczne)
6. Implementacja ScenariosView (edytor priorytetów)
7. Integracja z API Home Assistant
8. Konfiguracja zmiennych środowiskowych
9. Testy UI i integracyjne

#### Struktura i Uruchomienie

**Instalacja:**

```bash
npm install
```

**Start:**

```bash
npm start
```

Dostęp pod `http://localhost:3000`

**Proxy:**  
Plik `src/setupProxy.js` przekierowuje zapytania `/api` do Home Assistant.

#### Główne Pliki

- `src/App.tsx` - Główny layout aplikacji
- `src/components/DashboardView.tsx` - Aktualne odczyty (PV, Zużycie)
- `src/components/DevicesView.tsx` - Lista urządzeń i sterowanie manualne
- `src/components/ChartsView.tsx` - Wykresy dzienne/tygodniowe (recharts)
- `src/components/ScenariosView.tsx` - Edytor priorytetów (Drag & Drop)

#### Funkcjonalności

**Dashboard**

- Wyświetla aktualny bilans mocy
- Pobiera dane z encji `sensor.*` Home Assistant
- Real-time monitoring produkcji PV i zużycia

**Devices**

- Lista wszystkich urządzeń w systemie
- Ręczne włączanie/wyłączanie Smart Plugów
- POST requesty do backendu
- Status urządzeń w czasie rzeczywistym

**Charts**

- Agregacja danych historycznych z HA API (`/api/history/period/...`)
- Wykresy słupkowe (recharts)
- Widoki: dzienny, tygodniowy
- Analiza produkcji i zużycia energii

**Scenarios**

- Interfejs Drag & Drop do zmiany kolejności urządzeń
- Zarządzanie priorytetami automatyzacji
- Definiowanie custom mode
- Podgląd aktualnej konfiguracji

#### Konfiguracja

Token dostępu do Home Assistant należy przechowywać w pliku `.env`:

```env
REACT_APP_HA_TOKEN=twój_token_tutaj
```

#### Logika Systemu: Scenariusze Zużycia

Decyzje o sterowaniu podejmowane są na podstawie obecności domowników.

## Scenariusz 1: Dom pusty (Tryb: Maksymalna Autokonsumpcja)

Priorytetem jest zużycie całej nadwyżki, aby zminimalizować eksport.

1. **E-vehicle (Priorytet główny)**

   - Działanie: Płynna regulacja mocy ładowania w zależności od nadwyżki PV (start od >300W)
   - Cel: Utrzymanie eksportu bliskiego zeru

2. **AC / Electric Furnace**

   - Działanie: Uruchamiane tylko przy bardzo dużej nadwyżce (gdy auto nie może przyjąć więcej) lub skrajnych temperaturach w domu
   - Warunek: Nadwyżka > (Moc urządzenia + Histereza)

3. **Misc (Smart Plugs)**
   - Działanie: Uruchamiane na końcu, jeśli nadwyżka jest stabilna (np. zmywarka)

## Scenariusz 2: Domownicy obecni (Tryb: Komfort)

Priorytetem są potrzeby użytkownika — system dba przede wszystkim o komfort, a optymalizacja energetyczna działa w tle.

### 1. Komfort termiczny (AC)

**Działanie:**  
Termostat utrzymuje stałą temperaturę zadaną przez użytkownika (np. **22°C**) niezależnie od poziomu produkcji PV.

- AC nie jest wykorzystywany agresywnie jako magazyn energii.
- Priorytet: komfort mieszkańców, a nie maksymalizacja autokonsumpcji.
- Wpływ PV na AC jest ograniczony — tylko w przypadku wyraźnych nadwyżek wykonywane jest lekkie "pre-conditioning".

---

### 2. E-vehicle (Regulator resztkowy)

**Działanie:**  
Samochód elektryczny ładuje się wyłącznie energią **pozostałą po pokryciu zużycia domu i pracy klimatyzacji**.

**Wzór:**  
Moc ładowania = ProdukcjaPV - (Dom + AC)

- System nie obniża komfortu (nie wyłącza AC), by zwiększyć moc ładowania.
- Dynamiczna regulacja co kilka sekund.
- Priorytet: komfort → stabilność domu → wykorzystanie nadwyżki.

---

### 3. Przekondycjonowanie (Magazyn ciepła)

**Działanie:**  
Jeśli występuje stabilna nadwyżka PV, system **delikatnie koryguje temperaturę** (np. chłodzenie mocniej o **1°C**) w celu magazynowania energii w strukturze budynku.

- Zmiana jest subtelna, nieodczuwalna dla użytkowników.
- Mechanizm działa tylko przy pewnych nadwyżkach i po spełnieniu warunku komfortu.
- Celem jest wykorzystanie darmowej energii w sposób nieinwazyjny.

---

## 4. Podsumowanie

Projekt IoT stanowi zintegrowany system zarządzania energią domową, którego zadaniem jest zwiększenie autokonsumpcji fotowoltaiki, inteligentne sterowanie urządzeniami oraz zapewnienie komfortu użytkowników.

### Hardware

- Raspberry Pi 3B+ (czujniki: BME280, US-015)
- Falownik fotowoltaiczny Deye
- Klimatyzator MDV
- Smart Plug
- Licznik energii Tauron

### Backend

- Java 17 + Spring Boot
- PostgreSQL
- Docker & Docker Compose
- REST API
- WebClient (Home Assistant)
- HttpClient (Raspberry Pi)

### Frontend

- React + TypeScript
- Recharts (wizualizacje)
- Drag & Drop (zarządzanie priorytetami)

### Infrastruktura

- Home Assistant (hub IoT)
- Tailscale VPN (zdalny dostęp)
- GitHub Actions (CI/CD)
- CodeQL (analiza bezpieczeństwa)

System automatycznie optymalizuje energię z fotowoltaiki, minimalizuje eksport do sieci oraz maksymalizuje autokonsumpcję — zawsze zachowując komfort użytkowników jako najwyższy priorytet.
