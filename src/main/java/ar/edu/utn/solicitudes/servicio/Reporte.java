package ar.edu.utn.solicitudes.servicio;

import ar.edu.utn.solicitudes.dominio.EstadoSolicitud;
import ar.edu.utn.solicitudes.dominio.Solicitud;
import ar.edu.utn.solicitudes.dominio.TipoSolicitud;

import java.util.List;
import java.util.Map;

/**
 * Resultado del reporte: solo datos, sin formato de presentacion.
 */
public record Reporte(
        int total,
        Map<EstadoSolicitud, Integer> cantidadPorEstado,
        Map<TipoSolicitud, Integer> cantidadPorTipo,
        List<Solicitud> vencidas) {
}
