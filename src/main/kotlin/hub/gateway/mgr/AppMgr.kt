package hub.gateway.mgr

import hub.gateway.repo.Repos

class AppMgr:Mgr() {
    // 热对象缓冲
    private val _hotCacheApp = HotCache<String, App>()

    /**
     * 创建App
     */
    fun createApp(app:App):App{
        val appCreated = Repos.appRepo.createApp(app)

        _hotCacheApp.put(appCreated.id, appCreated)

        return appCreated
    }

    /**
     * 获取所有app
     */
    fun getApps(uid:String, oid:Int):List<AppNameOnly>{
        return Repos.appRepo.getApps(uid, oid)
    }

    /**
     * 删除App
     */
    fun deleteApp(uid:String, oid:Int, appId:String){
        val app = getApp(appId)

        // 如果匹配不上uid和oid,则不应该执行删除操作
        if(app != null && uid == app.uid && oid == app.oid){
            Repos.appRepo.deleteApp(uid, oid, appId)
            _hotCacheApp.remove(appId)
        }
    }

    /**
     * 查询单个App
     */
    fun getApp(appId:String):App?{
        val app = _hotCacheApp.get(appId){
            _ -> Repos.appRepo.getApp(appId)
        }

        return app
    }
}