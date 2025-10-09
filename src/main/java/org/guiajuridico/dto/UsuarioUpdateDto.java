package org.guiajuridico.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioUpdateDto {
    private String nome;
    private String celular;
    private String senhaAtual; // Para validação quando alterar senha
    private String senhaNova;  // Nova senha (opcional)
}