package ar.edu.utn.solicitudes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SistemaSolicitudes {

    private static final int LARGO_MINIMO_DESCRIPCION = 10;

    private final SolicitudRepository repositorio;
    private final CatalogoResponsables responsables;

    public SistemaSolicitudes(SolicitudRepository repositorio, CatalogoResponsables responsables) {
        this.repositorio = repositorio;
        this.responsables = responsables;
    }

    public List<Solicitud> listarTodas() {
        return repositorio.listarTodas();
    }

    public List<Responsable> listarResponsables() {
        return responsables.listarTodos();
    }

    public Solicitud registrarSolicitud(DatosSolicitud datos) {
        try {
            validarDatosPersonales(datos.legajo(), datos.nombre(), datos.email());
            TipoSolicitud tipo = convertirTipo(datos.tipo());
            Prioridad prioridad = convertirPrioridad(datos.prioridad());
            validarDescripcion(datos.descripcion());

            Estudiante estudiante = buscarOCrearEstudiante(datos.legajo(), datos.nombre(), datos.email());
            Solicitud solicitud = crearSolicitud(tipo, prioridad, datos.descripcion(),
                    LocalDate.parse(datos.fecha()), estudiante);

            System.out.println("Solicitud #" + solicitud.getId() + " registrada. Plazo: " + solicitud.diasPlazo() + " dias");
            return solicitud;
        } catch (SolicitudException | PersistenciaException ex) {
            informarError(ex);
            return null;
        }
    }

    public boolean cambiarEstado(int id, String nuevo) {
        try {
            Solicitud solicitud = buscarSolicitud(id);
            EstadoSolicitud estadoNuevo = EstadoSolicitud.desdeTexto(nuevo).orElseThrow(
                    () -> new SolicitudException("no se puede pasar de " + solicitud.getEstado() + " a " + nuevo));

            solicitud.cambiarEstado(estadoNuevo);
            repositorio.guardar(solicitud);

            System.out.println("Solicitud #" + id + " ahora esta " + solicitud.getEstado());
            return true;
        } catch (SolicitudException | PersistenciaException ex) {
            informarError(ex);
            return false;
        }
    }

    public boolean asignarResponsable(int idSolicitud, int idResponsable) {
        try {
            Solicitud solicitud = buscarSolicitud(idSolicitud);
            solicitud.verificarAbierta();
            Responsable responsable = buscarResponsable(idResponsable);

            solicitud.asignarResponsable(responsable);
            repositorio.guardar(solicitud);

            System.out.println("Solicitud #" + idSolicitud + " asignada a " + responsable.getNombre()
                    + " (" + responsable.getArea() + ")");
            return true;
        } catch (SolicitudException | PersistenciaException ex) {
            informarError(ex);
            return false;
        }
    }

    public List<Solicitud> filtrarPorEstado(String estado) {
        return filtrar(solicitud -> solicitud.getEstado().name().equals(estado));
    }

    public List<Solicitud> filtrarPorTipo(String tipo) {
        return filtrar(solicitud -> solicitud.getTipo().name().equals(tipo));
    }

    public List<Solicitud> filtrarPorEstudiante(String legajo) {
        return filtrar(solicitud -> solicitud.perteneceA(legajo));
    }

    // ---------- metodos extraidos ----------

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

    private Solicitud crearSolicitud(TipoSolicitud tipo, Prioridad prioridad, String descripcion,
                                     LocalDate fecha, Estudiante estudiante) {
        Solicitud solicitud = new Solicitud(repositorio.siguienteId(), tipo, prioridad, descripcion, fecha, estudiante);
        repositorio.guardar(solicitud);
        return solicitud;
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
        List<Solicitud> resultado = new ArrayList<>();
        for (Solicitud solicitud : repositorio.listarTodas()) {
            if (condicion.test(solicitud)) {
                resultado.add(solicitud);
            }
        }
        return resultado;
    }

    private void informarError(RuntimeException ex) {
        System.out.println("Error: " + ex.getMessage());
    }

    public void mostrar(List<Solicitud> l) {
        if (l.isEmpty()) {
            System.out.println("No hay solicitudes para mostrar");
            return;
        }
        for (Solicitud s : l) {
            String r = "sin asignar";
            if (s.getResponsable() != null) {
                r = s.getResponsable().getNombre() + " (" + s.getResponsable().getArea() + ")";
            }
            System.out.println("#" + s.getId() + " | " + s.getTipo() + " | " + s.getPrioridad() + " | " + s.getEstado()
                    + " | " + s.getEstudiante().getNombre() + " | " + r + " | " + s.getDescripcion());
        }
    }

    public void reporte() {
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        List<Solicitud> todas = repositorio.listarTodas();
        System.out.println("Total: " + todas.size());

        Map<EstadoSolicitud, Integer> porEstado = new EnumMap<>(EstadoSolicitud.class);
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            porEstado.put(estado, 0);
        }
        Map<TipoSolicitud, Integer> porTipo = new EnumMap<>(TipoSolicitud.class);
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            porTipo.put(tipo, 0);
        }
        for (Solicitud s : todas) {
            porEstado.merge(s.getEstado(), 1, Integer::sum);
            porTipo.merge(s.getTipo(), 1, Integer::sum);
        }

        System.out.println("--- Por estado ---");
        for (Map.Entry<EstadoSolicitud, Integer> fila : porEstado.entrySet()) {
            System.out.println(fila.getKey() + ": " + fila.getValue());
        }
        System.out.println("--- Por tipo ---");
        for (Map.Entry<TipoSolicitud, Integer> fila : porTipo.entrySet()) {
            System.out.println(fila.getKey() + ": " + fila.getValue());
        }

        System.out.println("--- Vencidas ---");
        int venc = 0;
        LocalDate hoy = LocalDate.now();
        for (Solicitud s : todas) {
            if (s.estaVencida(hoy)) {
                System.out.println("#" + s.getId() + " " + s.getTipo() + " vencio el " + s.fechaLimite());
                venc++;
            }
        }
        if (venc == 0) {
            System.out.println("No hay solicitudes vencidas");
        }
    }
}
