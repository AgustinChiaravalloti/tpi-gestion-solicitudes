package ar.edu.utn.solicitudes;

import java.util.Arrays;
import java.util.Optional;

public enum TipoSolicitud {
    CERTIFICADO,
    EQUIVALENCIA,
    CAMBIO_COMISION,
    RECLAMO_NOTA;

    public static Optional<TipoSolicitud> desdeTexto(String texto) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.name().equals(texto))
                .findFirst();
    }
}
