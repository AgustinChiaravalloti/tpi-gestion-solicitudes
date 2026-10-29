package ar.edu.utn.solicitudes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SistemaSolicitudes {

    public List<Solicitud> lst = new ArrayList<>();
    public List<Estudiante> estudiantes = new ArrayList<>();
    public List<Responsable> responsables = new ArrayList<>();
    int ultimoId = 0;

    public SistemaSolicitudes() {
        responsables.add(new Responsable(1, "Laura Gomez", "Bedelia"));
        responsables.add(new Responsable(2, "Martin Ruiz", "Secretaria Academica"));
        responsables.add(new Responsable(3, "Sofia Diaz", "Departamento Docente"));
        cargar();
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
        Solicitud s = new Solicitud(ultimoId, tipoSolicitud.get(), prioridadSolicitud.get(), desc, f, e);
        s.estado = EstadoSolicitud.PENDIENTE;

        // los reclamos de nota siempre son prioridad alta
        if (s.tipo == TipoSolicitud.RECLAMO_NOTA) {
            s.prioridad = Prioridad.ALTA;
        }

        lst.add(s);
        guardar();

        System.out.println("Solicitud #" + s.id + " registrada. Plazo: " + calcularDiasLimite(s) + " dias");
        return s;
    }

    public int calcularDiasLimite(Solicitud s) {
        int dias = 0;
        switch (s.tipo) {
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
        if (s.prioridad == Prioridad.ALTA) {
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

        Optional<EstadoSolicitud> estadoNuevo = EstadoSolicitud.desdeTexto(nuevo);
        if (estadoNuevo.isEmpty()) {
            System.out.println("Error: no se puede pasar de " + s.estado + " a " + nuevo);
            return false;
        }
        EstadoSolicitud n = estadoNuevo.get();

        if (n == EstadoSolicitud.EN_PROCESO && s.resp == null) {
            System.out.println("Error: no se puede pasar a EN_PROCESO sin responsable asignado");
            return false;
        }

        if (s.estado == EstadoSolicitud.PENDIENTE && n == EstadoSolicitud.EN_PROCESO) {
            s.estado = n;
        } else if (s.estado == EstadoSolicitud.PENDIENTE && n == EstadoSolicitud.RECHAZADA) {
            s.estado = n;
        } else if (s.estado == EstadoSolicitud.EN_PROCESO && n == EstadoSolicitud.RESUELTA) {
            s.estado = n;
        } else if (s.estado == EstadoSolicitud.EN_PROCESO && n == EstadoSolicitud.RECHAZADA) {
            s.estado = n;
        } else {
            System.out.println("Error: no se puede pasar de " + s.estado + " a " + nuevo);
            return false;
        }

        guardar();
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

        if (s.estado == EstadoSolicitud.RESUELTA || s.estado == EstadoSolicitud.RECHAZADA) {
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
        guardar();
        System.out.println("Solicitud #" + idSolicitud + " asignada a " + r.nombre + " (" + r.area + ")");
        return true;
    }

    public List<Solicitud> filtrarPorEstado(String estado) {
        List<Solicitud> res = new ArrayList<>();
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).estado.name().equals(estado)) {
                res.add(lst.get(i));
            }
        }
        return res;
    }

    public List<Solicitud> filtrarPorTipo(String tipo) {
        List<Solicitud> res = new ArrayList<>();
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).tipo.name().equals(tipo)) {
                res.add(lst.get(i));
            }
        }
        return res;
    }

    public List<Solicitud> filtrarPorEstudiante(String legajo) {
        List<Solicitud> res = new ArrayList<>();
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).est.legajo.equals(legajo)) {
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
            if (s.resp != null) {
                r = s.resp.nombre + " (" + s.resp.area + ")";
            }
            System.out.println("#" + s.id + " | " + s.tipo + " | " + s.prioridad + " | " + s.estado
                    + " | " + s.est.nombre + " | " + r + " | " + s.desc);
        }
    }

    public void reporte() {
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        System.out.println("Total: " + lst.size());

        int pend = 0, proc = 0, res = 0, rech = 0;
        for (Solicitud s : lst) {
            if (s.estado == EstadoSolicitud.PENDIENTE) {
                pend++;
            } else if (s.estado == EstadoSolicitud.EN_PROCESO) {
                proc++;
            } else if (s.estado == EstadoSolicitud.RESUELTA) {
                res++;
            } else if (s.estado == EstadoSolicitud.RECHAZADA) {
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
            if (s.tipo == TipoSolicitud.CERTIFICADO) {
                cert++;
            } else if (s.tipo == TipoSolicitud.EQUIVALENCIA) {
                eq++;
            } else if (s.tipo == TipoSolicitud.CAMBIO_COMISION) {
                cc++;
            } else if (s.tipo == TipoSolicitud.RECLAMO_NOTA) {
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
        for (Solicitud s : lst) {
            if (s.estado != EstadoSolicitud.RESUELTA && s.estado != EstadoSolicitud.RECHAZADA) {
                LocalDate limite = s.fecha.plusDays(calcularDiasLimite(s));
                if (LocalDate.now().isAfter(limite)) {
                    System.out.println("#" + s.id + " " + s.tipo + " vencio el " + limite);
                    venc++;
                }
            }
        }
        if (venc == 0) {
            System.out.println("No hay solicitudes vencidas");
        }
    }

    public void guardar() {
        try {
            FileWriter fw = new FileWriter("solicitudes.csv");
            for (Solicitud s : lst) {
                int idR = 0;
                if (s.resp != null) {
                    idR = s.resp.id;
                }
                fw.write(s.id + ";" + s.tipo + ";" + s.prioridad.getCodigo() + ";" + s.estado + ";" + s.desc + ";"
                        + s.fecha + ";" + s.est.legajo + ";" + s.est.nombre + ";" + s.est.email + ";" + idR + "\n");
            }
            fw.close();
        } catch (Exception ex) {
        }
    }

    public void cargar() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("solicitudes.csv"));
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] x = linea.split(";");

                Estudiante e = null;
                for (Estudiante aux : estudiantes) {
                    if (aux.legajo.equals(x[6])) {
                        e = aux;
                    }
                }
                if (e == null) {
                    e = new Estudiante(x[6], x[7], x[8]);
                    estudiantes.add(e);
                }

                Solicitud s = new Solicitud(Integer.parseInt(x[0]), TipoSolicitud.valueOf(x[1]),
                        Prioridad.desdeCodigo(Integer.parseInt(x[2])).get(), x[4], LocalDate.parse(x[5]), e);
                s.estado = EstadoSolicitud.valueOf(x[3]);

                int idR = Integer.parseInt(x[9]);
                for (Responsable r : responsables) {
                    if (r.id == idR) {
                        s.resp = r;
                    }
                }

                lst.add(s);
                if (s.id > ultimoId) {
                    ultimoId = s.id;
                }
            }
            br.close();
        } catch (Exception ex) {
        }
    }
}
