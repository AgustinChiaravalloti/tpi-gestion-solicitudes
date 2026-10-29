package ar.edu.utn.solicitudes;

public class Responsable {

    private final int id;
    private final String nombre;
    private final String area;

    public Responsable(int id, String nombre, String area) {
        this.id = id;
        this.nombre = nombre;
        this.area = area;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getArea() {
        return area;
    }
}
