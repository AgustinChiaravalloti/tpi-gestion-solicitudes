package ar.edu.utn.solicitudes;

import java.time.LocalDate;

public class Solicitud {

    public int id;
    public String tipo;
    public int prioridad;
    public String estado;
    public String desc;
    public LocalDate fecha;
    public Estudiante est;
    public Responsable resp;

    public Solicitud(int id, String tipo, int prioridad, String desc, LocalDate fecha, Estudiante est) {
        this.id = id;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.desc = desc;
        this.fecha = fecha;
        this.est = est;
    }
}