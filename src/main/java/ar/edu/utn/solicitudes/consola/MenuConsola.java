package ar.edu.utn.solicitudes.consola;

import ar.edu.utn.solicitudes.dominio.*;
import ar.edu.utn.solicitudes.servicio.*;

import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Interfaz de consola: lee lo que escribe el usuario, llama al servicio e imprime
 * los resultados. Es la unica clase que conoce la consola.
 */
public class MenuConsola {

    private static final int SALIR = 0;
    private static final int OPCION_INVALIDA = -1;

    private final SolicitudService servicio;
    private final GeneradorReporte generadorReporte;
    private final Scanner entrada;
    private final PrintStream salida;
    private final Clock reloj;

    public MenuConsola(SolicitudService servicio, GeneradorReporte generadorReporte,
                       Scanner entrada, PrintStream salida, Clock reloj) {
        this.servicio = servicio;
        this.generadorReporte = generadorReporte;
        this.entrada = entrada;
        this.salida = salida;
        this.reloj = reloj;
    }

    public void iniciar() {
        int opcion = OPCION_INVALIDA;
        while (opcion != SALIR) {
            mostrarOpciones();
            opcion = leerOpcion();
            ejecutar(opcion);
        }
    }

    // ---------- menu ----------

    private void mostrarOpciones() {
        salida.println();
        salida.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
        salida.println("1. Registrar solicitud");
        salida.println("2. Asignar responsable");
        salida.println("3. Cambiar estado");
        salida.println("4. Listar todas");
        salida.println("5. Filtrar por estado");
        salida.println("6. Filtrar por tipo");
        salida.println("7. Filtrar por estudiante");
        salida.println("8. Reporte");
        salida.println("0. Salir");
        salida.print("Opcion: ");
    }

    private int leerOpcion() {
        try {
            return Integer.parseInt(entrada.nextLine().trim());
        } catch (NumberFormatException ex) {
            return OPCION_INVALIDA;
        }
    }

    private void ejecutar(int opcion) {
        try {
            switch (opcion) {
                case 1 -> registrarSolicitud();
                case 2 -> asignarResponsable();
                case 3 -> cambiarEstado();
                case 4 -> mostrar(servicio.listarTodas());
                case 5 -> filtrarPorEstado();
                case 6 -> filtrarPorTipo();
                case 7 -> filtrarPorEstudiante();
                case 8 -> mostrarReporte();
                case SALIR -> salida.println("Hasta luego");
                default -> salida.println("Opcion invalida");
            }
        } catch (SolicitudException | PersistenciaException ex) {
            salida.println("Error: " + ex.getMessage());
        }
    }

    // ---------- opciones ----------

    private void registrarSolicitud() {
        String legajo = leerTexto("Legajo: ");
        String nombre = leerTexto("Nombre: ");
        String email = leerTexto("Email: ");
        String tipo = leerTexto("Tipo (" + opciones(TipoSolicitud.values()) + "): ").toUpperCase();
        int prioridad = leerEntero("Prioridad (1 = alta, 2 = media, 3 = baja): ");
        String descripcion = leerTexto("Descripcion: ");
        LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD, Enter = hoy): ");

