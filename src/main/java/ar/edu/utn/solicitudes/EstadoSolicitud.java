package ar.edu.utn.solicitudes;

import java.util.Arrays;
import java.util.Optional;

public enum EstadoSolicitud {
    PENDIENTE {
        @Override
        public boolean puedePasarA(EstadoSolicitud nuevo) {
            return nuevo == EN_PROCESO || nuevo == RECHAZADA;
        }
    },
    EN_PROCESO {
        @Override
        public boolean puedePasarA(EstadoSolicitud nuevo) {
            return nuevo == RESUELTA || nuevo == RECHAZADA;
        }

        @Override
        public boolean requiereResponsable() {
            return true;
        }
    },
    RESUELTA {
        @Override
        public boolean puedePasarA(EstadoSolicitud nuevo) {
            return false;
        }
    },
    RECHAZADA {
        @Override
        public boolean puedePasarA(EstadoSolicitud nuevo) {
            return false;
        }
    };

    /** Indica si desde este estado se puede pasar al estado indicado. */
    public abstract boolean puedePasarA(EstadoSolicitud nuevo);

    /** Indica si para entrar a este estado la solicitud necesita un responsable. */
    public boolean requiereResponsable() {
        return false;
    }

    /** Un estado es final cuando no admite ninguna transicion. */
    public boolean esFinal() {
        return Arrays.stream(values()).noneMatch(this::puedePasarA);
    }

    public static Optional<EstadoSolicitud> desdeTexto(String texto) {
        return Arrays.stream(values())
                .filter(estado -> estado.name().equals(texto))
                .findFirst();
    }
}
