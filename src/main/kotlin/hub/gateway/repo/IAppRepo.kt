package hub.gateway.repo

import hub.gateway.mgr.*
import hub.gateway.realm.DataRealmVersion

interface IAppRepo {
    /**
     * 创建一个app
     */
    fun createApp(app: App): App

    /**
     * 查询所有app
     */
    fun getApps(uid:String, oid:Int): List<AppNameOnly>

    /**
     * 查询单个app
     */
    fun getApp(appId: String): App?

    /**
     * 删除app
     */
    fun deleteApp(uid:String, oid:Int, appId: String)

    /**
     * 修改app基本信息
     */
    fun modifyApp(app:App)

    /**
     * 增加功能
     */
    fun addAbility(appId: String, abi: AppAbility): App
}

class AppAbility{
    lateinit var target: String
    lateinit var realm: String
    var level: Int = 0
    lateinit var version: DataRealmVersion
}