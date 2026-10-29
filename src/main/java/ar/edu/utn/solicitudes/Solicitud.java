package ar.edu.utn.solicitudes;

import java.time.LocalDate;

public class Solicitud {

    private final int id;
    private final TipoSolicitud tipo;
    private Prioridad prioridad;
    private EstadoSolicitud estado;
    private final String descripcion;
    private final LocalDate fecha;
    private final Estudiante estudiante;
    private Responsable responsable;

    public Solicitud(int id, TipoSolicitud tipo, Prioridad prioridad, String descripcion,
                     LocalDate fecha, Estudiante estudiante) {
        this.id = id;
        this.tipo = tipo;
        this.prioridad = tipo.prioridadPara(prioridad);
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.estudiante = estudiante;
        this.estado = EstadoSolicitud.PENDIENTE;
    }

    /** Reconstruye una solicitud ya existente (por ejemplo, al leerla del archivo). */
    public static Solicitud reconstruir(int id, TipoSolicitud tipo, Prioridad prioridad, EstadoSolicitud estado,
                                       String descripcion, LocalDate fecha, Estudiante estudiante,
                                       Responsable responsable) {
        Solicitud solicitud = new Solicitud(id, tipo, prioridad, descripcion, fecha, estudiante);
        solicitud.estado = estado;
        solicitud.responsable = responsable;
        return solicitud;
    }

    // ---------- comportamiento (movido desde SistemaSolicitudes) ----------

    public int diasPlazo() {
        return prioridad.ajustarPlazo(tipo.getDiasPlazoBase());
    }

    public LocalDate fechaLimite() {
        return fecha.plusDays(diasPlazo());
    }

    public boolean estaCerrada() {
        return estado.esFinal();
    }

    public boolean estaVencida(LocalDate hoy) {
        return !estaCerrada() && hoy.isAfter(fechaLimite());
    }

    public boolean perteneceA(String legajo) {
        return estudiante.getLegajo().equals(legajo);
    }

    public void asignarResponsable(Responsable nuevoResponsable) {
        if (estaCerrada()) {
            throw new SolicitudException("la solicitud ya esta cerrada");
        }
        this.responsable = nuevoResponsable;
    }

    public void cambiarEstado(EstadoSolicitud nuevo) {
        if (nuevo.requiereResponsable() && responsable == null) {
            throw new SolicitudException("no se puede pasar a " + nuevo + " sin responsable asignado");
        }
        if (!estado.puedePasarA(nuevo)) {
            throw new SolicitudException("no se puede pasar de " + estado + " a " + nuevo);
        }
        estado = nuevo;
    }

    // ---------- consultas ----------

    public int getId() {
        return id;
    }

    public TipoSolicitud getTipo() {
        return tipo;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Responsable getResponsable() {
        return responsable;
    }
}
