package com.two.workflow_tests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class UserLogin {
    private final String email;
    private final String password;
}
