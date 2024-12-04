# Proyecto de Mensajería

Este proyecto es una aplicación de mensajería que utiliza microservicios para enviar y procesar mensajes. La aplicación está compuesta por tres microservicios principales: `message-sql`, `message-mongo`.

## Estructura del Proyecto

- `message-sql`: Microservicio que maneja las solicitudes REST y envía mensajes a través de RabbitMQ.
- `message-mongo`: Microservicio que consume mensajes de RabbitMQ y los almacena en MongoDB.

## Requisitos

- Java 11 o superior
- Maven 3.6.3 o superior
- RabbitMQ
- MySQL
- MongoDB

## Configuración

### Configuración de MySQL

1. Crea una base de datos en MySQL aunque el proyecto ya genera una db creala con el nombre `linea_de_origen`:
    ```sql
    CREATE DATABASE mensajeria;

    CREATE TABLE line_origin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    origin_number VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
    );

    INSERT INTO line_origin (origin_number, description) VALUES
    ('12345', 'Línea de origen 1'),
    ('67890', 'Línea de origen 2'),
    ('11111', 'Línea de origen 3'),
    ('22222', 'Línea de origen 4'),
    ('33333', 'Línea de origen 5');
    ```

2. Configura las propiedades de conexión en `application.properties`:
    ```properties
    # Configuración para la base de datos MySQL
    spring.datasource.url=jdbc:mysql://localhost:3306/mensajeria
    spring.datasource.username=root
    spring.datasource.password=root
    spring.jpa.hibernate.ddl-auto=update

    # Configuración para la base de datos MongoDB
    spring.data.mongodb.uri=mongodb://localhost:27017/mensajeria

    # Configuración para RabbitMQ
    spring.rabbitmq.host=localhost
    spring.rabbitmq.port=5672
    spring.rabbitmq.username=guest
    spring.rabbitmq.password=guest
    ```

## Uso de la API

### Enviar un Mensaje

Para enviar un mensaje, realiza una solicitud POST al endpoint `/api/messages` del microservicio `message-sql` con el siguiente cuerpo JSON:

```http
POST /api/messages
```

Esta API utiliza autenticación básica para asegurar los endpoints.

Credenciales:
- Nombre de usuario: user
- Contraseña: password

```json
{
  "origin": "12345",
  "destination": "67890",
  "messageType": "VIDEO",
  "content": "Este es un mensaje de prueba."
}
```

### Consultar Mensajes

Para consultar los mensajes almacenados en MongoDB, realiza una solicitud GET al endpoint `/api/messages` del microservicio `message-mongo`. Puedes filtrar los mensajes por origen o destino utilizando parámetros de consulta:

```http
GET /api/messages/destination/67890
```

### Ejemplo de Respuesta

La respuesta a una solicitud GET para consultar mensajes tendrá el siguiente formato:

```json
[
    {
        "id": "60c72b2f9b1e8a5f3c8e4b8a",
        "origin": "12345",
        "destination": "67890",
        "content": "Hola, este es un mensaje de prueba.",
        "timestamp": "2023-10-01T12:34:56.789Z"
    },
    {
        "id": "60c72b2f9b1e8a5f3c8e4b8b",
        "origin": "12345",
        "destination": "67890",
        "content": "Otro mensaje de prueba.",
        "timestamp": "2023-10-01T12:35:56.789Z"
    }
]
```
# PREGUNTAS [OPCIONALES]
## Requisitos No Funcionales Considerados

1. **Seguridad**:
    - **Autenticación y Autorización**: Implementación de autenticación básica para asegurar que solo usuarios autorizados puedan enviar mensajes y acceder a la API.
    - **Protección CSRF**: Deshabilitación de CSRF para simplificar la configuración en un entorno de API REST.
2. **Mantenibilidad**:
    - **Inyección de Dependencias**: Uso de Spring para la inyección de dependencias, lo que facilita la prueba y el mantenimiento del código.
    - **Modularidad**: Separación del código en diferentes microservicios (message-sql, message-mongo) para mejorar la mantenibilidad y la escalabilidad.
3. **Escalabilidad**:
    - **Microservicios**: Uso de una arquitectura de microservicios para permitir la escalabilidad horizontal.
    - **RabbitMQ**: Uso de RabbitMQ para la mensajería asíncrona, lo que permite manejar grandes volúmenes de mensajes de manera eficiente.
4. **Disponibilidad**:
    - **Persistencia en MongoDB y MySQL**: Uso de bases de datos robustas y escalables para asegurar la disponibilidad de los datos.
5. **Rendimiento**:
    - **Procesamiento Asíncrono**: Uso de RabbitMQ para el procesamiento asíncrono de mensajes.
6. **Trazabilidad y Monitoreo**:
    - **Logs**: Uso de logs para rastrear el flujo de mensajes y detectar errores.
    - **Timestamp en Headers**: Inclusión de un timestamp en los headers de los mensajes para calcular el tiempo total de procesamiento.

## Requisitos No Funcionales que Faltaron Incluir

1. **Seguridad Avanzada**:
    - **Encriptación de Datos**: Encriptación de datos sensibles tanto en tránsito como en reposo.
    - **Autenticación y Autorización Avanzada**: Uso de OAuth2 o JWT para una autenticación y autorización más robusta. (No implementada, requiero investigar la implementación, encriptación básica implementada)

## Implementación de Patrones de Diseño

Más que utilizar un patrón de diseño específico, me enfoqué en un diseño de arquitectura como práctica propia para poner a prueba ciertos conocimientos ya que Spring utiliza internamente algunos patrones como lo son singleton para los beans.

En general, utilicé los siguientes patrones:

- **Patrón de Comportamiento**:
  - Inyección de dependencia: `@Autowired`, `@Bean`.
  - Repository: `@Repository`.
  - Consumidor como servicio de bróker: `@RabbitListener`.
- **Patrón de Arquitectura**:
  - Controlador: `@Controller`, `@RequestMapping`, `@RestController`.
  - Configuración: `@Configuration`.

Con este enfoque, me di cuenta que manejando un proyecto modular como lo es **message-api** y de este generando las dependencias para ser consumidas por los microservicios **message-mongo, message-sql**, me permite tener una construcción de una aplicación robusta y mantenible en cuanto a integración.
