package ar.edu.utn.solicitudes;

import java.time.LocalDate;

/**
 * Datos de entrada para registrar una solicitud (Introduce Parameter Object).
 */
public record DatosSolicitud(
        String legajo,
        String nombre,
        String email,
        String tipo,
        int prioridad,
        String descripcion,
        LocalDate fecha) {
}
