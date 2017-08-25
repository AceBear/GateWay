package hub.gateway.user;

import java.time.Duration;

abstract class OAuth2 {
    public abstract OAuth2Token findAccessToken(String authCode) throws Exception;
}


class OAuth2Token {
    public String userId;
    public String token;
    public Duration expired;
}