package com.two.workflow_tests.users;

import com.two.http_api.model.PublicApiModel.UserRegistration;
import com.two.http_api.model.Tokens;
import com.two.workflow_tests.GatewayAPI;
import com.two.workflow_tests.TestUserRegistration;
import com.two.workflow_tests.UserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticateUserTest {
    private GatewayAPI gatewayAPI = new GatewayAPI();

    @Nested
    class RegisterUser {
        @Test
        @DisplayName("it should return a pair of tokens, where the access token is of connect type")
        void registerUserSuccess() {
            Tokens tokens = gatewayAPI.registerValidUser()
                    .expectStatus().isOk()
                    .returnResult(Tokens.class)
                    .getResponseBody()
                    .blockFirst();

            UserDetails userDetails = UserDetails.fromTokens(tokens);

            assertThat(userDetails.isConnected).isFalse();
            assertThat(userDetails.connectCode).isNotEmpty();
            assertThat(userDetails.uid).isGreaterThan(0);
        }

        @Test
        @DisplayName("it should return a Bad Request if the account already exists")
        void accountExists() {
            UserRegistration user = TestUserRegistration.validUser();

            // create first account
            gatewayAPI.registerUser(user).expectStatus().isOk();

            // create second account
            gatewayAPI.registerUser(user).expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("This user already exists.");
        }

        @Test
        @DisplayName("it should return a Bad Request if registration data is missing")
        void missingData() {
            // will not test all permutations, just enough to check that bean validation is occurring
            UserRegistration invalidUser = TestUserRegistration.validUser().setFirstName(null);

            gatewayAPI.registerUser(invalidUser).expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("First Name must be provided.");
        }

        @Test
        @DisplayName("it should return a bad request if terms and conditions not accepted")
        void invalidAge() {
            UserRegistration invalidUser = TestUserRegistration.validUser().setAcceptedTerms(false);

            gatewayAPI.registerUser(invalidUser).expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Terms & Conditions must be accepted.");
        }

        @Test
        @DisplayName("it should return a Bad Request if the email is malformed")
        void invalidEmail() {
            UserRegistration invalidUser = TestUserRegistration.validUser().setEmail("wrong");

            gatewayAPI.registerUser(invalidUser).expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Email must be valid.");
        }
    }

    @Nested
    class Login {
        @Test
        @DisplayName("it should return tokens when the credentials are valid")
        void validCredentials() {
            UserRegistration userRegistration = TestUserRegistration.validUser();

            Tokens registerTokens = gatewayAPI.registerUser(userRegistration)
                    .expectStatus().isOk()
                    .returnResult(Tokens.class)
                    .getResponseBody().blockFirst();

            UserDetails registeredDetails = UserDetails.fromTokens(registerTokens);

            Tokens loginTokens = gatewayAPI.login(userRegistration.getEmail(), userRegistration.getPassword())
                    .expectStatus().isOk()
                    .returnResult(Tokens.class)
                    .getResponseBody().blockFirst();

            UserDetails loggedInDetails = UserDetails.fromTokens(loginTokens);

            assertThat(registeredDetails).isEqualTo(loggedInDetails);
        }

        @Test
        @DisplayName("it should return a bad request if the credentials are invalid")
        void invalidCredentials() {
            gatewayAPI.login("unknown@gmail.com", "rawPassword")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("This user does not exist.");
        }

        @Test
        @DisplayName("it should return a bad request if the user exists but password is incorrect")
        void wrongPassword() {
            UserRegistration validUser = TestUserRegistration.validUser();
            gatewayAPI.registerUser(validUser);

            gatewayAPI.login(validUser.getEmail(), "wrongPassword")
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Incorrect password.");
        }
    }

}
