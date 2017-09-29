package hub.gateway.portal


import hub.gateway.mgr.*
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*

@RestController
class UserController {

    /**
     * 第三方登录
     */
    @RequestMapping(path=arrayOf("/portal/user/login"), method=arrayOf(RequestMethod.POST))
    fun login(@RequestBody data: LoginFrom3rd, @RequestHeader headers: HttpHeaders): UserInfoLogin {
        require(data.provider.equals("qq", true)){ "暂不支持QQ以外的登录方式" }

        val usr = Mgrs.userMgr.findFromQQ(data.code)
        check(usr !== null){ "QQ登录失败" }

        // 如果存在,生成登录token,返回给客户端,同时返回的还有用户昵称头像token有效期
        val headUA = headers.getFirst("user-agent")?:"NA"
        val sess = Mgrs.userMgr.createSession(usr!!.id, data.provider, headUA)

        return UserInfoLogin(usr, sess.token)
    }

    /**
     * 根据登录token查询用户
     * 如果token被从服务器端清除,token会无效
     */
    @RequestMapping(path=arrayOf("/portal/user/token"), method=arrayOf(RequestMethod.POST))
    fun findUserByToken(@RequestBody data: LoginToken): UserInfo?{
        val sess = Session(data.token)
        val usr = Mgrs.userMgr.findUserBySession(sess.uid, sess.token)

        if(usr === null) return null

        return UserInfo(usr)
    }

    /**
     * 从服务器端清除登录会话
     */
    @RequestMapping(path=arrayOf("/portal/user/logout"), method=arrayOf(RequestMethod.DELETE))
    fun logout(@RequestBody data: LoginToken){
        val sess = Session(data.token)
        Mgrs.userMgr.deleteSession(sess.uid, sess.token)
    }
}

class LoginFrom3rd{
    lateinit var provider:String
    lateinit var code:String
}

class LoginToken{
    lateinit var token:String
}