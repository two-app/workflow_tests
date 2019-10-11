package com.two.workflow_tests.register;

import com.two.workflow_tests.UserLogin;
import com.two.workflow_tests.UserRegistration;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

class AuthenticateUserTest {

    private WebTestClient gateway;

    @BeforeEach
    void beforeEach() {
        gateway = WebTestClient.bindToServer().baseUrl("http://0.0.0.0:8080").build();
    }

    @Nested
    class RegisterUser {
        @Test
        @DisplayName("it should return a pair of tokens")
        void registerUserSuccess() {
            registerValidUser()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.accessToken").isNotEmpty()
                    .jsonPath("$.refreshToken").isNotEmpty();
        }

        @Test
        @DisplayName("it should return a Bad Request if the account already exists")
        void accountExists() {
            UserRegistration user = validUser();

            // create first account
            registerUser(user).expectStatus().isOk();

            // create second account
            registerUser(user).expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("This user already exists.");
        }

        @Test
        @DisplayName("it should return a Bad Request if registration data is missing")
        void missingData() {
            // will not test all permutations, just enough to check that bean validation is occurring
            UserRegistration invalidUser = validUser().toBuilder().name(null).build();

            registerUser(invalidUser).expectStatus().isBadRequest()
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

            UserRegistration invalidUser = validUser().toBuilder().dob(invalidAge).build();

            registerUser(invalidUser).expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Age must be greater than 13.");
        }

        @Test
        @DisplayName("it should return a Bad Request if the email is malformed")
        void invalidEmail() {
            UserRegistration invalidUser = validUser().toBuilder().email("wrong").build();

            registerUser(invalidUser).expectStatus().isBadRequest();
        }
    }

    @Nested
    class Login {
        @Test
        @DisplayName("it should return a bad request if the credentials are invalid")
        void invalidCredentials() {
            login("unknown@gmail.com", "rawPassword").expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("This user does not exist.");
        }

        @Test
        @DisplayName("it should return a bad request if the user exists but password is incorrect")
        void wrongPassword() {
            UserRegistration validUser = validUser();
            registerUser(validUser);

            login(validUser.getEmail(), "wrongPassword")
                    .expectStatus().isBadRequest();
        }
    }

    private WebTestClient.ResponseSpec registerValidUser() {
        return registerUser(validUser());
    }

    private WebTestClient.ResponseSpec registerUser(UserRegistration userRegistration) {
        return gateway.post().uri("/self")
                .body(BodyInserters.fromObject(userRegistration))
                .exchange();
    }

    private WebTestClient.ResponseSpec login(String email, String password) {
        return gateway.post().uri("/login")
                .body(BodyInserters.fromObject(new UserLogin(email, password)))
                .exchange();
    }

    private UserRegistration validUser() {
        return UserRegistration.builder()
                .name("registerUserWorkflowTest")
                .email(("registerUserWorkflowTest-" + RandomString.make(10) + "@two.com"))
                .dob(LocalDate.parse("1997-08-21"))
                .password("aTestPassw0rd")
                .build();
    }

}
