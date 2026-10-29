package ar.edu.utn.solicitudes;

/**
 * Datos de entrada para registrar una solicitud (Introduce Parameter Object).
 * Agrupa los valores tal como los ingresa el usuario, antes de validarlos.
 */
public record DatosSolicitud(
        String legajo,
        String nombre,
        String email,
        String tipo,
        int prioridad,
        String descripcion,
        String fecha) {
}
