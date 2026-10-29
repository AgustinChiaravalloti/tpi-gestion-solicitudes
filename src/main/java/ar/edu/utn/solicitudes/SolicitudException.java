package ar.edu.utn.solicitudes;

/**
 * Se lanza cuando una operacion viola una regla de negocio de las solicitudes.
 */
public class SolicitudException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SolicitudException(String mensaje) {
        super(mensaje);
    }
}
