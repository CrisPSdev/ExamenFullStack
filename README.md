###### Integrante ######
Cristóbal Pardo

---

# Proyecto Final - Arquitectura de Microservicios DUOC

## 1. Descripcion del proyecto

Este proyecto implementa una arquitectura de microservicios basada en **Spring Boot + Spring Cloud**, integrando servicios independientes gestionados mediante **Eureka Server** y expuestos a traves de un **API Gateway**. Cada microservicio cuenta con su propia base de datos **MySQL**, orquestada con **Docker Compose**.

El objetivo del examen fue desarrollar un nuevo microservicio e integrarlo a la infraestructura existente. Se eligio la **Opcion 1: Microservicio de Itinerario**, el cual permite registrar los lugares que un usuario desea visitar durante su viaje, clasificandolos como **Hoteles, Tours o Sitios Turisticos**.

---

## 2. Arquitectura general

```text
+-------------+      +----------------+      +------------------+
|   Cliente   +----->|  API Gateway   +----->|  Eureka Server   |
+-------------+      |    (9999)      |      |    (8761)        |
                     +----------------+      +------------------+
                              |
        +---------------------+---------------------+
        |                     |                     |
   +----v----+          +-----v-----+         +-----v------+
   |  login  |          | destination|        |  traveler  |
   | (9000)  |          |  (9001)   |        |  (9002)    |
   +---------+          +-----------+        +------------+
        |                                           |
        |              +----------------------------+
        |              |
        |         +----v------+
        |         | itinerary |
        |         |  (9003)   |
        |         +-----------+
        |              |
   +----v----+    +-----v-------+
   |mysql_login|  |mysql_itinerary|
   |  (3310)   |  |   (3313)     |
   +-----------+  +--------------+
```

### Flujo de comunicacion

1. **Eureka Server** registra todos los microservicios.
2. **API Gateway** expone las rutas publicas y enruta las peticiones hacia cada servicio.
3. **Login Service** gestiona la autenticacion y generacion de tokens JWT.
4. **Destination Service** administra destinos turisticos.
5. **Traveler Service** consume Login y Destination para crear y listar viajes.
6. **Itinerary Service** consume Login para validar token y Traveler para validar viajes.

---

## 3. Servicios del ecosistema

| Servicio | Puerto | Base de datos | Tecnologias principales |
|---|---|---|---|
| Eureka Server | 8761 | No aplica | Spring Cloud Netflix Eureka |
| API Gateway | 9999 | No aplica | Spring Cloud Gateway |
| Login Service | 9000 | mysql_login (3310) | Spring Boot, Spring Security, JWT |
| Destination Service | 9001 | mysql_destination (3311) | Spring Boot, Spring WebFlux, JPA |
| Traveler Service | 9002 | mysql_traveler (3312) | Spring Boot, WebClient, JPA |
| **Itinerary Service** | **9003** | **mysql_itinerary (3313)** | **Spring Boot, WebClient, JPA** |

Cada servicio tiene su propia base de datos aislada, con usuario `examen` y contrasena `examen123`.

---

## 4. Microservicio Itinerary

### 4.1 Descripcion

Permite registrar y gestionar itinerarios asociados a un viaje existente. Cada itinerario contiene una lista de lugares o actividades clasificadas como:

- `HOTEL`
- `TOUR`
- `SITIO_TURISTICO`

### 4.2 Patron CSR

El microservicio sigue el patron **Controller - Service - Repository/Model**, separando responsabilidades en paquetes por capa:

```text
cl.duoc.itinerary/
├── client/         # Comunicacion con otros microservicios
├── config/         # Seguridad y WebClient
├── controller/     # Endpoints REST
├── dto/            # Request y Response DTOs
├── enums/          # Enumeraciones de dominio
├── exception/      # Manejo global de excepciones
├── model/          # Entidades JPA
├── repository/     # Repositorios Spring Data
└── service/        # Logica de negocio
```

### 4.3 Modelo de datos (IE 2.1.3)

Se utilizan **2 tablas**, cumpliendo con el maximo de 3 tablas exigido.

#### Tabla `itineraries`

| Campo | Tipo | Descripcion |
|---|---|---|
| `id` | BINARY(16) | Primary key, UUID |
| `trip_id` | BINARY(16) | ID del viaje en Traveler Service |
| `title` | VARCHAR(100) | Titulo del itinerario |
| `description` | TEXT | Descripcion opcional |
| `start_date` | DATE | Fecha de inicio |
| `end_date` | DATE | Fecha de termino |
| `created_at` | DATETIME | Fecha de creacion |

