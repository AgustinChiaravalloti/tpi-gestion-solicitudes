package ar.edu.utn.solicitudes;

import java.time.LocalDate;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        SistemaSolicitudes sis = new SistemaSolicitudes();
        Scanner sc = new Scanner(System.in);
        int op = -1;

        while (op != 0) {
            System.out.println();
            System.out.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Asignar responsable");
            System.out.println("3. Cambiar estado");
            System.out.println("4. Listar todas");
            System.out.println("5. Filtrar por estado");
            System.out.println("6. Filtrar por tipo");
            System.out.println("7. Filtrar por estudiante");
            System.out.println("8. Reporte");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            try {
                op = Integer.parseInt(sc.nextLine());
            } catch (Exception ex) {
                op = -1;
            }

            switch (op) {
                case 1:
                    System.out.print("Legajo: ");
                    String leg = sc.nextLine();
                    System.out.print("Nombre: ");
                    String nom = sc.nextLine();
                    System.out.print("Email: ");
                    String mail = sc.nextLine();
                    System.out.print("Tipo (CERTIFICADO / EQUIVALENCIA / CAMBIO_COMISION / RECLAMO_NOTA): ");
                    String tipo = sc.nextLine().toUpperCase();
                    System.out.print("Prioridad (1 = alta, 2 = media, 3 = baja): ");
                    int prio = Integer.parseInt(sc.nextLine());
                    System.out.print("Descripcion: ");
                    String desc = sc.nextLine();
                    System.out.print("Fecha (AAAA-MM-DD, Enter = hoy): ");
                    String fec = sc.nextLine();
                    if (fec.equals("")) {
                        fec = LocalDate.now().toString();
                    }
                    sis.registrarSolicitud(leg, nom, mail, tipo, prio, desc, fec);
                    break;
                case 2:
                    System.out.println("Responsables disponibles:");
                    for (Responsable r : sis.listarResponsables()) {
                        System.out.println(r.getId() + ". " + r.getNombre() + " - " + r.getArea());
                    }
                    System.out.print("Id de la solicitud: ");
                    int idS = Integer.parseInt(sc.nextLine());
                    System.out.print("Id del responsable: ");
                    int idR = Integer.parseInt(sc.nextLine());
                    sis.asignarResponsable(idS, idR);
                    break;
                case 3:
                    System.out.print("Id de la solicitud: ");
                    int id = Integer.parseInt(sc.nextLine());
                    System.out.print("Nuevo estado (EN_PROCESO / RESUELTA / RECHAZADA): ");
                    String est = sc.nextLine().toUpperCase();
                    sis.cambiarEstado(id, est);
                    break;
                case 4:
                    sis.mostrar(sis.listarTodas());
                    break;
                case 5:
                    System.out.print("Estado (PENDIENTE / EN_PROCESO / RESUELTA / RECHAZADA): ");
                    sis.mostrar(sis.filtrarPorEstado(sc.nextLine().toUpperCase()));
                    break;
                case 6:
                    System.out.print("Tipo (CERTIFICADO / EQUIVALENCIA / CAMBIO_COMISION / RECLAMO_NOTA): ");
                    sis.mostrar(sis.filtrarPorTipo(sc.nextLine().toUpperCase()));
                    break;
                case 7:
                    System.out.print("Legajo: ");
                    sis.mostrar(sis.filtrarPorEstudiante(sc.nextLine()));
                    break;
                case 8:
                    sis.reporte();
                    break;
                case 0:
                    System.out.println("Hasta luego");
                    break;
                default:
                    System.out.println("Opcion invalida");
            }
        }
    }
}
