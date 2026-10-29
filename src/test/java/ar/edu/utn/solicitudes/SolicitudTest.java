package ar.edu.utn.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del comportamiento que se movio a Solicitud (refactorings 9.2 y 9.3).
 */
class SolicitudTest {

    private static final LocalDate ALTA = LocalDate.of(2026, 10, 1);
    private static final Estudiante ANA = new Estudiante("1001", "Ana Perez", "ana@mail.com");
    private static final Responsable LAURA = new Responsable(1, "Laura Gomez", "Bedelia");

    private Solicitud certificado(Prioridad prioridad) {
        return new Solicitud(1, TipoSolicitud.CERTIFICADO, prioridad, "Certificado de alumno regular", ALTA, ANA);
    }

    @Test
    @DisplayName("Una solicitud nueva siempre arranca PENDIENTE y sin responsable")
    void nuevaSolicitudArrancaPendiente() {
        Solicitud solicitud = certificado(Prioridad.MEDIA);

        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertNull(solicitud.getResponsable());
    }

    @Test
    @DisplayName("Un reclamo de nota se crea con prioridad alta aunque se pida baja")
    void reclamoDeNotaQuedaConPrioridadAlta() {
        Solicitud reclamo = new Solicitud(1, TipoSolicitud.RECLAMO_NOTA, Prioridad.BAJA,
                "Reclamo nota del parcial", ALTA, ANA);

        assertEquals(Prioridad.ALTA, reclamo.getPrioridad());
        assertEquals(3, reclamo.diasPlazo());
    }

    @Test
    @DisplayName("La fecha limite es la fecha de alta mas el plazo")
    void fechaLimite() {
        assertEquals(LocalDate.of(2026, 10, 6), certificado(Prioridad.MEDIA).fechaLimite());
        assertEquals(LocalDate.of(2026, 10, 3), certificado(Prioridad.ALTA).fechaLimite());
    }

    @Test
    @DisplayName("Vence recien el dia siguiente a la fecha limite")
    void vencimientoEnElLimite() {
        Solicitud solicitud = certificado(Prioridad.MEDIA);

        assertFalse(solicitud.estaVencida(LocalDate.of(2026, 10, 6)));
        assertTrue(solicitud.estaVencida(LocalDate.of(2026, 10, 7)));
    }

    @Test
    @DisplayName("Una solicitud cerrada nunca esta vencida")
    void cerradaNoVence() {
        Solicitud solicitud = certificado(Prioridad.MEDIA);
        solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA);

        assertTrue(solicitud.estaCerrada());
        assertFalse(solicitud.estaVencida(LocalDate.of(2030, 1, 1)));
    }

    @Test
    @DisplayName("No pasa a EN_PROCESO sin responsable")
    void noPasaAEnProcesoSinResponsable() {
        Solicitud solicitud = certificado(Prioridad.MEDIA);

        SolicitudException error = assertThrows(SolicitudException.class,
                () -> solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO));
        assertEquals("no se puede pasar a EN_PROCESO sin responsable asignado", error.getMessage());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
    }

    @Test
    @DisplayName("Recorre el flujo completo hasta RESUELTA")
    void flujoCompleto() {
        Solicitud solicitud = certificado(Prioridad.MEDIA);
        solicitud.asignarResponsable(LAURA);
        solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO);
        solicitud.cambiarEstado(EstadoSolicitud.RESUELTA);

        assertEquals(EstadoSolicitud.RESUELTA, solicitud.getEstado());
        assertEquals(LAURA, solicitud.getResponsable());
    }

    @Test
    @DisplayName("Rechaza una transicion invalida sin cambiar el estado")
    void rechazaTransicionInvalida() {
        Solicitud solicitud = certificado(Prioridad.MEDIA);

        SolicitudException error = assertThrows(SolicitudException.class,
                () -> solicitud.cambiarEstado(EstadoSolicitud.RESUELTA));
        assertEquals("no se puede pasar de PENDIENTE a RESUELTA", error.getMessage());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
    }

    @Test
    @DisplayName("No se puede asignar responsable a una solicitud cerrada")
    void noAsignaACerrada() {
        Solicitud solicitud = certificado(Prioridad.MEDIA);
        solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA);

        assertThrows(SolicitudException.class, () -> solicitud.asignarResponsable(LAURA));
    }

    @Test
    @DisplayName("Sabe a que estudiante pertenece")
    void perteneceAEstudiante() {
        Solicitud solicitud = certificado(Prioridad.MEDIA);

        assertTrue(solicitud.perteneceA("1001"));
        assertFalse(solicitud.perteneceA("9999"));
    }
}