#### Tabla `itinerary_items`

| Campo | Tipo | Descripcion |
|---|---|---|
| `id` | BINARY(16) | Primary key, UUID |
| `itinerary_id` | BINARY(16) | Foreign key a `itineraries.id` |
| `item_type` | VARCHAR(30) | Tipo: HOTEL, TOUR, SITIO_TURISTICO |
| `name` | VARCHAR(100) | Nombre del lugar o actividad |
| `scheduled_date` | DATE | Fecha programada |
| `scheduled_time` | TIME | Hora programada (opcional) |
| `notes` | TEXT | Notas opcionales |
| `order_index` | INT | Orden dentro del itinerario |

#### Relaciones

- `Itinerary` **1:N** `ItineraryItem`: un itinerario tiene muchos items.
- La relacion con `traveler.trips` no se modela como FK fisica porque esta en otra base de datos; se valida mediante comunicacion remota.
- Se aplica `cascade = CascadeType.ALL` y `orphanRemoval = true`, de modo que al eliminar un itinerario se eliminan automaticamente sus items.

### 4.4 Endpoints REST

#### Itinerarios (`/api/v1/itinerary/itineraries`)

| Metodo | Endpoint | Descripcion |
|---|---|---|
| POST | `/` | Crear itinerario |
| GET | `/` | Listar todos los itinerarios |
| GET | `/{id}` | Obtener itinerario con sus items |
| GET | `/trip/{tripId}` | Listar itinerarios de un viaje |
| PUT | `/{id}` | Actualizar itinerario |
| DELETE | `/{id}` | Eliminar itinerario e items |

#### Items (`/api/v1/itinerary/items`)

| Metodo | Endpoint | Descripcion |
|---|---|---|
| POST | `/{itineraryId}` | Agregar item a un itinerario |
| GET | `/{id}` | Obtener item por ID |
| PUT | `/{id}` | Actualizar item |
| DELETE | `/{id}` | Eliminar item |

Todos los endpoints requieren autenticacion mediante JWT en el header `Authorization: Bearer <token>`.

### 4.5 Reglas de negocio

1. Al crear un itinerario se valida que el viaje exista en **Traveler Service**.
2. La fecha de termino no puede ser anterior a la fecha de inicio.
3. Al agregar o actualizar un item, su fecha programada debe estar dentro del rango del itinerario.
4. El tipo de item debe ser `HOTEL`, `TOUR` o `SITIO_TURISTICO`.
5. El orden del item debe ser mayor o igual a cero.

---

## 5. Comunicacion entre microservicios (IE 2.4.2)

El microservicio `itinerary` consume dos servicios mediante **WebClient** con **Spring Cloud LoadBalancer**:

### 5.1 AuthClient -> Login Service

```text
GET http://login/api/v1/users/validate?token=<JWT>
```

Valida el token JWT en cada peticion. Si el token es invalido, se responde con HTTP 401.

### 5.2 TravelerClient -> Traveler Service

```text
GET http://traveler/api/v1/traveler/trips/{id}
```

Valida que el `tripId` enviado corresponda a un viaje existente antes de crear el itinerario. Se maneja el error 404 como "viaje no encontrado".

### 5.3 Justificacion

La validacion remota del viaje garantiza la integridad referencial entre microservicios sin depender de una base de datos compartida, respetando el principio de base de datos aislada por servicio.

---

## 6. Seguridad

- Todos los endpoints protegidos requieren token JWT.
- Se implementa un `TokenValidationFilter` que consulta al Login Service.
- Los endpoints de Swagger estan permitidos sin autenticacion para facilitar la documentacion.

---

## 7. Pruebas unitarias (IE 3.1.2)

Se desarrollaron pruebas unitarias para validar la logica del microservicio:

```text
src/test/java/cl/duoc/itinerary/
├── controller/
│   ├── ItineraryControllerStandaloneTest.java
│   └── ItineraryItemControllerStandaloneTest.java
└── service/
    ├── ItineraryServiceTest.java
    └── ItineraryItemServiceTest.java
```

### Casos de prueba cubiertos

