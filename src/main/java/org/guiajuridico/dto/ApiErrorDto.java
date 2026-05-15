package org.guiajuridico.dto;

/** Resposta simples para erros 4xx legíveis no front (ex.: axios response.data.message). */
public class ApiErrorDto {

    private String message;

    public ApiErrorDto() {
    }

    public ApiErrorDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
