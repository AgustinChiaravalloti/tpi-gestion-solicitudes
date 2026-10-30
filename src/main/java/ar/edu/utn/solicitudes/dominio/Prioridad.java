package ar.edu.utn.solicitudes.dominio;

import java.util.Arrays;
import java.util.Optional;

public enum Prioridad {
    ALTA(1) {
        @Override
        public int ajustarPlazo(int dias) {
            return dias / 2;
        }
    },
    MEDIA(2),
    BAJA(3);

    private final int codigo;

    Prioridad(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    /** Plazo final segun la prioridad. Por defecto no se modifica. */
    public int ajustarPlazo(int dias) {
        return dias;
    }

    public static Optional<Prioridad> desdeCodigo(int codigo) {
        return Arrays.stream(values())
                .filter(prioridad -> prioridad.codigo == codigo)
                .findFirst();
    }
}
