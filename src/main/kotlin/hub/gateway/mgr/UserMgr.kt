package hub.gateway.mgr

import hub.gateway.Application
import hub.gateway.repo.Repos
import hub.gateway.x3rd.OAuth2ForQQ

class UserMgr: Mgr() {
    private val _qq = Application.getCtx().getBean(OAuth2ForQQ::class.java)

    /**
     * 从QQ登录获取的Code找到对应的用户
     * 用户如果是第1次登录,会创建用户记录
     * 随后的登录,会从存储里找回用户
     */
    fun findFromQQ(code: String): UserAgent{
        // 访问QQ的服务器,验证code的有效性
        val tokenQQ = _qq.getAccessTokenByCode(code)

        // 拿到OpenId
        val openId = _qq.getOpenIdByAccessToken(tokenQQ)

        // 在我们的存储中检索OpenId,看看是否已经存在
        var uid = Repos.userRepo.findUidByQQOpenId(_qq.appId, openId)

        // TODO: 如果存在,先尝试从缓冲区中检索,再尝试从存储中加载

        // 如果不存在,访问QQ的服务器,获取用户昵称头像性别,记录在我们的存储中
        if(uid === null) {
            val qqInfo = _qq.getUserInfo(tokenQQ, openId)
            uid = Repos.userRepo.createUserFromQQ(_qq.appId, openId, qqInfo)
        }

        val userAgent = Repos.userRepo.findUserByUID(uid)
        return userAgent!!
    }
}