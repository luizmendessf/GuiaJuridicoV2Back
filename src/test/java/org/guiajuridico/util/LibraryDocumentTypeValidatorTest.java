package org.guiajuridico.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryDocumentTypeValidatorTest {

    @Test
    void aceitaTodosOsTiposValidos() {
        for (String tipo : LibraryDocumentTypeValidator.TIPOS) {
            assertTrue(LibraryDocumentTypeValidator.tipoValido(tipo));
            assertDoesNotThrow(() -> LibraryDocumentTypeValidator.validar(tipo));
        }
    }

    @Test
    void rejeitaNuloOuEmBranco() {
        assertFalse(LibraryDocumentTypeValidator.tipoValido(null));
        assertFalse(LibraryDocumentTypeValidator.tipoValido(""));
        assertThrows(RuntimeException.class, () -> LibraryDocumentTypeValidator.validar(null));
        assertThrows(RuntimeException.class, () -> LibraryDocumentTypeValidator.validar("   "));
    }

    @Test
    void rejeitaTipoInvalido() {
        assertFalse(LibraryDocumentTypeValidator.tipoValido("Tipo Inventado"));
        assertThrows(RuntimeException.class, () -> LibraryDocumentTypeValidator.validar("Tipo Inventado"));
    }
}