        Solicitud solicitud = servicio.registrar(
                new DatosSolicitud(legajo, nombre, email, tipo, prioridad, descripcion, fecha));
        salida.println("Solicitud #" + solicitud.getId() + " registrada. Plazo: " + solicitud.diasPlazo() + " dias");
    }

    private void asignarResponsable() {
        salida.println("Responsables disponibles:");
        for (Responsable responsable : servicio.listarResponsables()) {
            salida.println(responsable.getId() + ". " + responsable.getNombre() + " - " + responsable.getArea());
        }
        int idSolicitud = leerEntero("Id de la solicitud: ");
        int idResponsable = leerEntero("Id del responsable: ");

        Solicitud solicitud = servicio.asignarResponsable(idSolicitud, idResponsable);
        salida.println("Solicitud #" + solicitud.getId() + " asignada a " + describir(solicitud.getResponsable()));
    }

    private void cambiarEstado() {
        int id = leerEntero("Id de la solicitud: ");
        EstadoSolicitud[] destinos = Arrays.stream(EstadoSolicitud.values())
                .filter(EstadoSolicitud::esDestinoPosible)
                .toArray(EstadoSolicitud[]::new);
        String estado = leerTexto("Nuevo estado (" + opciones(destinos) + "): ").toUpperCase();

        Solicitud solicitud = servicio.cambiarEstado(id, estado);
        salida.println("Solicitud #" + solicitud.getId() + " ahora esta " + solicitud.getEstado());
    }

    private void filtrarPorEstado() {
        String texto = leerTexto("Estado (" + opciones(EstadoSolicitud.values()) + "): ").toUpperCase();
        mostrar(EstadoSolicitud.desdeTexto(texto)
                .map(servicio::filtrarPorEstado)
                .orElse(List.of()));
    }

    private void filtrarPorTipo() {
        String texto = leerTexto("Tipo (" + opciones(TipoSolicitud.values()) + "): ").toUpperCase();
        mostrar(TipoSolicitud.desdeTexto(texto)
                .map(servicio::filtrarPorTipo)
                .orElse(List.of()));
    }

    private void filtrarPorEstudiante() {
        mostrar(servicio.filtrarPorEstudiante(leerTexto("Legajo: ")));
    }

    private void mostrarReporte() {
        Reporte reporte = generadorReporte.generar();
        salida.println("===== REPORTE DE SOLICITUDES =====");
        salida.println("Total: " + reporte.total());
        salida.println("--- Por estado ---");
        imprimirCantidades(reporte.cantidadPorEstado());
        salida.println("--- Por tipo ---");
        imprimirCantidades(reporte.cantidadPorTipo());
        salida.println("--- Vencidas ---");
        if (reporte.vencidas().isEmpty()) {
            salida.println("No hay solicitudes vencidas");
        }
        for (Solicitud solicitud : reporte.vencidas()) {
            salida.println("#" + solicitud.getId() + " " + solicitud.getTipo() + " vencio el " + solicitud.fechaLimite());
        }
    }

    // ---------- formato ----------

    private void mostrar(List<Solicitud> solicitudes) {
        if (solicitudes.isEmpty()) {
            salida.println("No hay solicitudes para mostrar");
            return;
        }
        for (Solicitud solicitud : solicitudes) {
            salida.println(formatear(solicitud));
        }
    }

    private String formatear(Solicitud solicitud) {
        return "#" + solicitud.getId()
                + " | " + solicitud.getTipo()
                + " | " + solicitud.getPrioridad()
                + " | " + solicitud.getEstado()
                + " | " + solicitud.getEstudiante().getNombre()
                + " | " + describir(solicitud.getResponsable())
                + " | " + solicitud.getDescripcion();
    }

    private String describir(Responsable responsable) {
        if (responsable == null) {
            return "sin asignar";
        }
        return responsable.getNombre() + " (" + responsable.getArea() + ")";
    }

    private void imprimirCantidades(Map<? extends Enum<?>, Integer> cantidades) {
        cantidades.forEach((clave, cantidad) -> salida.println(clave + ": " + cantidad));
    }

    private String opciones(Enum<?>[] valores) {
        return Arrays.stream(valores).map(Enum::name).collect(Collectors.joining(" / "));
    }

    // ---------- lectura con validacion (corrige D-01 y D-02) ----------

    private String leerTexto(String mensaje) {
        salida.print(mensaje);
        return entrada.nextLine();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return Integer.parseInt(texto.trim());
            } catch (NumberFormatException ex) {
                salida.println("Debe ingresar un numero entero. Intente de nuevo.");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.isBlank()) {
                return LocalDate.now(reloj);
            }
            try {
                return LocalDate.parse(texto.trim());
            } catch (DateTimeParseException ex) {
                salida.println("Fecha invalida, use el formato AAAA-MM-DD. Intente de nuevo.");
            }
        }
    }
}
