package com.two.workflow_tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ErrorResponse {
    private final List<String> errors;

    public ErrorResponse(@JsonProperty("errors") List<String> errors) {
        this.errors = errors;
    }

}