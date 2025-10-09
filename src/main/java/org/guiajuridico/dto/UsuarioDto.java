package org.guiajuridico.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class UsuarioDto {
    private Integer id;
    private String nome;
    private String email;
    private Set<String> roles;
}