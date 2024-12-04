# Proyecto de Mensajería

Este proyecto es una aplicación de mensajería que utiliza microservicios para enviar y procesar mensajes. La aplicación está compuesta por dos microservicios principales: `message-sql`, `message-mongo`.

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

## Componentes de la aplicacion orientadas a reducir carga operativa y reducir el minimo de recursos
1. **Mensajería Asíncrona**:
    - **RabbitMQ**: Utilizamos RabbitMQ para manejar la mensajería asíncrona, lo que permite procesar grandes volúmenes de mensajes sin bloquear las operaciones principales.
2. **Escalabilidad Horizontal**:
    - **Microservicios**: Se divide la aplicación en microservicios independientes que pueden escalarse horizontalmente según el punto 5 de los requisitos.
3. **Optimización de la Base de Datos**:
    - **Consulta Eficiente y simple**: tenemos una consultas SQL para reducir el tiempo de ejecución y el uso de recursos que es simple y al caso de utilizacion de las lineas disponibles.

## Fase evolutiva de la solucion orientada a la refactorizacion
1. **Mejora de la Seguridad**:
    - **Implementación de OAuth2 o JWT**: Me gustaria migrar a un sistema de autenticación y autorización más robusto utilizando OAuth2 o JWT pero pora hora esta en basic auth.
    - **Encriptación de Datos**: Implementar encriptación de datos sensibles tanto en tránsito como en reposo esta practica me requiere un tiempo debido a que estoy desactualizado en cuanto al funcionamiento de estos y la integración.

## Estrategia de despliegue para optimizar y dar solucion (propuesta)

Docker es una alternativa optima, se puede realizar una configuracion con contenedores utilizando docker-compose para una estrategia que me permita gestionar los recursos y el despliegue, utilizando una interfaz como portainer.

## Mecanismo de seguridad

se han implementado mecanismos de seguridad como la autenticación básica HTTP, el cifrado de contraseñas con BCrypt y la deshabilitación de CSRF para proteger la integridad y confidencialidad de los datos en la aplicación message-api. Estos mecanismos aseguran que solo los usuarios autenticados puedan acceder a los datos y que las contraseñas estén protegidas contra accesos no autorizados.

## Monitorear el rendimiento y estabilidad con portainer (propuesta)

Portainer es una herramienta óptima para gestionar y monitorear contenedores Docker. Proporcionandonos una interfaz gráfica de usuario (GUI) que facilita la administración de los recursos y el despliegue de la aplicación.

Basicamente Portainer es una herramienta poderosa para monitorear el rendimiento y la estabilidad de la solución message-api. Proporciona una interfaz gráfica fácil de usar para gestionar contenedores Docker, visualizar el uso de recursos, acceder a logs y métricas, y asegurar la aplicación mediante la gestión de usuarios y políticas de seguridad. Utilizando Portainer, se puede asegurar que la solución opere de manera eficiente y estable en un entorno productivo.

## ¿Cómo garantiza la escalabilidad de la solucion para grandes volumenes de trabajo?

1. **Microservicios**: la aplicacion se divide en microservicios que facilitan escalar cada componente de maner independiente se gun los requisitos.

2. **RabbitMQ**: Utilizamos RabbitMQ para manejar grandes volumenes de mensajes sin que bloquee la aplicacion segun la operatividad del consumer.

## Pruebas para validar la solucion que cumpla con los requisitos funcionales

1. **Workflows de CI/CD**:
    - **GitHub Actions**: Se han configurado workflows de CI/CD en GitHub Actions para automatizar la ejecución de pruebas unitarias, de integración y de carga en cada commit y pull request. Esto asegura que cualquier cambio en el código sea validado automáticamente antes de ser fusionado en la rama principal.

## Solucion facil de mantener y extender a futuro

1. **Modularidad y Arquitectura**:
    - **Inyección de Dependencias Spring Framework**: Se utiliza Spring para la inyección de dependencias, lo que facilita la prueba y el mantenimiento del código al desacoplar los componentes y permitir la sustitución de implementaciones.
    Workflows de CI/CD:

    - **GitHub Actions**: Configuración de workflows de CI/CD en GitHub Actions para automatizar la construcción, prueba y despliegue de la aplicación. Esto asegura que cualquier cambio en el código sea validado automáticamente antes de ser fusionado en la rama principal.

    - **Patrones de Diseño**:
        - **Patrones de Comportamiento**: Uso de patrones como Inyección de Dependencias (@Autowired, @Bean), Repository (@Repository), y Consumidor de Mensajes (@RabbitListener).
        - **Patrones de Arquitectura**: Uso de patrones como Controlador (@Controller, @RequestMapping, @RestController) y Configuración (@Configuration).

