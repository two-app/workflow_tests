package com.two.workflow_tests;

import static com.two.http_api.model.PublicApiModel.UserRegistration;

public class TestUserRegistration {
    public static UserRegistration validUser() {
        return new UserRegistration(
                UserUtil.uniqueEmail(),
                "testPassword",
                "Workflow",
                "Test",
                true,
                true
        );
    }
}
