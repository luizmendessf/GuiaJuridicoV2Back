package org.guiajuridico.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CadastroUsuarioDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void rejectsSqlInjectionStyleEmail() {
        CadastroUsuarioDto dto = validDto();
        dto.setEmail("test' OR 1=1--@test.com");

        Set<ConstraintViolation<CadastroUsuarioDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsInvalidNomeCharacters() {
        CadastroUsuarioDto dto = validDto();
        dto.setNome("Robert'); DROP TABLE usuarios;--");

        Set<ConstraintViolation<CadastroUsuarioDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void acceptsNormalBrazilianRegistration() {
        CadastroUsuarioDto dto = validDto();
        dto.setNome("Maria da Silva");
        dto.setEmail("maria.silva@gmail.com");
        dto.setCelular("(21) 98888-7777");

        Set<ConstraintViolation<CadastroUsuarioDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void rejectsShortPassword() {
        CadastroUsuarioDto dto = validDto();
        dto.setSenha("123");

        Set<ConstraintViolation<CadastroUsuarioDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    private static CadastroUsuarioDto validDto() {
        CadastroUsuarioDto dto = new CadastroUsuarioDto();
        dto.setNome("João Teste");
        dto.setEmail("joao.teste@gmail.com");
        dto.setSenha("senha123");
        dto.setCelular("(11) 99999-9999");
        return dto;
    }
}
