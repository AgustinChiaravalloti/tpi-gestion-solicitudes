package ar.edu.utn.solicitudes.servicio;

import ar.edu.utn.solicitudes.dominio.Estudiante;
import ar.edu.utn.solicitudes.dominio.Solicitud;

import java.util.Optional;

/**
 * Abstraccion del almacenamiento de solicitudes.
 * La logica de negocio depende de esta interfaz y no de un archivo concreto (DIP).
 */
public interface SolicitudRepository extends ConsultaSolicitudes {

    /** Agrega la solicitud o actualiza la existente con el mismo id. */
    void guardar(Solicitud solicitud);

    int siguienteId();

    Optional<Estudiante> buscarEstudiante(String legajo);
}
