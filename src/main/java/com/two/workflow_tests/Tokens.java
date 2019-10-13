package com.two.workflow_tests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Tokens {

    private String refreshToken;
    private String accessToken;

}
