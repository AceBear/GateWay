package hub.gateway.user;

import org.springframework.web.bind.annotation.*;

@RestController
public class UserSrv {
    @RequestMapping(value = "/api/user/hello", produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String getHello(){

        return "Hello";
    }

    @RequestMapping("/api/user/login")
    public OAuth2LoginReq login(@RequestBody OAuth2LoginReq loginReq){
        return loginReq;
    }
}

class OAuth2LoginReq {
    public String provider;
    public String query;
}