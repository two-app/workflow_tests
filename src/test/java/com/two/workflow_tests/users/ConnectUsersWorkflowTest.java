package com.two.workflow_tests.users;

import com.two.http_api.model.PublicApiModel.UserRegistration;
import com.two.http_api.model.Tokens;
import com.two.workflow_tests.GatewayAPI;
import com.two.workflow_tests.PartnerDetails;
import com.two.workflow_tests.TestUserRegistration;
import com.two.workflow_tests.UserDetails;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConnectUsersWorkflowTest {

    private GatewayAPI gatewayAPI = new GatewayAPI();

    private UserRegistration userOneRegistration = TestUserRegistration.validUser();
    private UserRegistration userTwoRegistration = TestUserRegistration.validUser();
    private UserDetails userOne;
    private UserDetails userTwo;
    private Tokens userOneTokens;
    private Tokens userTwoTokens;

    @Test
    @DisplayName("it should register user one")
    @Order(0)
    void registersUserOne() {
        userOneTokens = gatewayAPI.registerUser(userOneRegistration).expectStatus().isOk().returnResult(Tokens.class)
                .getResponseBody().blockFirst();

        assertThat(userOneTokens).isNotNull();
        assertThat(userOneTokens.getAccessToken()).isNotEmpty();
        assertThat(userOneTokens.getRefreshToken()).isNotEmpty();
    }

    @Test
    @DisplayName("it should register user two")
    @Order(1)
    void registersUserTwo() {
        userTwoTokens = gatewayAPI.registerUser(userTwoRegistration).expectStatus().isOk().returnResult(Tokens.class)
                .getResponseBody().blockFirst();

        assertThat(userTwoTokens).isNotNull();
        assertThat(userTwoTokens.getAccessToken()).isNotEmpty();
        assertThat(userTwoTokens.getRefreshToken()).isNotEmpty();
    }

    @Test
    @DisplayName("it should return 400 BAD REQUEST reattempting sign up")
    @Order(2)
    void reattemptSignupFails() {
        gatewayAPI.registerUser(userOneRegistration).expectStatus().isBadRequest();
        gatewayAPI.registerUser(userTwoRegistration).expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("user one should have a connect token")
    @Order(3)
    void userOneHasConnectToken() {
        userOne = UserDetails.fromTokens(userOneTokens);
        assertThat(userOne.isConnected).isFalse();
    }

    @Test
    @DisplayName("user two should have a connect token")
    @Order(4)
    void userTwoHasConnectToken() {
        userTwo = UserDetails.fromTokens(userTwoTokens);
        assertThat(userTwo.isConnected).isFalse();
    }

    @Test
    @DisplayName("user one should have no partner")
    @Order(5)
    void userOneNoPartner() {
        gatewayAPI.getPartner(userOneTokens.getAccessToken()).expectStatus().isNotFound();
    }

    @Test
    @DisplayName("user two should have no partner")
    @Order(6)
    void userTwoNoPartner() {
        gatewayAPI.getPartner(userTwoTokens.getAccessToken()).expectStatus().isNotFound();
    }

    @Test
    @DisplayName("it should connect user one to user two, returning new tokens")
    @Order(7)
    void userOneConnectGeneratesNewTokens() {
        userOneTokens = gatewayAPI.connect(userOneTokens.getAccessToken(), userTwo.connectCode).expectStatus().isOk()
                .returnResult(Tokens.class).getResponseBody().blockFirst();

        assertThat(userOneTokens).isNotNull();
        assertThat(userOneTokens.getAccessToken()).isNotEmpty();
        assertThat(userOneTokens.getRefreshToken()).isNotEmpty();

        userOne = UserDetails.fromTokens(userOneTokens);
    }

    @Test
    @DisplayName("it should update user twos tokens on login")
    @Order(8)
    void userTwoLoginGeneratesNewTokens() {
        userTwoTokens = gatewayAPI.login(userTwoRegistration.getEmail(), userTwoRegistration.getPassword())
                .expectStatus().isOk().returnResult(Tokens.class).getResponseBody().blockFirst();

        assertThat(userTwoTokens).isNotNull();
        assertThat(userTwoTokens.getAccessToken()).isNotEmpty();
        assertThat(userTwoTokens.getRefreshToken()).isNotEmpty();

        userTwo = UserDetails.fromTokens(userTwoTokens);
    }

    @Test
    @DisplayName("user one should be connected to user two")
    @Order(9)
    void userOneConnectedToUserTwo() {
        assertThat(userOne.isConnected).isTrue();
        assertThat(userOne.pid).isEqualTo(userTwo.uid);
    }

    @Test
    @DisplayName("user two should be connected to user one")
    @Order(10)
    void userTwoConnectedToUserOne() {
        assertThat(userTwo.isConnected).isTrue();
        assertThat(userTwo.pid).isEqualTo(userOne.uid);
    }

    @Test
    @DisplayName("both users should have the same couple id")
    @Order(11)
    void sameCoupleId() {
        assertThat(userOne.cid).isEqualTo(userTwo.cid);
    }

    @Test
    @DisplayName("it should return userTwo when userOne GET /partner")
    @Order(12)
    void userOneGetPartner() {
        PartnerDetails userOnePartner = gatewayAPI.getPartner(userOneTokens.getAccessToken()).expectStatus().isOk()
                .returnResult(PartnerDetails.class).getResponseBody().blockFirst();

        assertThat(userOnePartner).isNotNull();
        assertThat(userOnePartner.uid).isEqualTo(userOne.pid);
        assertThat(userOnePartner.pid).isEqualTo(userOne.uid);
        assertThat(userOnePartner.cid).isEqualTo(userOne.cid);
        assertThat(userOnePartner.firstName).isEqualTo(userTwoRegistration.getFirstName());
        assertThat(userOnePartner.lastName).isEqualTo(userTwoRegistration.getLastName());
    }

    @Test
    @DisplayName("it should return userOne when userTwo GET /partner")
    @Order(13)
    void userTwoGetPartner() {
        PartnerDetails userTwoPartner = gatewayAPI.getPartner(userTwoTokens.getAccessToken()).expectStatus().isOk()
                .returnResult(PartnerDetails.class).getResponseBody().blockFirst();

        assertThat(userTwoPartner).isNotNull();
        assertThat(userTwoPartner.uid).isEqualTo(userTwo.pid);
        assertThat(userTwoPartner.pid).isEqualTo(userTwo.uid);
        assertThat(userTwoPartner.cid).isEqualTo(userTwo.cid);
        assertThat(userTwoPartner.firstName).isEqualTo(userOneRegistration.getFirstName());
        assertThat(userTwoPartner.lastName).isEqualTo(userOneRegistration.getLastName());
    }
}
