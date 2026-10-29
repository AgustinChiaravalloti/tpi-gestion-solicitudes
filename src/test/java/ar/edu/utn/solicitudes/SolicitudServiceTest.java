package ar.edu.utn.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del servicio con un repositorio en memoria.
 * Gracias a la inversion de dependencias (refactoring 9.6) no se toca ningun archivo.
 */
class SolicitudServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 10, 1);

    private SolicitudService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new SolicitudService(new RepositorioSolicitudesEnMemoria(), CatalogoResponsables.predeterminado());
    }

    private DatosSolicitud datos(String legajo, String nombre, String email, String tipo,
                                 int prioridad, String descripcion) {
        return new DatosSolicitud(legajo, nombre, email, tipo, prioridad, descripcion, FECHA);
    }

    private DatosSolicitud certificadoDeAna() {
        return datos("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", 2, "Certificado de alumno regular");
    }

    private void verificarError(String mensajeEsperado, DatosSolicitud datos) {
        SolicitudException error = assertThrows(SolicitudException.class, () -> servicio.registrar(datos));
        assertEquals(mensajeEsperado, error.getMessage());
        assertEquals(0, servicio.listarTodas().size());
    }

    @Test
    @DisplayName("Registra solicitudes con ids consecutivos")
    void registraConIdsConsecutivos() {
        Solicitud primera = servicio.registrar(certificadoDeAna());
        Solicitud segunda = servicio.registrar(
                datos("1002", "Juan Paz", "juan@mail.com", "EQUIVALENCIA", 3, "Equivalencia de Algebra I"));

        assertEquals(1, primera.getId());
        assertEquals(2, segunda.getId());
        assertEquals(2, servicio.listarTodas().size());
    }

    @Test
    @DisplayName("Valida cada dato de entrada con su mensaje")
    void validaLosDatos() {
        verificarError("legajo vacio",
                datos("", "Ana Perez", "ana@mail.com", "CERTIFICADO", 2, "Certificado de alumno regular"));
        verificarError("nombre vacio",
                datos("1001", "", "ana@mail.com", "CERTIFICADO", 2, "Certificado de alumno regular"));
        verificarError("email invalido",
                datos("1001", "Ana Perez", "ana.mail.com", "CERTIFICADO", 2, "Certificado de alumno regular"));
        verificarError("tipo invalido",
                datos("1001", "Ana Perez", "ana@mail.com", "BAJA_MATERIA", 2, "Certificado de alumno regular"));
        verificarError("prioridad invalida",
                datos("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", 7, "Certificado de alumno regular"));
        verificarError("la descripcion debe tener al menos 10 caracteres",
                datos("1001", "Ana Perez", "ana@mail.com", "CERTIFICADO", 2, "corta"));
    }

    @Test
    @DisplayName("Un legajo existente reutiliza al mismo estudiante")
    void reutilizaEstudiante() {
        Solicitud primera = servicio.registrar(certificadoDeAna());
        Solicitud segunda = servicio.registrar(
                datos("1001", "Otro Nombre", "otro@mail.com", "CERTIFICADO", 2, "Certificado analitico parcial"));

        assertEquals(primera.getEstudiante(), segunda.getEstudiante());
        assertEquals("Ana Perez", segunda.getEstudiante().getNombre());
    }

    @Test
    @DisplayName("Asigna responsable y cambia de estado")
    void asignaYCambiaEstado() {
        servicio.registrar(certificadoDeAna());

        Solicitud asignada = servicio.asignarResponsable(1, 2);
        Solicitud enProceso = servicio.cambiarEstado(1, "EN_PROCESO");

        assertEquals("Martin Ruiz", asignada.getResponsable().getNombre());
        assertEquals(EstadoSolicitud.EN_PROCESO, enProceso.getEstado());
    }

    @Test
    @DisplayName("Informa solicitudes, responsables y estados inexistentes")
    void informaInexistentes() {
        servicio.registrar(certificadoDeAna());

        assertEquals("no existe la solicitud 99",
                assertThrows(SolicitudException.class, () -> servicio.asignarResponsable(99, 1)).getMessage());
        assertEquals("no existe el responsable 9",
                assertThrows(SolicitudException.class, () -> servicio.asignarResponsable(1, 9)).getMessage());
        assertEquals("no se puede pasar de PENDIENTE a TERMINADA",
                assertThrows(SolicitudException.class, () -> servicio.cambiarEstado(1, "TERMINADA")).getMessage());
    }

    @Test
    @DisplayName("Filtra por estado, tipo y estudiante")
    void filtra() {
        servicio.registrar(certificadoDeAna());
        servicio.registrar(datos("1002", "Juan Paz", "juan@mail.com", "EQUIVALENCIA", 3, "Equivalencia de Algebra I"));
        servicio.cambiarEstado(2, "RECHAZADA");

        List<Solicitud> rechazadas = servicio.filtrarPorEstado(EstadoSolicitud.RECHAZADA);
        List<Solicitud> certificados = servicio.filtrarPorTipo(TipoSolicitud.CERTIFICADO);
        List<Solicitud> deAna = servicio.filtrarPorEstudiante("1001");

        assertEquals(1, rechazadas.size());
        assertEquals(2, rechazadas.get(0).getId());
        assertEquals(1, certificados.size());
        assertEquals(1, deAna.size());
        assertEquals(0, servicio.filtrarPorEstudiante("9999").size());
    }
}
