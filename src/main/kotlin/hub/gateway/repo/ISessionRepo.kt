package hub.gateway.repo

import hub.gateway.mgr.Session

interface ISessionRepo {

    /**
     * 创建session记录
     * @param uid 用户ID
     * @param provider 登录来源,如QQ,wechat
     * @param userAgent 浏览器的客户端user-mgr
     */
    fun createSession(uid:String, provider:String, userAgent:String): Session

    /**
     * 从uid+token找到对应的session记录
     * 有可能找不到,返回null
     */
    fun findSession(uid:String, token:String): Session?

    /**
     * 删除session记录
     */
    fun deleteSession(uid:String, token:String)
}