package com.two.workflow_tests;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.two.http_api.model.Tokens;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class UserDetails {
    public final int uid;
    public final Integer pid;
    public final Integer cid;
    public final String connectCode;
    public final boolean isConnected;

    public static UserDetails fromTokens(Tokens tokens) {
        DecodedJWT jwt = JWT.decode(tokens.getAccessToken());

        int uid = jwt.getClaim("uid").asInt();
        Integer pid = jwt.getClaim("pid").asInt();
        Integer cid = jwt.getClaim("cid").asInt();
        String connectCode = jwt.getClaim("connectCode").asString();
        boolean isConnected = pid != null && cid != null && connectCode == null;

        return new UserDetails(uid, pid, cid, connectCode, isConnected);
    }
}
