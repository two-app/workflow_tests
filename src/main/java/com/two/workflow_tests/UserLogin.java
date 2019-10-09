package com.two.workflow_tests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLogin {
    private final String email;
    private final String password;
}
