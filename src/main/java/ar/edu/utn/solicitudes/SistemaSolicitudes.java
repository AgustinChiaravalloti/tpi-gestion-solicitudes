package ar.edu.utn.solicitudes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SistemaSolicitudes {

    private final List<Solicitud> lst = new ArrayList<>();
    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final List<Responsable> responsables = new ArrayList<>();
    private int ultimoId = 0;

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
        Optional<TipoSolicitud> tipoSolicitud = TipoSolicitud.desdeTexto(tipo);
        if (tipoSolicitud.isEmpty()) {
            System.out.println("Error: tipo invalido");
            return null;
        }
        Optional<Prioridad> prioridadSolicitud = Prioridad.desdeCodigo(prioridad);
        if (prioridadSolicitud.isEmpty()) {
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
            if (estudiantes.get(i).getLegajo().equals(legajo)) {
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
        Solicitud s = new Solicitud(ultimoId, tipoSolicitud.get(), prioridadSolicitud.get(), desc, f, e);

        lst.add(s);
        guardar();

        System.out.println("Solicitud #" + s.getId() + " registrada. Plazo: " + s.diasPlazo() + " dias");
        return s;
    }

    public boolean cambiarEstado(int id, String nuevo) {
        Solicitud s = null;
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).getId() == id) {
                s = lst.get(i);
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return false;
        }

        Optional<EstadoSolicitud> estadoNuevo = EstadoSolicitud.desdeTexto(nuevo);
        if (estadoNuevo.isEmpty()) {
            System.out.println("Error: no se puede pasar de " + s.getEstado() + " a " + nuevo);
            return false;
        }

        try {
            s.cambiarEstado(estadoNuevo.get());
        } catch (SolicitudException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }

        guardar();
        System.out.println("Solicitud #" + id + " ahora esta " + s.getEstado());
        return true;
    }

    public boolean asignarResponsable(int idSolicitud, int idResp) {
        Solicitud s = null;
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).getId() == idSolicitud) {
                s = lst.get(i);
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + idSolicitud);
            return false;
        }

        if (s.estaCerrada()) {
            System.out.println("Error: la solicitud ya esta cerrada");
            return false;
        }

        Responsable r = null;
        for (int i = 0; i < responsables.size(); i++) {
            if (responsables.get(i).getId() == idResp) {
                r = responsables.get(i);
            }
        }
        if (r == null) {
            System.out.println("Error: no existe el responsable " + idResp);
            return false;
        }

        s.asignarResponsable(r);
        guardar();
        System.out.println("Solicitud #" + idSolicitud + " asignada a " + r.getNombre() + " (" + r.getArea() + ")");
        return true;
    }

    public List<Solicitud> filtrarPorEstado(String estado) {
        List<Solicitud> res = new ArrayList<>();
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).getEstado().name().equals(estado)) {
                res.add(lst.get(i));
            }
        }
        return res;
    }

    public List<Solicitud> filtrarPorTipo(String tipo) {
        List<Solicitud> res = new ArrayList<>();
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).getTipo().name().equals(tipo)) {
                res.add(lst.get(i));
            }
        }
        return res;
    }

    public List<Solicitud> filtrarPorEstudiante(String legajo) {
        List<Solicitud> res = new ArrayList<>();
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).perteneceA(legajo)) {
                res.add(lst.get(i));
            }
        }
        return res;
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

        int pend = 0, proc = 0, res = 0, rech = 0;
        for (Solicitud s : lst) {
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                pend++;
            } else if (s.getEstado() == EstadoSolicitud.EN_PROCESO) {
                proc++;
            } else if (s.getEstado() == EstadoSolicitud.RESUELTA) {
                res++;
            } else if (s.getEstado() == EstadoSolicitud.RECHAZADA) {
                rech++;
            }
        }
        System.out.println("--- Por estado ---");
        System.out.println("PENDIENTE: " + pend);
        System.out.println("EN_PROCESO: " + proc);
        System.out.println("RESUELTA: " + res);
        System.out.println("RECHAZADA: " + rech);

        int cert = 0, eq = 0, cc = 0, rn = 0;
        for (Solicitud s : lst) {
            if (s.getTipo() == TipoSolicitud.CERTIFICADO) {
                cert++;
            } else if (s.getTipo() == TipoSolicitud.EQUIVALENCIA) {
                eq++;
            } else if (s.getTipo() == TipoSolicitud.CAMBIO_COMISION) {
                cc++;
            } else if (s.getTipo() == TipoSolicitud.RECLAMO_NOTA) {
                rn++;
            }
        }
        System.out.println("--- Por tipo ---");
        System.out.println("CERTIFICADO: " + cert);
        System.out.println("EQUIVALENCIA: " + eq);
        System.out.println("CAMBIO_COMISION: " + cc);
        System.out.println("RECLAMO_NOTA: " + rn);

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

                Estudiante e = null;
                for (Estudiante aux : estudiantes) {
                    if (aux.getLegajo().equals(x[6])) {
                        e = aux;
                    }
                }
                if (e == null) {
                    e = new Estudiante(x[6], x[7], x[8]);
                    estudiantes.add(e);
                }

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
