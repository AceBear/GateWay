package hub.gateway.repo

import hub.gateway.mgr.User
import hub.gateway.x3rd.QQUserInfo

interface IUserRepo {
    /**
     * 根据用户的QQ OpenId查找用户uid
     * 有可能找不到返回null
     * 由于有Mgr层的存在,有可能从Mgr的缓冲区中直接得到UserAgent
     * 而不需要真正去访问存储层
     * 所以此方法仅返回uid,而不是UserAgent
     */
    fun findUidByQQOpenId(qqAppId:String, openId:String):String?

    /**
     * 删除QQRef
     * 有一个很小的概率导致存储中有QQRef,但找不到对应的user
     * 这表明QQRef已经失效,需要被删除
     */
    fun deleteQQRef(qqAppId:String, openId:String)

    /**
     * 为来自QQ登录的用户创建帐号
     * 返回新创建用户的uid
     */
    fun createUserFromQQ(qqAppId:String, openId:String, info: QQUserInfo): String

    /**
     * 根据uid查询用户
     * 有可能找不到返回null
     * 根据最近最常用原理,此方法返回的对象值得被Mgr层缓冲
     */
    fun findUserByUID(uid:String): User?
}