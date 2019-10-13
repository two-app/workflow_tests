package com.two.workflow_tests;

import net.bytebuddy.utility.RandomString;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDate;

public class AuthenticationUtil {

    private final WebTestClient gateway;

    public AuthenticationUtil(WebTestClient gateway) {
        this.gateway = gateway;
    }

    public WebTestClient.ResponseSpec registerValidUser() {
        return registerUser(validUser());
    }

    public WebTestClient.ResponseSpec registerUser(UserRegistration userRegistration) {
        return gateway.post().uri("/self")
                .body(BodyInserters.fromObject(userRegistration))
                .exchange();
    }

    public WebTestClient.ResponseSpec login(String email, String password) {
        return gateway.post().uri("/login")
                .body(BodyInserters.fromObject(new UserLogin(email, password)))
                .exchange();
    }

    public WebTestClient.ResponseSpec connect(String accessToken, String partnerConnectCode) {
        return gateway.post().uri("/connect/" + partnerConnectCode)
                .header("Authorization", "Bearer " + accessToken)
                .exchange();
    }

    public UserRegistration validUser() {
        return UserRegistration.builder()
                .name("registerUserWorkflowTest")
                .email(("registerUserWorkflowTest-" + RandomString.make(10) + "@two.com"))
                .dob(LocalDate.parse("1997-08-21"))
                .password("aTestPassw0rd")
                .build();
    }
}
