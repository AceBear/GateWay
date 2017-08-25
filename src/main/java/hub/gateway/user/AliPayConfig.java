package hub.gateway.user;

import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class AliPayConfig {
    @Value("${alipay.api}")
    private String api;

    @Value("${alipay.app_id}")
    private String appId;

    @Value("${alipay.ali_pub_key}")
    private String aliPubKey;

    @Value("${alipay.app_pub_key}")
    private String pubKey;

    @Value("${alipay.app_pri_key}")
    private String priKey;

    public DefaultAlipayClient createClient(){
        DefaultAlipayClient client = new DefaultAlipayClient(
                this.api,
                this.appId,
                this.priKey,
                "json",
                "GBK",
                this.aliPubKey,
                "RSA2");
        return client;
    }
}
