package com.banco.utils;

import java.util.regex.Pattern;

/**
 * Validaciones centralizadas para el número de documento.
 */
public final class DocumentoValidator {

    public static final int MAX_DIGITOS = 10;
    private static final Pattern SOLO_NUMEROS = Pattern.compile("^\\d+$");

    private DocumentoValidator() {}

    public static String normalizar(String documento) {
        return documento == null ? "" : documento.trim();
    }

    public static void validar(String documento) {
        String doc = normalizar(documento);

        if (doc.isEmpty()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        if (!SOLO_NUMEROS.matcher(doc).matches()) {
            throw new IllegalArgumentException(
                "El documento solo puede contener números. No se permiten letras ni caracteres especiales.");
        }
        if (doc.length() > MAX_DIGITOS) {
            throw new IllegalArgumentException(
                "El documento no puede superar los " + MAX_DIGITOS + " dígitos.");
        }
    }
}
