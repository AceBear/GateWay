package hub.gateway.user;


import com.alipay.api.*;
import com.alipay.api.request.*;
import com.alipay.api.response.*;

import java.time.Duration;

class OAuthAliPay extends OAuth2 {
    private DefaultAlipayClient _client;

    public OAuthAliPay(AliPayConfig cfg){
        this._client = cfg.createClient();
    }

    @Override
    public OAuth2Token findAccessToken(String authCode) throws Exception {
        // AccessToken
        AlipaySystemOauthTokenRequest tokenReq = new AlipaySystemOauthTokenRequest();
        tokenReq.setCode(authCode);
        tokenReq.setGrantType("authorization_code");
        AlipaySystemOauthTokenResponse tokenResp = this._client.execute(tokenReq);
        OAuth2Token o2 = new OAuth2Token();
        o2.userId = tokenResp.getUserId();
        o2.token = tokenResp.getAccessToken();
        o2.expired = Duration.ofSeconds(Long.parseUnsignedLong(tokenResp.getExpiresIn()));

        return o2;

        // User Info
        // AlipayUserInfoShareRequest userReq = new AlipayUserInfoShareRequest();
        // AlipayUserInfoShareResponse userResp = this._client.execute(userReq, token);
        // String name = userResp.getUserName();
        // String nick = userResp.getNickName();
    }
}
