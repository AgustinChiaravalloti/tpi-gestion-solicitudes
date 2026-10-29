package ar.edu.utn.solicitudes;

import java.util.List;
import java.util.function.Predicate;

/**
 * Casos de uso del sistema. No lee ni imprime nada: recibe datos, aplica las reglas
 * y devuelve resultados. Ante un error lanza SolicitudException.
 */
public class SolicitudService {

    private static final int LARGO_MINIMO_DESCRIPCION = 10;

    private final SolicitudRepository repositorio;
    private final CatalogoResponsables responsables;

    public SolicitudService(SolicitudRepository repositorio, CatalogoResponsables responsables) {
        this.repositorio = repositorio;
        this.responsables = responsables;
    }

    // ---------- operaciones ----------

    public Solicitud registrar(DatosSolicitud datos) {
        validarDatosPersonales(datos.legajo(), datos.nombre(), datos.email());
        TipoSolicitud tipo = convertirTipo(datos.tipo());
        Prioridad prioridad = convertirPrioridad(datos.prioridad());
        validarDescripcion(datos.descripcion());

        Estudiante estudiante = buscarOCrearEstudiante(datos.legajo(), datos.nombre(), datos.email());
        Solicitud solicitud = new Solicitud(repositorio.siguienteId(), tipo, prioridad,
                datos.descripcion(), datos.fecha(), estudiante);
        repositorio.guardar(solicitud);
        return solicitud;
    }

    public Solicitud asignarResponsable(int idSolicitud, int idResponsable) {
        Solicitud solicitud = buscarSolicitud(idSolicitud);
        solicitud.verificarAbierta();
        Responsable responsable = buscarResponsable(idResponsable);

        solicitud.asignarResponsable(responsable);
        repositorio.guardar(solicitud);
        return solicitud;
    }

    public Solicitud cambiarEstado(int idSolicitud, String nuevoEstado) {
        Solicitud solicitud = buscarSolicitud(idSolicitud);
        EstadoSolicitud estado = EstadoSolicitud.desdeTexto(nuevoEstado).orElseThrow(
                () -> new SolicitudException("no se puede pasar de " + solicitud.getEstado() + " a " + nuevoEstado));

        solicitud.cambiarEstado(estado);
        repositorio.guardar(solicitud);
        return solicitud;
    }

    // ---------- consultas ----------

    public List<Solicitud> listarTodas() {
        return repositorio.listarTodas();
    }

    public List<Responsable> listarResponsables() {
        return responsables.listarTodos();
    }

    public List<Solicitud> filtrarPorEstado(EstadoSolicitud estado) {
        return filtrar(solicitud -> solicitud.getEstado() == estado);
    }

    public List<Solicitud> filtrarPorTipo(TipoSolicitud tipo) {
        return filtrar(solicitud -> solicitud.getTipo() == tipo);
    }

    public List<Solicitud> filtrarPorEstudiante(String legajo) {
        return filtrar(solicitud -> solicitud.perteneceA(legajo));
    }

    // ---------- validaciones y busquedas ----------

    private void validarDatosPersonales(String legajo, String nombre, String email) {
        if (estaVacio(legajo)) {
            throw new SolicitudException("legajo vacio");
        }
        if (estaVacio(nombre)) {
            throw new SolicitudException("nombre vacio");
        }
        if (email == null || !email.contains("@")) {
            throw new SolicitudException("email invalido");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isEmpty();
    }

    private TipoSolicitud convertirTipo(String tipo) {
        return TipoSolicitud.desdeTexto(tipo)
                .orElseThrow(() -> new SolicitudException("tipo invalido"));
    }

    private Prioridad convertirPrioridad(int codigo) {
        return Prioridad.desdeCodigo(codigo)
                .orElseThrow(() -> new SolicitudException("prioridad invalida"));
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.length() < LARGO_MINIMO_DESCRIPCION) {
            throw new SolicitudException("la descripcion debe tener al menos " + LARGO_MINIMO_DESCRIPCION + " caracteres");
        }
    }

    private Estudiante buscarOCrearEstudiante(String legajo, String nombre, String email) {
        return repositorio.buscarEstudiante(legajo)
                .orElseGet(() -> new Estudiante(legajo, nombre, email));
    }

    private Solicitud buscarSolicitud(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new SolicitudException("no existe la solicitud " + id));
    }

    private Responsable buscarResponsable(int id) {
        return responsables.buscarPorId(id)
                .orElseThrow(() -> new SolicitudException("no existe el responsable " + id));
    }

    private List<Solicitud> filtrar(Predicate<Solicitud> condicion) {
        return repositorio.listarTodas().stream()
                .filter(condicion)
                .toList();
    }
}
