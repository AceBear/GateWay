package hub.gateway.mgr

import hub.gateway.Application
import hub.gateway.repo.Repos
import hub.gateway.x3rd.OAuth2ForQQ

class UserMgr: Mgr() {
    // 热对象缓冲
    private val _hotCacheUSER = HotCache<String, User>()

    // QQ登录服务
    private val _qq = Application.getCtx().getBean(OAuth2ForQQ::class.java)


    /**
     * 从QQ登录获取的Code找到对应的用户
     * 用户如果是第1次登录,会创建用户记录
     * 随后的登录,会从存储里找回用户
     */
    fun findFromQQ(code: String): User? {
        // 访问QQ的服务器,验证code的有效性
        val tokenQQ = _qq.getAccessTokenByCode(code)

        // 拿到OpenId
        val openId = _qq.getOpenIdByAccessToken(tokenQQ)

        // 在我们的存储中检索OpenId,看看是否已经存在
        var uid = Repos.userRepo.findUidByQQOpenId(_qq.appId, openId)

        // 如果不存在,访问QQ的服务器,获取用户昵称头像性别,记录在我们的存储中
        if(uid === null) {
            val qqInfo = _qq.getUserInfo(tokenQQ, openId)
            uid = Repos.userRepo.createUserFromQQ(_qq.appId, openId, qqInfo)
        }

        // 尝试从缓冲区中检索
        var usr = _hotCacheUSER.get(uid)

        if(usr === null){
            // 缓冲区中没有,从存储中加载
            usr = Repos.userRepo.findUserByUID(uid)
            if(usr !== null) {
                _hotCacheUSER.put(uid, usr)
            }
            else {
                // 存储中的QQRef无效,清理掉无效数据
                Repos.userRepo.deleteQQRef(_qq.appId, openId)
            }
        }

        return usr
    }

    /**
     * 创建会话
     */
    fun createSession(uid:String, provider:String, userAgent:String):Session{
        val sess = Repos.sessRepo.createSession(uid, provider, userAgent)
        return sess
    }

    /**
     * 从uid+token找到对应的用户
     * 有可能找不到,返回null
     */
    fun findUserBySession(uid:String, token:String):User?{
        val sess = Repos.sessRepo.findSession(uid, token)
        if(sess === null) return null

        val user = Repos.userRepo.findUserByUID(sess.uid)
        return user
    }

    /**
     * 删除session即注销
     */
    fun deleteSession(uid:String, token:String){
        Repos.sessRepo.deleteSession(uid, token)
    }
}