package org.guiajuridico.validation;

/**
 * Padrões restritos para cadastro público — bloqueia payloads de scanner (SQLi, etc.) nos campos de texto.
 */
public final class RegistrationValidationPatterns {

    private RegistrationValidationPatterns() {
    }

    public static final String EMAIL =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$";

    public static final String NOME =
            "^[\\p{L}0-9 .'\\-]{2,120}$";

    public static final String CELULAR =
            "^[0-9+().\\-\\s]{8,20}$";
}
