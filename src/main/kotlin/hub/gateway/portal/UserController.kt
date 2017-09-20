package hub.gateway.portal


import hub.gateway.mgr.User
import hub.gateway.mgr.UserLogin
import hub.gateway.ots.Repos
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class UserController {

    @Autowired
    private lateinit var _appCtx:ApplicationContext

    @RequestMapping(path=arrayOf("/portal/user/login"), method=arrayOf(RequestMethod.POST))
    fun login(@RequestBody data: LoginFrom3rd, @RequestHeader headers: HttpHeaders): UserLogin {
        var qq = _appCtx.getBean(OAuth2ForQQ::class.java)
        // 访问QQ的服务器,验证code的有效性
        val tokenQQ = qq.getAccessTokenByCode(data.code)

        // 拿到OpenId
        val openId = qq.getOpenIdByAccessToken(tokenQQ)

        // 在我们的存储中检索OpenId,看看是否已经存在
        var user = Repos.userRepo.findUserByQQOpenId(qq.appId, openId)

        // 如果不存在,访问QQ的服务器,获取用户昵称头像性别,记录在我们的存储中
        if(user === null) {
            val qqInfo = qq.getUserInfo(tokenQQ, openId)
            Repos.userRepo.createUserFromQQLogin(qq.appId, openId, qqInfo)
            user = Repos.userRepo.findUserByQQOpenId(qq.appId, openId)
        }

        // 如果存在,生成登录token,返回给客户端,同时返回的还有用户昵称头像token有效期
        val headUA = headers.getFirst("user-agent")
        val token = Repos.sessRepo.createSession(user!!.Id, data.provider, headUA?:"NA")

        var userLogin = UserLogin(user)
        userLogin.token = token

        return userLogin
    }

    @RequestMapping(path=arrayOf("/portal/user/token"), method=arrayOf(RequestMethod.POST))
    fun findUserByToken(@RequestBody data: LoginToken): User?{
        val uid = Repos.sessRepo.findSession(data.token)
        if(uid === null) return null

        val user = Repos.userRepo.findUserByUID(uid)
        return user
    }
}

class LoginFrom3rd{
    lateinit var provider:String
    lateinit var code:String
}

class LoginToken{
    lateinit var token:String
}