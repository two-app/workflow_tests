package com.two.workflow_tests.users;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.two.workflow_tests.AuthenticationUtil;
import com.two.workflow_tests.Tokens;
import com.two.workflow_tests.UserRegistration;
import lombok.Value;
import org.junit.jupiter.api.*;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConnectUsersWorkflowTest {

    private AuthenticationUtil authUtil;

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

    @BeforeEach
    void beforeEach() {
        this.authUtil = new AuthenticationUtil(
                WebTestClient.bindToServer().baseUrl("http://0.0.0.0:8080").build()
        );
    }

    @Test
    @DisplayName("it should register two new users, returning their respective connect tokens")
    @Order(0)
    void generatesConnectTokens() {
        userRegistration = authUtil.validUser();
        partnerRegistration = authUtil.validUser();

        userTokens = authUtil.registerUser(userRegistration).expectStatus().isOk().returnResult(Tokens.class)
                .getResponseBody().blockFirst();
        partnerTokens = authUtil.registerUser(partnerRegistration).expectStatus().isOk().returnResult(Tokens.class)
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
        updatedUserTokens = authUtil.connect(userTokens.getAccessToken(), partnerDetails.getConnectCode())
                .expectStatus().isOk().returnResult(Tokens.class).getResponseBody().blockFirst();

        updatedUserDetails = UserDetails.fromTokens(updatedUserTokens);

        assertThat(updatedUserDetails.isConnected).isTrue();
        assertThat(updatedUserDetails.pid).isEqualTo(partnerDetails.getUid());
        assertThat(updatedUserDetails.cid).isGreaterThan(0);
    }

    @Test
    @DisplayName("it should return connected access tokens when the partner logs in")
    @Order(2)
    void partnerGeneratesAccessToken() {
        System.out.println(partnerRegistration.getEmail());
        System.out.println(partnerRegistration.getPassword());
        updatedPartnerTokens = authUtil.login(partnerRegistration.getEmail(), partnerRegistration.getPassword())
                .expectStatus().isOk().returnResult(Tokens.class).getResponseBody().blockFirst();

        updatedPartnerDetails = UserDetails.fromTokens(updatedPartnerTokens);

        assertThat(updatedPartnerDetails.isConnected).isTrue();
        assertThat(updatedPartnerDetails.pid).isEqualTo(userDetails.uid);
        assertThat(updatedPartnerDetails.cid).isEqualTo(updatedUserDetails.cid);
    }

    @Value
    static class UserDetails {
        private final int uid;
        private final Integer pid;
        private final Integer cid;
        private final String connectCode;
        private final boolean isConnected;

        static UserDetails fromTokens(Tokens tokens) {
            DecodedJWT jwt = JWT.decode(tokens.getAccessToken());

            int uid = jwt.getClaim("uid").asInt();
            Integer pid = jwt.getClaim("pid").asInt();
            Integer cid = jwt.getClaim("cid").asInt();
            String connectCode = jwt.getClaim("connectCode").asString();
            boolean isConnected = pid != null && cid != null && connectCode == null;

            return new UserDetails(uid, pid, cid, connectCode, isConnected);
        }
    }

}
