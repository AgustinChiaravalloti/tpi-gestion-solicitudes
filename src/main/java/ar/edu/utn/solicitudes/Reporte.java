package ar.edu.utn.solicitudes;

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
