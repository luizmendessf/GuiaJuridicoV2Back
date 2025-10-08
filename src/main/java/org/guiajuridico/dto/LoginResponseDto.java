package org.guiajuridico.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor // Cria um construtor com todos os argumentos
public class LoginResponseDto {
    private String token;
}