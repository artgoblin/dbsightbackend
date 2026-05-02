package com.dbsight.neo.modal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Error {
    public Error() {
        this.message = null;
        this.code = null;
    }

    public Error(String message, String code) {
        this.message = message;
        this.code = code;
    }

    @JsonProperty("message")
    private String message;
    @JsonProperty("code")
    private String code;
}
