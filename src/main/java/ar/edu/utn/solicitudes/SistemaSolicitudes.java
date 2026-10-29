package ar.edu.utn.solicitudes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SistemaSolicitudes {

    private final List<Solicitud> lst = new ArrayList<>();
    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final List<Responsable> responsables = new ArrayList<>();
    private int ultimoId = 0;

    private static final int LARGO_MINIMO_DESCRIPCION = 10;

    public SistemaSolicitudes() {
        responsables.add(new Responsable(1, "Laura Gomez", "Bedelia"));
        responsables.add(new Responsable(2, "Martin Ruiz", "Secretaria Academica"));
        responsables.add(new Responsable(3, "Sofia Diaz", "Departamento Docente"));
        cargar();
    }

    public List<Solicitud> listarTodas() {
        return Collections.unmodifiableList(lst);
    }

    public List<Responsable> listarResponsables() {
        return Collections.unmodifiableList(responsables);
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
        } catch (SolicitudException ex) {
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
            guardar();

            System.out.println("Solicitud #" + id + " ahora esta " + solicitud.getEstado());
            return true;
        } catch (SolicitudException ex) {
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
            guardar();

            System.out.println("Solicitud #" + idSolicitud + " asignada a " + responsable.getNombre()
                    + " (" + responsable.getArea() + ")");
            return true;
        } catch (SolicitudException ex) {
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
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getLegajo().equals(legajo)) {
                return estudiante;
            }
        }
        Estudiante nuevo = new Estudiante(legajo, nombre, email);
        estudiantes.add(nuevo);
        return nuevo;
    }

    private Solicitud crearSolicitud(TipoSolicitud tipo, Prioridad prioridad, String descripcion,
                                     LocalDate fecha, Estudiante estudiante) {
        ultimoId++;
        Solicitud solicitud = new Solicitud(ultimoId, tipo, prioridad, descripcion, fecha, estudiante);
        lst.add(solicitud);
        guardar();
        return solicitud;
    }

    private Solicitud buscarSolicitud(int id) {
        for (Solicitud solicitud : lst) {
            if (solicitud.getId() == id) {
                return solicitud;
            }
        }
        throw new SolicitudException("no existe la solicitud " + id);
    }

    private Responsable buscarResponsable(int id) {
        for (Responsable responsable : responsables) {
            if (responsable.getId() == id) {
                return responsable;
            }
        }
        throw new SolicitudException("no existe el responsable " + id);
    }

    private List<Solicitud> filtrar(Predicate<Solicitud> condicion) {
        List<Solicitud> resultado = new ArrayList<>();
        for (Solicitud solicitud : lst) {
            if (condicion.test(solicitud)) {
                resultado.add(solicitud);
            }
        }
        return resultado;
    }

    private void informarError(SolicitudException ex) {
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
        System.out.println("Total: " + lst.size());

        Map<EstadoSolicitud, Integer> porEstado = new EnumMap<>(EstadoSolicitud.class);
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            porEstado.put(estado, 0);
        }
        Map<TipoSolicitud, Integer> porTipo = new EnumMap<>(TipoSolicitud.class);
        for (TipoSolicitud tipo : TipoSolicitud.values()) {
            porTipo.put(tipo, 0);
        }
        for (Solicitud s : lst) {
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
        for (Solicitud s : lst) {
            if (s.estaVencida(hoy)) {
                System.out.println("#" + s.getId() + " " + s.getTipo() + " vencio el " + s.fechaLimite());
                venc++;
            }
        }
        if (venc == 0) {
            System.out.println("No hay solicitudes vencidas");
        }
    }

    private void guardar() {
        try {
            FileWriter fw = new FileWriter("solicitudes.csv");
            for (Solicitud s : lst) {
                int idR = 0;
                if (s.getResponsable() != null) {
                    idR = s.getResponsable().getId();
                }
                fw.write(s.getId() + ";" + s.getTipo() + ";" + s.getPrioridad().getCodigo() + ";" + s.getEstado() + ";"
                        + s.getDescripcion() + ";" + s.getFecha() + ";" + s.getEstudiante().getLegajo() + ";"
                        + s.getEstudiante().getNombre() + ";" + s.getEstudiante().getEmail() + ";" + idR + "\n");
            }
            fw.close();
        } catch (Exception ex) {
        }
    }

    private void cargar() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("solicitudes.csv"));
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] x = linea.split(";");

                Estudiante e = buscarOCrearEstudiante(x[6], x[7], x[8]);

                Responsable resp = null;
                int idR = Integer.parseInt(x[9]);
                for (Responsable r : responsables) {
                    if (r.getId() == idR) {
                        resp = r;
                    }
                }

                Solicitud s = Solicitud.reconstruir(Integer.parseInt(x[0]), TipoSolicitud.valueOf(x[1]),
                        Prioridad.desdeCodigo(Integer.parseInt(x[2])).get(), EstadoSolicitud.valueOf(x[3]),
                        x[4], LocalDate.parse(x[5]), e, resp);

                lst.add(s);
                if (s.getId() > ultimoId) {
                    ultimoId = s.getId();
                }
            }
            br.close();
        } catch (Exception ex) {
        }
    }
}
