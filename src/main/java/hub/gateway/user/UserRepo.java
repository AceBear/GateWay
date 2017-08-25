package hub.gateway.user;

import com.alicloud.openservices.tablestore.*;
import hub.gateway.shared.AliYunConfig;

public class UserRepo {
    private AsyncClient _client;

    public UserRepo(AliYunConfig cfg){
        this._client = cfg.createClient();
    }

    public User findUserByAliPayId(String alipayId){
        return null;
    }
}


class User{
    public String id;
    public String nick;
}