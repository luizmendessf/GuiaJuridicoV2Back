package org.guiajuridico.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadastroUsuarioDto {
    private String nome;
    private String email;
    private String senha;
    private String celular;
}