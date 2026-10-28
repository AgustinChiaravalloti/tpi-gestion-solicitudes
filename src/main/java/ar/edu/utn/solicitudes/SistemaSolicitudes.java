package ar.edu.utn.solicitudes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SistemaSolicitudes {

    public List<Solicitud> lst = new ArrayList<>();
    public List<Estudiante> estudiantes = new ArrayList<>();
    public List<Responsable> responsables = new ArrayList<>();
    int ultimoId = 0;

    public SistemaSolicitudes() {
        responsables.add(new Responsable(1, "Laura Gómez", "Bedelía"));
        responsables.add(new Responsable(2, "Martín Ruiz", "Secretaría Académica"));
        responsables.add(new Responsable(3, "Sofía Díaz", "Departamento Docente"));
    }
    public Solicitud registrarSolicitud(String legajo, String nombre, String email, String tipo, int prioridad, String desc, String fecha) {
        // validaciones
        if (legajo == null || legajo.equals("")) {
            System.out.println("Error: legajo vacio");
            return null;
        }
        if (nombre == null || nombre.equals("")) {
            System.out.println("Error: nombre vacio");
            return null;
        }
        if (email == null || !email.contains("@")) {
            System.out.println("Error: email invalido");
            return null;
        }
        if (!tipo.equals("CERTIFICADO") && !tipo.equals("EQUIVALENCIA") && !tipo.equals("CAMBIO_COMISION") && !tipo.equals("RECLAMO_NOTA")) {
            System.out.println("Error: tipo invalido");
            return null;
        }
        if (prioridad < 1 || prioridad > 3) {
            System.out.println("Error: prioridad invalida");
            return null;
        }
        if (desc == null || desc.length() < 10) {
            System.out.println("Error: la descripcion debe tener al menos 10 caracteres");
            return null;
        }

        // buscar o crear estudiante
        Estudiante e = null;
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).legajo.equals(legajo)) {
                e = estudiantes.get(i);
            }
        }
        if (e == null) {
            e = new Estudiante(legajo, nombre, email);
            estudiantes.add(e);
        }

        // crear solicitud
        ultimoId++;
        LocalDate f = LocalDate.parse(fecha);
        Solicitud s = new Solicitud(ultimoId, tipo, prioridad, desc, f, e);
        s.estado = "PENDIENTE";

        // los reclamos de nota siempre son prioridad alta
        if (tipo.equals("RECLAMO_NOTA")) {
            s.prioridad = 1;
        }

        lst.add(s);

        System.out.println("Solicitud #" + s.id + " registrada. Plazo: " + calcularDiasLimite(s) + " dias");
        return s;
    }

    public int calcularDiasLimite(Solicitud s) {
        int dias = 0;
        switch (s.tipo) {
            case "CERTIFICADO":
                dias = 5;
                break;
            case "EQUIVALENCIA":
                dias = 30;
                break;
            case "CAMBIO_COMISION":
                dias = 10;
                break;
            case "RECLAMO_NOTA":
                dias = 7;
                break;
        }
        if (s.prioridad == 1) {
            dias = dias / 2;
        }
        return dias;
    }
    public boolean cambiarEstado(int id, String nuevo) {
        Solicitud s = null;
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).id == id) {
                s = lst.get(i);
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return false;
        }

        if (nuevo.equals("EN_PROCESO") && s.resp == null) {
            System.out.println("Error: no se puede pasar a EN_PROCESO sin responsable asignado");
            return false;
        }

        if (s.estado.equals("PENDIENTE") && nuevo.equals("EN_PROCESO")) {
            s.estado = nuevo;
        } else if (s.estado.equals("PENDIENTE") && nuevo.equals("RECHAZADA")) {
            s.estado = nuevo;
        } else if (s.estado.equals("EN_PROCESO") && nuevo.equals("RESUELTA")) {
            s.estado = nuevo;
        } else if (s.estado.equals("EN_PROCESO") && nuevo.equals("RECHAZADA")) {
            s.estado = nuevo;
        } else {
            System.out.println("Error: no se puede pasar de " + s.estado + " a " + nuevo);
            return false;
        }

        System.out.println("Solicitud #" + id + " ahora esta " + s.estado);
        return true;
    }

    public boolean asignarResponsable(int idSolicitud, int idResp) {
        Solicitud s = null;
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).id == idSolicitud) {
                s = lst.get(i);
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + idSolicitud);
            return false;
        }

        if (s.estado.equals("RESUELTA") || s.estado.equals("RECHAZADA")) {
            System.out.println("Error: la solicitud ya esta cerrada");
            return false;
        }

        Responsable r = null;
        for (int i = 0; i < responsables.size(); i++) {
            if (responsables.get(i).id == idResp) {
                r = responsables.get(i);
            }
        }
        if (r == null) {
            System.out.println("Error: no existe el responsable " + idResp);
            return false;
        }

        s.resp = r;
        System.out.println("Solicitud #" + idSolicitud + " asignada a " + r.nombre + " (" + r.area + ")");
        return true;
    }
}


