package ar.edu.utn.solicitudes;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones de solo lectura sobre las solicitudes.
 * Separada de la escritura para que quien solo consulta (por ejemplo, el reporte)
 * no dependa de metodos que no usa (ISP).
 */
public interface ConsultaSolicitudes {

    List<Solicitud> listarTodas();

    Optional<Solicitud> buscarPorId(int id);
}
