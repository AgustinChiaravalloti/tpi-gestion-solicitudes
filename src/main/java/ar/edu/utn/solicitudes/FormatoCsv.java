package ar.edu.utn.solicitudes;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte una lista de campos en una linea CSV y viceversa.
 * Escapa el separador para que un ';' dentro de un texto no rompa la linea.
 */
public final class FormatoCsv {

    private static final char SEPARADOR = ';';
    private static final char ESCAPE = '\\';

    private FormatoCsv() {
    }

    public static String unir(List<String> campos) {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < campos.size(); i++) {
            if (i > 0) {
                linea.append(SEPARADOR);
            }
            linea.append(escapar(campos.get(i)));
        }
        return linea.toString();
    }

    public static List<String> separar(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean escapado = false;
        for (char c : linea.toCharArray()) {
            if (escapado) {
                actual.append(c);
                escapado = false;
            } else if (c == ESCAPE) {
                escapado = true;
            } else if (c == SEPARADOR) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }

    private static String escapar(String texto) {
        return texto
                .replace(String.valueOf(ESCAPE), "" + ESCAPE + ESCAPE)
                .replace(String.valueOf(SEPARADOR), "" + ESCAPE + SEPARADOR);
    }
}
