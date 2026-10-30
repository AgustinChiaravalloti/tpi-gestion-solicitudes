# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador — **Metodología de Sistemas II**
Tecnicatura Universitaria en Programación a Distancia — UTN

## Integrantes

- Agustin Chiaravalloti
- Franco Ghilardi

## Descripción

Aplicación de consola que permite registrar solicitudes académicas de estudiantes, clasificarlas por tipo, prioridad y estado, asignarles un responsable, consultarlas mediante filtros y generar un reporte básico.

El objetivo del trabajo no es agregar funcionalidad, sino **diagnosticar y mejorar la calidad interna** de una versión inicial con problemas de diseño. Se aplicaron principios SOLID, Clean Code y refactorings seguros, y se verificó con pruebas automatizadas que el comportamiento externo se conservara.

| | Versión inicial (`estado-inicial`) | Versión refactorizada (`estado-refactorizado`) |
|---|---|---|
| Estructura | 5 clases en un paquete; una "God Class" de 334 líneas con 7 responsabilidades | 20 clases en 4 paquetes por capa, una responsabilidad por clase |
| Abstracciones | Ninguna | 2 interfaces (repositorio de lectura y de escritura) |
| Tipos, estados y prioridades | `String` e `int` repetidos en 55 lugares | Enums con comportamiento propio |
| Manejo de errores | 2 `catch` vacíos | Excepciones propias con mensajes claros |
| Tests automatizados | 22 (caracterización) | 63 (los mismos 22 sin cambios + 41 unitarios y de integración) |
| Defectos externos conocidos | 3 | 0 |

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
- Una solicitud está **vencida** si sigue abierta después de su fecha límite (fecha de alta + plazo).

## Arquitectura

```
consola ──────► servicio ──────► dominio
                   ▲                ▲
persistencia ──────┘────────────────┘
```

| Paquete | Responsabilidad | Clases principales |
|---------|-----------------|--------------------|
| `dominio` | Entidades y reglas de negocio. No depende de ningún otro paquete. | `Solicitud`, `Estudiante`, `Responsable`, `TipoSolicitud`, `EstadoSolicitud`, `Prioridad`, `CatalogoResponsables` |
| `servicio` | Casos de uso y las interfaces que necesita para guardar datos. | `SolicitudService`, `GeneradorReporte`, `SolicitudRepository`, `ConsultaSolicitudes` |
| `persistencia` | Implementaciones del repositorio (archivo CSV y memoria). | `RepositorioSolicitudesCsv`, `RepositorioSolicitudesEnMemoria`, `FormatoCsv` |
| `consola` | Interfaz de usuario: lectura, validación de entrada e impresión. | `MenuConsola` |
| (raíz) | Punto de entrada: arma las dependencias y arranca el menú. | `Main` |

La persistencia **implementa** las interfaces definidas en `servicio`, por lo que la lógica de negocio no depende del archivo (principio de inversión de dependencias).

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

Los datos se guardan en `solicitudes.csv`, en la carpeta desde donde se ejecuta el programa. Todos los datos utilizados son **sintéticos**.

## Pruebas

```bash
mvnw.cmd test      # Windows
./mvnw test        # Linux / Mac
```

| Clase de test | Tipo | Tests | Qué verifica |
|---------------|------|:-----:|--------------|
| `ComportamientoSistemaTest` | Caracterización (caja negra) | 22 | El sistema completo a través del menú. **El mismo archivo pasa sin cambios sobre `estado-inicial` y `estado-refactorizado`.** |
| `EnumsTest` | Unitario | 10 | Plazos, prioridades y transiciones de estado |
| `SolicitudTest` | Unitario | 11 | Reglas de la entidad: vencimiento, cierre, cambios de estado |
| `SolicitudServiceTest` | Unitario | 6 | Casos de uso y validaciones, con repositorio en memoria |
| `GeneradorReporteTest` | Unitario | 2 | Reporte con un reloj fijo |
| `FormatoCsvTest` | Unitario | 4 | Formato del archivo y escape del separador |
| `RepositorioSolicitudesCsvTest` | Integración | 4 | Persistencia y archivos dañados (defecto D-03) |
| `MenuConsolaTest` | Integración | 4 | Entrada inválida del usuario (defectos D-01 y D-02) |
| **Total** | | **63** | |

Para verificar la conservación del comportamiento sobre la versión inicial:

```bash
git checkout estado-inicial
mvnw.cmd test        # 22 tests en verde
git checkout main
mvnw.cmd test        # 63 tests en verde, incluidos los mismos 22
```

## Versiones del sistema

| Tag | Descripción |
|-----|-------------|
| `estado-inicial` | Versión base con problemas de diseño a diagnosticar |
| `etapa-diagnostico` | Diagnóstico de calidad y code smells documentado |
| `etapa-refactoring` | Siete refactorings aplicados |
| `etapa-pruebas` | Verificación: tests de caracterización y tests unitarios |
| `etapa-docs` | Code review y documentación final |
| `estado-refactorizado` | Versión final mejorada |

## Documentación

| Archivo | Contenido |
|---------|-----------|
| `docs/diagnostico.md` | Calidad externa vs. interna, 12 code smells y plan de corrección |
| `docs/code-review.md` | Checklist técnico, observaciones, correcciones y criterios de aceptación |
| `docs/decisiones-tecnicas.md` | Decisiones de diseño y su justificación |
| `docs/registro-ia.md` | Registro del uso de inteligencia artificial |
| `docs/evidencias/inicial/` | Capturas de la versión inicial y de sus defectos |
| `docs/evidencias/refactorizado/` | Capturas de la versión final y de las correcciones |
