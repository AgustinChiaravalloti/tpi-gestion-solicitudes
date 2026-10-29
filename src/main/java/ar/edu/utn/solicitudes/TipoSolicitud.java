package ar.edu.utn.solicitudes;

import java.util.Arrays;
import java.util.Optional;

public enum TipoSolicitud {
    CERTIFICADO(5),
    EQUIVALENCIA(30),
    CAMBIO_COMISION(10),
    RECLAMO_NOTA(7) {
        @Override
        public Prioridad prioridadPara(Prioridad solicitada) {
            // los reclamos de nota siempre son prioridad alta
            return Prioridad.ALTA;
        }
    };

    private final int diasPlazoBase;

    TipoSolicitud(int diasPlazoBase) {
        this.diasPlazoBase = diasPlazoBase;
    }

    public int getDiasPlazoBase() {
        return diasPlazoBase;
    }

    /** Prioridad con la que queda la solicitud. Por defecto, la que se pidio. */
    public Prioridad prioridadPara(Prioridad solicitada) {
        return solicitada;
    }

    public static Optional<TipoSolicitud> desdeTexto(String texto) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.name().equals(texto))
                .findFirst();
    }
}
