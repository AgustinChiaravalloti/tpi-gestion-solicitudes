package ar.edu.utn.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del comportamiento polimorfico de los enums (refactoring 9.3).
 */
class EnumsTest {

    @Test
    @DisplayName("Cada tipo conoce su plazo base")
    void cadaTipoConoceSuPlazoBase() {
        assertEquals(5, TipoSolicitud.CERTIFICADO.getDiasPlazoBase());
        assertEquals(30, TipoSolicitud.EQUIVALENCIA.getDiasPlazoBase());
        assertEquals(10, TipoSolicitud.CAMBIO_COMISION.getDiasPlazoBase());
        assertEquals(7, TipoSolicitud.RECLAMO_NOTA.getDiasPlazoBase());
    }

    @Test
    @DisplayName("Solo el reclamo de nota fuerza la prioridad alta")
    void soloElReclamoDeNotaFuerzaPrioridadAlta() {
        assertEquals(Prioridad.ALTA, TipoSolicitud.RECLAMO_NOTA.prioridadPara(Prioridad.BAJA));
        assertEquals(Prioridad.BAJA, TipoSolicitud.CERTIFICADO.prioridadPara(Prioridad.BAJA));
        assertEquals(Prioridad.MEDIA, TipoSolicitud.EQUIVALENCIA.prioridadPara(Prioridad.MEDIA));
    }

    @Test
    @DisplayName("Solo la prioridad alta reduce el plazo a la mitad")
    void soloLaPrioridadAltaReduceElPlazo() {
        assertEquals(15, Prioridad.ALTA.ajustarPlazo(30));
        assertEquals(3, Prioridad.ALTA.ajustarPlazo(7));
        assertEquals(30, Prioridad.MEDIA.ajustarPlazo(30));
        assertEquals(30, Prioridad.BAJA.ajustarPlazo(30));
    }

    @Test
    @DisplayName("La prioridad se obtiene a partir de su codigo")
    void prioridadDesdeCodigo() {
        assertEquals(Optional.of(Prioridad.ALTA), Prioridad.desdeCodigo(1));
        assertEquals(Optional.of(Prioridad.BAJA), Prioridad.desdeCodigo(3));
        assertTrue(Prioridad.desdeCodigo(4).isEmpty());
    }

    @Test
    @DisplayName("Transiciones permitidas desde PENDIENTE")
    void transicionesDesdePendiente() {
        assertTrue(EstadoSolicitud.PENDIENTE.puedePasarA(EstadoSolicitud.EN_PROCESO));
        assertTrue(EstadoSolicitud.PENDIENTE.puedePasarA(EstadoSolicitud.RECHAZADA));
        assertFalse(EstadoSolicitud.PENDIENTE.puedePasarA(EstadoSolicitud.RESUELTA));
        assertFalse(EstadoSolicitud.PENDIENTE.puedePasarA(EstadoSolicitud.PENDIENTE));
    }

    @Test
    @DisplayName("Transiciones permitidas desde EN_PROCESO")
    void transicionesDesdeEnProceso() {
        assertTrue(EstadoSolicitud.EN_PROCESO.puedePasarA(EstadoSolicitud.RESUELTA));
        assertTrue(EstadoSolicitud.EN_PROCESO.puedePasarA(EstadoSolicitud.RECHAZADA));
        assertFalse(EstadoSolicitud.EN_PROCESO.puedePasarA(EstadoSolicitud.PENDIENTE));
    }

    @Test
    @DisplayName("RESUELTA y RECHAZADA son estados finales")
    void estadosFinales() {
        for (EstadoSolicitud destino : EstadoSolicitud.values()) {
            assertFalse(EstadoSolicitud.RESUELTA.puedePasarA(destino));
            assertFalse(EstadoSolicitud.RECHAZADA.puedePasarA(destino));
        }
        assertTrue(EstadoSolicitud.RESUELTA.esFinal());
        assertTrue(EstadoSolicitud.RECHAZADA.esFinal());
        assertFalse(EstadoSolicitud.PENDIENTE.esFinal());
        assertFalse(EstadoSolicitud.EN_PROCESO.esFinal());
    }

    @Test
    @DisplayName("Solo EN_PROCESO requiere responsable")
    void soloEnProcesoRequiereResponsable() {
        assertTrue(EstadoSolicitud.EN_PROCESO.requiereResponsable());
        assertFalse(EstadoSolicitud.PENDIENTE.requiereResponsable());
        assertFalse(EstadoSolicitud.RESUELTA.requiereResponsable());
        assertFalse(EstadoSolicitud.RECHAZADA.requiereResponsable());
    }

    @Test
    @DisplayName("Los textos desconocidos no se convierten en tipo ni estado")
    void textosDesconocidos() {
        assertEquals(Optional.of(TipoSolicitud.CERTIFICADO), TipoSolicitud.desdeTexto("CERTIFICADO"));
        assertTrue(TipoSolicitud.desdeTexto("CERTIFCADO").isEmpty());
        assertTrue(EstadoSolicitud.desdeTexto("PENDENTE").isEmpty());
    }
}
