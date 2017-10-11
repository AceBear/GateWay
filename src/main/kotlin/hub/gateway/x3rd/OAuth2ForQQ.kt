package hub.gateway.x3rd

import com.alibaba.fastjson.JSON
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.regex.Pattern

@Component
class OAuth2ForQQ {
    val _urlToken = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s"
    val _urlOpenId = "https://graph.qq.com/oauth2.0/me?access_token=%s"
    val _urlUserInfo = "https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s"

    @Value("\${qq.appId}")
    lateinit var appId:String

    @Value("\${qq.appSecret}")
    lateinit var appSecret:String

    @Value("\${qq.appCallback}")
    lateinit var appCallback:String

    fun getAccessTokenByCode(code:String):String{
        val url = String.format(_urlToken, appId, appSecret, code, appCallback)
        var request = RestTemplate()
        val response = request.getForObject(url, String::class.java)

        // access_token=2BC81F9C6DB6030897F84DBDDA244E4C&expires_in=7776000&refresh_token=5F98892EB399BA4F276E3DCB2A894C13
        // callback( {"error":100019,"error_description":"code to access token error"} );

        if(!response.startsWith("access_token=")){
            throw RuntimeException(response)
        }

        var mapToken = HashMap<String, String>()
        for(pair in response.split("&")){
            var kv = pair.split("=")
            if(kv.size == 2){
                mapToken.put(kv[0], kv[1])
            }
        }

        return mapToken.get("access_token")!!
    }

    fun getOpenIdByAccessToken(token:String):String{
        val url = String.format(_urlOpenId, token)
        var request = RestTemplate()
        val response = request.getForObject(url, String::class.java)

        // callback( {"client_id":"101419871","openid":"FC190FD4232665BF6C0BAB4EF3EC4A0F"} );

        var rgxOpenId = Pattern.compile("\"openid\":\"(\\w+)\"")
        var matcher = rgxOpenId.matcher(response)
        if(!matcher.find()) return response

        return matcher.group(1)
    }

    fun getUserInfo(token:String, openId:String): QQUserInfo {
        val url = String.format(_urlUserInfo, token, appId, openId)
        var request = RestTemplate()
        val response = request.getForObject(url, String::class.java)

        var obj = JSON.parseObject(response, QQUserInfo::class.java)

        return obj
    }
}

class QQUserInfo{
    lateinit var nickname:String
    lateinit var gender:String
    lateinit var province:String
    lateinit var city:String
    lateinit var year:String
    lateinit var figureurl_qq_1:String
    lateinit var figureurl_qq_2:String
}

class QQTicket{
    lateinit var appId:String
    lateinit var openId:String
}