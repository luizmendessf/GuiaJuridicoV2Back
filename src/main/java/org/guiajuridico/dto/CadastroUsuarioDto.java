package org.guiajuridico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.guiajuridico.validation.RegistrationValidationPatterns;

@Getter
@Setter
public class CadastroUsuarioDto {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 2, max = 120, message = "Nome deve ter entre 2 e 120 caracteres.")
    @Pattern(regexp = RegistrationValidationPatterns.NOME, message = "Nome contém caracteres inválidos.")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório.")
    @Size(max = 255, message = "E-mail muito longo.")
    @Pattern(regexp = RegistrationValidationPatterns.EMAIL, message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "Senha é obrigatória.")
    @Size(min = 6, max = 128, message = "Senha deve ter entre 6 e 128 caracteres.")
    private String senha;

    @NotBlank(message = "Celular é obrigatório.")
    @Size(max = 20, message = "Celular muito longo.")
    @Pattern(regexp = RegistrationValidationPatterns.CELULAR, message = "Informe um celular válido.")
    private String celular;
}
