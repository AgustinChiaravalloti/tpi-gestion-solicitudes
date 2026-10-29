package ar.edu.utn.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del formato CSV que corrige el defecto D-03.
 */
class FormatoCsvTest {

    @Test
    @DisplayName("Une campos simples con punto y coma")
    void uneCamposSimples() {
        assertEquals("1;CERTIFICADO;Ana", FormatoCsv.unir(List.of("1", "CERTIFICADO", "Ana")));
    }

    @Test
    @DisplayName("Escapa el punto y coma y la barra dentro de un texto")
    void escapaCaracteresEspeciales() {
        assertEquals("Algebra\\; urgente;C:\\\\datos",
                FormatoCsv.unir(List.of("Algebra; urgente", "C:\\datos")));
    }

    @Test
    @DisplayName("Lo que se une se vuelve a separar igual (ida y vuelta)")
    void idaYVuelta() {
        List<String> campos = List.of("2", "Equivalencia de Algebra; urgente", "", "barra \\ final\\");

        assertEquals(campos, FormatoCsv.separar(FormatoCsv.unir(campos)));
    }

    @Test
    @DisplayName("Lee las lineas del formato anterior sin escapes")
    void leeFormatoAnterior() {
        assertEquals(List.of("1", "CERTIFICADO", "2", "PENDIENTE"),
                FormatoCsv.separar("1;CERTIFICADO;2;PENDIENTE"));
    }
}
