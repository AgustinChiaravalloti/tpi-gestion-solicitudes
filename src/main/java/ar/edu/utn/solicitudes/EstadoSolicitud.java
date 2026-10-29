package ar.edu.utn.solicitudes;

import java.util.Arrays;
import java.util.Optional;

public enum EstadoSolicitud {
    PENDIENTE,
    EN_PROCESO,
    RESUELTA,
    RECHAZADA;

    public static Optional<EstadoSolicitud> desdeTexto(String texto) {
        return Arrays.stream(values())
                .filter(estado -> estado.name().equals(texto))
                .findFirst();
    }
}
