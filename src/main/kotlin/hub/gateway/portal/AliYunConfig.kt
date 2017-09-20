package hub.gateway.portal

import com.alicloud.openservices.tablestore.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class AliYunConfig {
    @Value("\${aliyun.accountId}")
    private lateinit var accountId:String

    @Value("\${aliyun.accessKeyId}")
    private lateinit var accessKeyId:String

    @Value("\${aliyun.accessKeySecret}")
    private lateinit var accessKeySecret:String

    @Value("\${aliyun.OTS.endpoint}")
    private lateinit var epOTS:String

    companion object {
        // 根据阿里云OTS官方解释,整个应用只需要1个
        private var _otsSyncClient:SyncClientInterface? = null
    }

    fun getOTSSyncClient(): SyncClientInterface {
        if(_otsSyncClient === null){
            var name : String? = null
            // endpoint的第1节就是实例名称
            val rgxName = Pattern.compile("https?://([\\w\\-]+)\\.")
            var matcher = rgxName.matcher(epOTS)
            if(matcher.find())
                name = matcher.group(1)

            _otsSyncClient = SyncClient(epOTS, accessKeyId, accessKeySecret, name)
        }

        return _otsSyncClient!!
    }
}