- Crear itinerario con viaje valido.
- Rechazar itinerario cuando el viaje no existe.
- Rechazar fechas incoherentes.
- Obtener itinerario con sus items.
- Eliminar itinerario.
- Agregar item con fecha dentro del rango.
- Rechazar item con fecha fuera de rango.
- Actualizar item.
- Validar token en controllers.
- Manejar requests invalidos.

### Ejecucion de tests

```bash
./mvnw test
```

Resultado: **15 tests, 0 fallas, 0 errores**.

---

## 8. Documentacion tecnica con Swagger (IE 3.2.2)

La documentacion de la API se genera automaticamente con **SpringDoc OpenAPI**. Cada controller esta anotado con `@Tag` y cada endpoint con `@Operation`, describiendo su proposito.

### URLs de acceso

- Itinerary Service: `http://localhost:9003/index.html`
- A traves del Gateway: `http://localhost:9999/index.html`

### Modelos documentados

- `ItineraryCreateRequestDTO`
- `ItineraryUpdateRequestDTO`
- `ItineraryItemCreateRequestDTO`
- `ItineraryItemUpdateRequestDTO`
- `ItineraryResponseDTO`
- `ItineraryItemResponseDTO`
- `ItineraryDetailResponseDTO`
- `ApiResponse<T>`

La documentacion refleja los endpoints, parametros, respuestas y ejemplos reales de uso, manteniendo coherencia con el codigo implementado.

---

## 9. Ejecucion del proyecto

### 9.1 Requisitos

- Java 21
- Maven
- Docker y Docker Compose

### 9.2 Levantar bases de datos

```bash
docker-compose up -d
```

### 9.3 Levantar microservicios

En orden:

```bash
./eurekaserver/mvnw spring-boot:run -f eurekaserver/pom.xml
./gateway/mvnw spring-boot:run -f gateway/pom.xml
./login/mvnw spring-boot:run -f login/pom.xml
./destination/mvnw spring-boot:run -f destination/pom.xml
./traveler/mvnw spring-boot:run -f traveler/pom.xml
./itinerary/mvnw spring-boot:run -f itinerary/pom.xml
```

### 9.4 Probar endpoints

A traves del Gateway:

```bash
# Crear itinerario
curl -X POST http://localhost:9999/api/v1/itinerary/itineraries \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Itinerario Santiago",
    "description": "Plan de 3 dias",
    "tripId": "<TRIP_ID>",
    "startDate": "2026-08-01",
    "endDate": "2026-08-03"
  }'
```

---

## 10. Aporte personal y commits (IE 2.5.2)

El desarrollo del microservicio `itinerary` se realizo de forma progresiva, siguiendo el plan definido en `estructura.md`. Los commits fueron organizados por fases:

1. Estructura base del microservicio.
2. Configuracion de seguridad y validacion de token.
3. Modelo de datos y migraciones Flyway.
4. Comunicacion remota con Traveler Service.
5. Logica de negocio y CRUD.
6. Endpoints REST y DTOs.
7. Documentacion Swagger y manejo de excepciones.
8. Pruebas unitarias.
9. Integracion con Docker Compose y API Gateway.
10. Documentacion final del proyecto.

---

## 11. Criterios de evaluacion cubiertos

| Criterio | Como se aborda |
|---|---|
| IE 1.1.1 | Endpoints REST semanticos con metodos HTTP correctos. |
| IE 1.2.1 | Patron CSR con paquetes separados. |
| IE 1.3.1 | Bean Validation en DTOs. |
| IE 1.3.2 | GlobalExceptionHandler para manejo de errores. |
| IE 2.1.1 | Esquema normalizado con 2 tablas, entidades JPA y CRUD completo. |
| IE 2.1.3 | Explicacion del modelado en este README. |
| IE 2.2.1 | Validaciones de negocio en la capa de servicio. |
| IE 2.3.1 | Manejo de excepciones y logs estructurados. |
| IE 2.4.1 | WebClient para consumir login y traveler. |
| IE 2.4.2 | Explicacion del flujo de comunicacion en este README. |
| IE 2.5.1 | Commits progresivos y tecnicos. |
| IE 2.5.2 | Aporte personal documentado. |
| IE 3.1.1 / 3.1.2 | Tests unitarios para controller y service. |
| IE 3.2.1 / 3.2.2 | Documentacion Swagger con ejemplos. |
| IE 3.3.1 | Configuracion YAML, Gateway, Docker Compose y Eureka. |
