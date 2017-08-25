package hub.gateway.user;

import hub.gateway.shared.AliYunConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserSrv {
    @Autowired
    private ApplicationContext _appCtx;

    @RequestMapping(value = "/api/user/hello", produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String getHello(){

        return "Hello";
    }

    @RequestMapping("/api/user/login")
    public OAuth2LoginReq login(@RequestBody OAuth2LoginReq loginReq) throws Exception {
        if(loginReq.provider.equalsIgnoreCase("alipay")){
            // 1. find authcode
            String authCode = null;
            for(String pair : loginReq.query.split("&")){
                String[] para = pair.split("=");
                if(para.length == 2 && para[0].equalsIgnoreCase("auth_code"))
                {
                    authCode = para[1];
                    break;
                }
            }

            // 2. authCode to accessToken
            AliPayConfig cfg = _appCtx.getBean(AliPayConfig.class);
            OAuthAliPay aliPay = new OAuthAliPay(cfg);
            OAuth2Token o2 = aliPay.findAccessToken(authCode);

            // 3. try to find user
            AliYunConfig aliyunCfg = _appCtx.getBean(AliYunConfig.class);
            UserRepo repo = new UserRepo(aliyunCfg);
            User user = repo.findUserByAliPayId(o2.userId);

        }
        return loginReq;
    }
}

class OAuth2LoginReq {
    public String provider;
    public String query;
}