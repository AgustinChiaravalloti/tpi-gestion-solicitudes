package ar.edu.utn.solicitudes;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula el reporte de solicitudes. Solo necesita consultar (ISP) y recibe
 * el reloj desde afuera, para poder probarlo con una fecha fija.
 */
public class GeneradorReporte {

    private final ConsultaSolicitudes solicitudes;
    private final Clock reloj;

    public GeneradorReporte(ConsultaSolicitudes solicitudes, Clock reloj) {
        this.solicitudes = solicitudes;
        this.reloj = reloj;
    }

    public Reporte generar() {
        List<Solicitud> todas = solicitudes.listarTodas();
        LocalDate hoy = LocalDate.now(reloj);
        return new Reporte(
                todas.size(),
                contarPorEstado(todas),
                contarPorTipo(todas),
                todas.stream().filter(solicitud -> solicitud.estaVencida(hoy)).toList());
    }

    private Map<EstadoSolicitud, Integer> contarPorEstado(List<Solicitud> todas) {
        Map<EstadoSolicitud, Integer> cantidades = new EnumMap<>(EstadoSolicitud.class);
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            cantidades.put(estado, 0);
        }
        for (Solicitud solicitud : todas) {
            cantidades.merge(solicitud.getEstado(), 1, Integer::sum);
        }
        return cantidades;
    }

    private Map<TipoSolicitud, Integer> contarPorTipo(List<Solicitud> todas) {
        Map<TipoSolicitud, Integer> cantidades = new EnumMap<>(TipoSolicitud.class);
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            cantidades.put(tipo, 0);
        }
        for (Solicitud solicitud : todas) {
            cantidades.merge(solicitud.getTipo(), 1, Integer::sum);
        }
        return cantidades;
    }
}
