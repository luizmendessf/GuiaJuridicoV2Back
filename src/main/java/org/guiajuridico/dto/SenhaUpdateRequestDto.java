package org.guiajuridico.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SenhaUpdateRequestDto {
    private String senhaAntiga;
    private String senhaNova;
}