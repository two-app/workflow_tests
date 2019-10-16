package com.two.workflow_tests.users;

import com.two.http_api.model.PublicApiModel.UserRegistration;
import com.two.http_api.model.Tokens;
import com.two.workflow_tests.GatewayAPI;
import com.two.workflow_tests.TestUserRegistration;
import com.two.workflow_tests.UserDetails;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConnectUsersWorkflowTest {

    private GatewayAPI gatewayAPI = new GatewayAPI();

    private UserRegistration userRegistration;
    private UserRegistration partnerRegistration;

    private Tokens userTokens;
    private Tokens partnerTokens;

    private UserDetails userDetails;
    private UserDetails partnerDetails;

    private Tokens updatedUserTokens;
    private Tokens updatedPartnerTokens;

    private UserDetails updatedUserDetails;
    private UserDetails updatedPartnerDetails;

    @Test
    @DisplayName("it should register two new users, returning their respective connect tokens")
    @Order(0)
    void generatesConnectTokens() {
        userRegistration = TestUserRegistration.validUser();
        partnerRegistration = TestUserRegistration.validUser();

        userTokens = gatewayAPI.registerUser(userRegistration).expectStatus().isOk().returnResult(Tokens.class)
                .getResponseBody().blockFirst();
        partnerTokens = gatewayAPI.registerUser(partnerRegistration).expectStatus().isOk().returnResult(Tokens.class)
                .getResponseBody().blockFirst();

        userDetails = UserDetails.fromTokens(userTokens);
        partnerDetails = UserDetails.fromTokens(partnerTokens);

        assertThat(userDetails.isConnected).isFalse();
        assertThat(partnerDetails.isConnected).isFalse();
    }

    @Test
    @DisplayName("it should connect the user with the partner, returning refreshed access tokens")
    @Order(1)
    void generatesAccessToken() {
        updatedUserTokens = gatewayAPI.connect(userTokens.getAccessToken(), partnerDetails.connectCode)
                .expectStatus().isOk().returnResult(Tokens.class).getResponseBody().blockFirst();

        updatedUserDetails = UserDetails.fromTokens(updatedUserTokens);

        assertThat(updatedUserDetails.isConnected).isTrue();
        assertThat(updatedUserDetails.pid).isEqualTo(partnerDetails.uid);
        assertThat(updatedUserDetails.cid).isGreaterThan(0);
    }

    @Test
    @DisplayName("it should return connected access tokens when the partner logs in")
    @Order(2)
    void partnerGeneratesAccessToken() {
        System.out.println(partnerRegistration.getEmail());
        System.out.println(partnerRegistration.getPassword());
        updatedPartnerTokens = gatewayAPI.login(partnerRegistration.getEmail(), partnerRegistration.getPassword())
                .expectStatus().isOk().returnResult(Tokens.class).getResponseBody().blockFirst();

        updatedPartnerDetails = UserDetails.fromTokens(updatedPartnerTokens);

        assertThat(updatedPartnerDetails.isConnected).isTrue();
        assertThat(updatedPartnerDetails.pid).isEqualTo(userDetails.uid);
        assertThat(updatedPartnerDetails.cid).isEqualTo(updatedUserDetails.cid);
    }

}
