package com.two.workflow_tests;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class UserRegistration {

    private String email;

    private String password;

    private String name;

    private LocalDate dob;
}