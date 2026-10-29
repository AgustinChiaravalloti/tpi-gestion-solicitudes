package ar.edu.utn.solicitudes;

import java.time.LocalDate;

public class Solicitud {

    public int id;
    public TipoSolicitud tipo;
    public Prioridad prioridad;
    public EstadoSolicitud estado;
    public String desc;
    public LocalDate fecha;
    public Estudiante est;
    public Responsable resp;

    public Solicitud(int id, TipoSolicitud tipo, Prioridad prioridad, String desc, LocalDate fecha, Estudiante est) {
        this.id = id;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.desc = desc;
        this.fecha = fecha;
        this.est = est;
    }
}
