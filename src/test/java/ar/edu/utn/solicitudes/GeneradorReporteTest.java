package ar.edu.utn.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import ar.edu.utn.solicitudes.dominio.*;
import ar.edu.utn.solicitudes.persistencia.RepositorioSolicitudesEnMemoria;
import ar.edu.utn.solicitudes.servicio.GeneradorReporte;
import ar.edu.utn.solicitudes.servicio.Reporte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del reporte con un reloj fijo: el resultado no depende del dia en que se corre.
 */
class GeneradorReporteTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final Estudiante ANA = new Estudiante("1001", "Ana Perez", "ana@mail.com");

    private Clock relojEn(LocalDate dia) {
        return Clock.fixed(dia.atStartOfDay(ZONA).toInstant(), ZONA);
    }

    private Solicitud solicitud(int id, TipoSolicitud tipo, LocalDate fecha) {
        return new Solicitud(id, tipo, Prioridad.MEDIA, "Descripcion de prueba", fecha, ANA);
    }

    @Test
    @DisplayName("Cuenta por estado y por tipo, incluidos los que estan en cero")
    void cuentaPorEstadoYTipo() {
        RepositorioSolicitudesEnMemoria repositorio = new RepositorioSolicitudesEnMemoria();
        repositorio.guardar(solicitud(1, TipoSolicitud.CERTIFICADO, LocalDate.of(2026, 10, 1)));
        repositorio.guardar(solicitud(2, TipoSolicitud.CERTIFICADO, LocalDate.of(2026, 10, 1)));
        Solicitud rechazada = solicitud(3, TipoSolicitud.EQUIVALENCIA, LocalDate.of(2026, 10, 1));
        rechazada.cambiarEstado(EstadoSolicitud.RECHAZADA);
        repositorio.guardar(rechazada);

        Reporte reporte = new GeneradorReporte(repositorio, relojEn(LocalDate.of(2026, 10, 2))).generar();

        assertEquals(3, reporte.total());
        assertEquals(2, reporte.cantidadPorEstado().get(EstadoSolicitud.PENDIENTE).intValue());
        assertEquals(1, reporte.cantidadPorEstado().get(EstadoSolicitud.RECHAZADA).intValue());
        assertEquals(0, reporte.cantidadPorEstado().get(EstadoSolicitud.EN_PROCESO).intValue());
        assertEquals(2, reporte.cantidadPorTipo().get(TipoSolicitud.CERTIFICADO).intValue());
        assertEquals(0, reporte.cantidadPorTipo().get(TipoSolicitud.RECLAMO_NOTA).intValue());
    }

    @Test
    @DisplayName("Las vencidas dependen del dia que marca el reloj")
    void vencidasSegunElReloj() {
        RepositorioSolicitudesEnMemoria repositorio = new RepositorioSolicitudesEnMemoria();
        repositorio.guardar(solicitud(1, TipoSolicitud.CERTIFICADO, LocalDate.of(2026, 10, 1)));

        Reporte enElLimite = new GeneradorReporte(repositorio, relojEn(LocalDate.of(2026, 10, 6))).generar();
        Reporte unDiaDespues = new GeneradorReporte(repositorio, relojEn(LocalDate.of(2026, 10, 7))).generar();

        assertEquals(0, enElLimite.vencidas().size());
        assertEquals(1, unDiaDespues.vencidas().size());
    }
}
