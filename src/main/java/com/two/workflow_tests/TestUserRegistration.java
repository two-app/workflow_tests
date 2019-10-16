package com.two.workflow_tests;

import java.time.LocalDate;

import static com.two.http_api.model.PublicApiModel.UserRegistration;

public class TestUserRegistration {
    public static UserRegistration validUser() {
        return new UserRegistration(
                UserUtil.uniqueEmail(),
                "testPassword",
                "WorkflowTest",
                LocalDate.parse("1997-08-21")
        );
    }
}
