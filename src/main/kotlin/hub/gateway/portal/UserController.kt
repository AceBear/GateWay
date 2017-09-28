package hub.gateway.portal


import hub.gateway.agent.*
import hub.gateway.repo.Repos
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class UserController {

    @RequestMapping(path=arrayOf("/portal/user/login"), method=arrayOf(RequestMethod.POST))
    fun login(@RequestBody data: LoginFrom3rd, @RequestHeader headers: HttpHeaders): UserLogin {
        require(data.provider.equals("qq", true)){ "暂不支持QQ以外的登录方式" }

        val userAgent = Mgrs.userMgr.findFromQQ(data.code)

        // 如果存在,生成登录token,返回给客户端,同时返回的还有用户昵称头像token有效期
        val headUA = headers.getFirst("user-agent")
        val sessAgent = Repos.sessRepo.createSession(userAgent.id, data.provider, headUA?:"NA")

        return UserLogin(userAgent, sessAgent.token)
    }

    @RequestMapping(path=arrayOf("/portal/user/token"), method=arrayOf(RequestMethod.POST))
    fun findUserByToken(@RequestBody data: LoginToken): User?{
        val sessAgent = Repos.sessRepo.findSession(data.token.substring(0, 32), data.token.substring(32))
        if(sessAgent === null) return null

        val userAgent = Repos.userRepo.findUserByUID(sessAgent.uid)
        return User(userAgent!!)
    }

    @RequestMapping(path=arrayOf("/portal/user/logout"), method=arrayOf(RequestMethod.POST))
    fun logout(@RequestBody data: LoginToken){
        Repos.sessRepo.deleteSession(data.token.substring(0, 32), data.token.substring(32))
    }
}

class LoginFrom3rd{
    lateinit var provider:String
    lateinit var code:String
}

class LoginToken{
    lateinit var token:String
}