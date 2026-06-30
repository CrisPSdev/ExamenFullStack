###### Integrante ######
Cristóbal Pardo


### Opción 2: Microservicio de Reseñas
- **Descripción:** Permite registrar reseñas y notas de los lugares visitados.  
- **Alcance:**  
  - Cada reseña debe estar asociada a un destino (Destination Service).  
  - El usuario autenticado (Login Service) debe ser el autor de la reseña.  
- **Ejemplo:**  
  - Usuario: Juan Pérez → Reseña: "Hotel Plaza" → Nota: 4/5, Comentario: "Muy buena atención".  

---

###### Arquitectura de Microservicios - Proyecto DUOC ######

Este proyecto implementa una arquitectura basada en **Spring Boot + Spring Cloud** con servicios independientes, gestionados mediante **Eureka Server** y expuestos a través de un **API Gateway**.  
Cada servicio tiene su propia base de datos MySQL, orquestada con **Docker Compose**.

---

## Servicios Actuales

### 1. Eureka Server
- **ArtifactId:** `eurekaserver`
- **Puerto:** `8761` (por defecto, configurable en `application.yml`)
- **Tecnologías:**
  - Spring Boot 4.0.6
  - Spring Cloud Netflix Eureka Server
- **Base de datos:** No aplica

---

### 2. API Gateway
- **ArtifactId:** `gateway`
- **Puerto:** `9999` (habitual, configurable en `application.yml`)
- **Tecnologías:**
  - Spring Boot 3.2.5
  - Spring Cloud Gateway
  - Eureka Client
- **Base de datos:** No aplica

---

### 3. Login Service
- **ArtifactId:** `login`
- **Puerto:** `9000`
- **Tecnologías:**
  - Spring Boot 4.0.6
  - Spring Security + JWT
  - Spring Data JPA
  - Flyway (migraciones)
  - Eureka Client
  - Swagger (OpenAPI)
- **Base de datos:** MySQL (`mysql_login`)
  - Host: `localhost`
  - Puerto: `3310`
  - DB: `login`
  - Usuario: `examen`
  - Password: `examen123`

---

### 4. Destination Service
- **ArtifactId:** `destination`
- **Puerto:** `9001`
- **Tecnologías:**
  - Spring Boot 4.0.7
  - Spring Data JPA
  - Flyway
  - Spring WebFlux
  - Spring Security
  - Eureka Client
  - Swagger (OpenAPI)
- **Base de datos:** MySQL (`mysql_destination`)
  - Host: `localhost`
  - Puerto: `3311`
  - DB: `destination`
  - Usuario: `examen`
  - Password: `examen123`

---

### 5. Traveler Service
- **ArtifactId:** `traveler`
- **Puerto:** `9002`
- **Tecnologías:**
  - Spring Boot
  - Spring Data JPA
  - Spring WebClient (para consumir otros servicios)
  - Eureka Client
  - Swagger (OpenAPI)
  - **Base de datos:** MySQL (`mysql_traveler`)
  - Host: `localhost`
  - Puerto: `3312`
  - DB: `traveler`
  - Usuario: `examen`
  - Password: `examen123`

---

### 6. Resenas Service
- **ArtifactId:** `resenas`
- **Puerto:** `9003`
- **Tecnologías:**
  - Spring Boot 4.0.7
  - Spring Data JPA
  - Flyway
  - Spring WebFlux (WebClient para consumir Login y Destination)
  - Spring Security + JWT (validación de token)
  - Eureka Client
  - Swagger (OpenAPI)
- **Base de datos:** MySQL (`mysql_resenas`)
  - Host: `localhost`
  - Puerto: `3313`
  - DB: `resenas`
  - Usuario: `examen`
  - Password: `examen123`
- **Responsabilidad:** Registrar reseñas y notas de los lugares/destinos visitados. Cada reseña se asocia a un destino (Destination Service) y al usuario autenticado (Login Service).

---

## Bases de Datos (Docker Compose)

- **mysql_login** → puerto `3310`, DB `login`
- **mysql_destination** → puerto `3311`, DB `destination`
- **mysql_traveler** → puerto `3312`, DB `traveler`
- **mysql_resenas** → puerto `3313`, DB `resenas`

Cada servicio tiene su propia base de datos aislada, con usuario `examen` y contraseña `examen123`.

---

## Flujo General

1. **Eureka Server** registra todos los microservicios.  
2. **API Gateway** expone las rutas y enruta las peticiones hacia cada servicio.  
3. **Login Service** gestiona autenticación y generación de tokens JWT.  
4. **Destination Service** administra destinos y validaciones.  
5. **Traveler Service** consume Login y Destination para crear y listar viajes.  
6. **Resenas Service** consume Login (valida token/autor) y Destination (valida destino) para registrar y consultar reseñas.

---

## Estado Actual
- 6 microservicios activos (Eureka, Gateway, Login, Destination, Traveler, Resenas).  
- 4 bases de datos MySQL independientes.  
- Seguridad basada en **JWT**.  
- Documentación de APIs con **Swagger/OpenAPI**.  



---

## Microservicio de Reseñas (Opción 2) — Documentación técnica


en proceso