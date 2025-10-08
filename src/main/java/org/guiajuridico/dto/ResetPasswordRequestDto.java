package org.guiajuridico.dto;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResetPasswordRequestDto {
    private String token;
    private String novaSenha;
}