package com.two.workflow_tests.users;

import com.two.workflow_tests.AuthenticationUtil;
import com.two.workflow_tests.UserRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

class AuthenticateUserTest {
    private AuthenticationUtil authenticationUtil;

    @BeforeEach
    void beforeEach() {
        this.authenticationUtil = new AuthenticationUtil(
                WebTestClient.bindToServer().baseUrl("http://0.0.0.0:8080").build()
        );
    }

    @Nested
    class RegisterUser {
        @Test
        @DisplayName("it should return a pair of tokens")
        void registerUserSuccess() {
            authenticationUtil.registerValidUser()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.accessToken").isNotEmpty()
                    .jsonPath("$.refreshToken").isNotEmpty();
        }

        @Test
        @DisplayName("it should return a Bad Request if the account already exists")
        void accountExists() {
            UserRegistration user = authenticationUtil.validUser();

            // create first account
            authenticationUtil.registerUser(user).expectStatus().isOk();

            // create second account
            authenticationUtil.registerUser(user).expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("This user already exists.");
        }

        @Test
        @DisplayName("it should return a Bad Request if registration data is missing")
        void missingData() {
            // will not test all permutations, just enough to check that bean validation is occurring
            UserRegistration invalidUser = authenticationUtil.validUser().toBuilder().name(null).build();

            authenticationUtil.registerUser(invalidUser).expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Name must be provided.");
        }

        @Test
        @DisplayName("it should return a bad request if registration age is lower than minimum")
        void invalidAge() {
            // will not test all permutations, just enough to check that bean validation is occurring
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -12);
            LocalDate invalidAge = LocalDateTime.ofInstant(
                    calendar.toInstant(), calendar.getTimeZone().toZoneId()
            ).toLocalDate();

            UserRegistration invalidUser = authenticationUtil.validUser().toBuilder().dob(invalidAge).build();

            authenticationUtil.registerUser(invalidUser).expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Age must be greater than 13.");
        }

        @Test
        @DisplayName("it should return a Bad Request if the email is malformed")
        void invalidEmail() {
            UserRegistration invalidUser = authenticationUtil.validUser().toBuilder().email("wrong").build();

            authenticationUtil.registerUser(invalidUser).expectStatus().isBadRequest();
        }
    }

    @Nested
    class Login {
        @Test
        @DisplayName("it should return tokens when the credentials are valid")
        void validCredentials() {
            UserRegistration userRegistration = authenticationUtil.validUser();

            authenticationUtil.registerUser(userRegistration).expectStatus().isOk();

            authenticationUtil.login(userRegistration.getEmail(), userRegistration.getPassword())
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("it should return a bad request if the credentials are invalid")
        void invalidCredentials() {
            authenticationUtil.login("unknown@gmail.com", "rawPassword").expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("This user does not exist.");
        }

        @Test
        @DisplayName("it should return a bad request if the user exists but password is incorrect")
        void wrongPassword() {
            UserRegistration validUser = authenticationUtil.validUser();
            authenticationUtil.registerUser(validUser);

            authenticationUtil.login(validUser.getEmail(), "wrongPassword")
                    .expectStatus().isBadRequest();
        }
    }

}
