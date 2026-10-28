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

}


