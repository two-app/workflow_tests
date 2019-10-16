package com.two.workflow_tests;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static com.two.http_api.model.PublicApiModel.UserRegistration;

public class GatewayAPI {

    private WebTestClient client() {
        String baseUrl = "http://0.0.0.0:8080";
        return WebTestClient.bindToServer().baseUrl(baseUrl).build();
    }

    public WebTestClient.ResponseSpec registerValidUser() {
        return registerUser(TestUserRegistration.validUser());
    }

    public WebTestClient.ResponseSpec registerUser(UserRegistration userRegistration) {
        return client().post().uri("/self")
                .body(BodyInserters.fromObject(userRegistration))
                .exchange();
    }

    public WebTestClient.ResponseSpec login(String email, String password) {
        return client().post().uri("/login")
                .body(BodyInserters.fromObject(new UserLogin(email, password)))
                .exchange();
    }

    public WebTestClient.ResponseSpec connect(String accessToken, String partnerConnectCode) {
        return client().post().uri("/connect/" + partnerConnectCode)
                .header("Authorization", "Bearer " + accessToken)
                .exchange();
    }
}
