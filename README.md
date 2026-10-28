# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador — **Metodología de Sistemas II**
Tecnicatura Universitaria en Programación a Distancia — UTN

## Integrantes

- Agustin Chiaravalloti
- Franco Ghilardi

## Descripción

Aplicación de consola que permite registrar solicitudes académicas de estudiantes, clasificarlas por tipo, prioridad y estado, asignarles un responsable, consultarlas mediante filtros y generar un reporte básico.

El objetivo del trabajo no es agregar funcionalidad, sino **diagnosticar y mejorar la calidad interna** de una versión inicial con problemas de diseño, aplicando principios SOLID, Clean Code y refactorings seguros, y verificando que el comportamiento externo se mantenga.

## Funcionalidades

| # | Operación | Descripción |
|---|-----------|-------------|
| 1 | Registrar solicitud | Alta de una solicitud con estudiante, tipo, prioridad, descripción y fecha. Calcula el plazo de resolución según el tipo. |
| 2 | Asignar responsable | Asigna un responsable de un área a una solicitud abierta. |
| 3 | Cambiar estado | Cambia el estado respetando las transiciones válidas. |
| 4 | Buscar / filtrar | Lista todas las solicitudes o las filtra por estado, tipo o estudiante. |
| 5 | Reporte | Cantidad de solicitudes por estado y por tipo, y listado de solicitudes vencidas. |

### Reglas de negocio

- **Tipos y plazos:** Certificado (5 días), Equivalencia (30), Cambio de comisión (10), Reclamo de nota (7).
- **Prioridad alta** reduce el plazo a la mitad.
- Los **reclamos de nota** siempre tienen prioridad alta.
- **Transiciones de estado:** PENDIENTE → EN_PROCESO → RESUELTA; PENDIENTE o EN_PROCESO → RECHAZADA.
- Para pasar a **EN_PROCESO** la solicitud debe tener responsable asignado.
- Una solicitud **RESUELTA** o **RECHAZADA** queda cerrada.

## Entidades

- `Estudiante` — legajo, nombre, email.
- `Responsable` — id, nombre, área.
- `Solicitud` — id, tipo, prioridad, estado, descripción, fecha, estudiante, responsable.

## Tecnologías

- Java 17
- Maven
- JUnit 5
- Persistencia en archivo CSV (`solicitudes.csv`)

## Instalación y uso

### Requisitos

- JDK 17 o superior.
- No hace falta instalar Maven: el proyecto incluye el wrapper (`mvnw`).

### Desde IntelliJ IDEA

1. Abrir la carpeta del proyecto (**File → Open**).
2. Esperar a que Maven descargue las dependencias.
3. Ejecutar la clase `ar.edu.utn.solicitudes.Main`.

### Desde la terminal

Windows:

```bash
mvnw.cmd compile
java -cp target\classes ar.edu.utn.solicitudes.Main
```

Linux / Mac:

```bash
./mvnw compile
java -cp target/classes ar.edu.utn.solicitudes.Main
```

Para correr los tests:

```bash
mvnw.cmd test      # Windows
./mvnw test        # Linux / Mac
```

Los datos se guardan en `solicitudes.csv`, en la carpeta desde donde se ejecuta el programa. Todos los datos utilizados son **sintéticos**.

## Versiones del sistema

| Tag | Descripción |
|-----|-------------|
| `estado-inicial` | Versión base con problemas de diseño a diagnosticar |
| `etapa-diagnostico` | Diagnóstico de calidad y code smells documentado |
| `etapa-refactoring` | Refactorings aplicados |
| `etapa-pruebas` | Verificación del comportamiento |
| `etapa-docs` | Documentación final |
| `estado-refactorizado` | Versión final mejorada |

## Documentación

La carpeta `docs/` contiene el registro de uso de IA, las evidencias de funcionamiento, el diagnóstico, el checklist de code review y las decisiones técnicas.