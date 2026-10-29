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
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.estudiante = estudiante;
        this.estado = EstadoSolicitud.PENDIENTE;

        // los reclamos de nota siempre son prioridad alta
        if (tipo == TipoSolicitud.RECLAMO_NOTA) {
            this.prioridad = Prioridad.ALTA;
        }
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
        int dias = 0;
        switch (tipo) {
            case CERTIFICADO:
                dias = 5;
                break;
            case EQUIVALENCIA:
                dias = 30;
                break;
            case CAMBIO_COMISION:
                dias = 10;
                break;
            case RECLAMO_NOTA:
                dias = 7;
                break;
        }
        if (prioridad == Prioridad.ALTA) {
            dias = dias / 2;
        }
        return dias;
    }

    public LocalDate fechaLimite() {
        return fecha.plusDays(diasPlazo());
    }

    public boolean estaCerrada() {
        return estado == EstadoSolicitud.RESUELTA || estado == EstadoSolicitud.RECHAZADA;
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
        if (nuevo == EstadoSolicitud.EN_PROCESO && responsable == null) {
            throw new SolicitudException("no se puede pasar a EN_PROCESO sin responsable asignado");
        }
        if (estado == EstadoSolicitud.PENDIENTE && nuevo == EstadoSolicitud.EN_PROCESO) {
            estado = nuevo;
        } else if (estado == EstadoSolicitud.PENDIENTE && nuevo == EstadoSolicitud.RECHAZADA) {
            estado = nuevo;
        } else if (estado == EstadoSolicitud.EN_PROCESO && nuevo == EstadoSolicitud.RESUELTA) {
            estado = nuevo;
        } else if (estado == EstadoSolicitud.EN_PROCESO && nuevo == EstadoSolicitud.RECHAZADA) {
            estado = nuevo;
        } else {
            throw new SolicitudException("no se puede pasar de " + estado + " a " + nuevo);
        }
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
