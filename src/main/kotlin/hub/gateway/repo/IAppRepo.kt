package hub.gateway.repo

import hub.gateway.mgr.*

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
     * 修改app
     */
    fun modifyApp(app:App)
}