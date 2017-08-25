package hub.gateway.shared;

import com.alicloud.openservices.tablestore.AsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AliYunConfig{
    @Value("${aliyun.accountId}")
    private String accountId;

    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.OTS.endpoint}")
    private String otsEndPoint;

    public AsyncClient createClient(){
        String x = this.getInstanceName();

        AsyncClient client = new AsyncClient(
                this.otsEndPoint,
                this.accessKeyId,
                this.accessKeySecret,
                this.getInstanceName()
        );
        return client;
    }

    private String getInstanceName(){
        Pattern rgxInstName = Pattern.compile("^https?://([^\\.]+)\\.");
        Matcher matcher = rgxInstName.matcher(this.otsEndPoint);
        if(matcher.find())
            return matcher.group(1);

        return null;
    }
}
