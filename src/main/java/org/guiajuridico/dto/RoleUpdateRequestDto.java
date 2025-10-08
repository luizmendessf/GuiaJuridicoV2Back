package org.guiajuridico.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class RoleUpdateRequestDto {
    private Set<String> nomesDasRoles;
}