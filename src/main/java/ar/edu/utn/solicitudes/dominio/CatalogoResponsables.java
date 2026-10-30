package ar.edu.utn.solicitudes.dominio;

import java.util.List;
import java.util.Optional;

/**
 * Responsables disponibles para atender solicitudes.
 */
public class CatalogoResponsables {

    private final List<Responsable> responsables;

    public CatalogoResponsables(List<Responsable> responsables) {
        this.responsables = List.copyOf(responsables);
    }

    /** Responsables con los que trabaja el sistema (datos sinteticos). */
    public static CatalogoResponsables predeterminado() {
        return new CatalogoResponsables(List.of(
                new Responsable(1, "Laura Gomez", "Bedelia"),
                new Responsable(2, "Martin Ruiz", "Secretaria Academica"),
                new Responsable(3, "Sofia Diaz", "Departamento Docente")));
    }

    public List<Responsable> listarTodos() {
        return responsables;
    }

    public Optional<Responsable> buscarPorId(int id) {
        return responsables.stream()
                .filter(responsable -> responsable.getId() == id)
                .findFirst();
    }
}
