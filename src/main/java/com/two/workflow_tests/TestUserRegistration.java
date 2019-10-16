package com.two.workflow_tests;

import java.time.LocalDate;

import static com.two.http_api.model.PublicApiModel.UserRegistration;

public class TestUserRegistration extends UserRegistration {

    public TestUserRegistration(String email, String password, String name, LocalDate dob) {
        super(email, password, name, dob);
    }

    public static UserRegistration validUser() {
        return new UserRegistration(
                UserUtil.uniqueEmail(),
                "testPassword",
                "WorkflowTest",
                LocalDate.parse("1997-08-21")
        );
    }

}
