package hub.gateway.mgr

import hub.gateway.realm.DataRealm
import hub.gateway.realm.DataRealmVersion
import hub.gateway.realm.targetClassHolder
import hub.gateway.repo.AppAbility
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
     * 修改App
     */
    fun modifyApp(app:App){
        _hotCacheApp.remove(app.id)
        Repos.appRepo.modifyApp(app)
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

    /**
     * 向App增加1项功能
     */
    fun addAbility(appId:String, abi: AppAbility):App{
        val app = _hotCacheApp.get(appId){
            _ -> Repos.appRepo.getApp(appId)
        }

        check(app != null){ "App $appId does not exist" }

        // 确认abi里指定的参数都是可行的
        // 1. target存在
        // 2. realm是target可接受的范围
        // 3. 指定的level和version是存在的

        val tc = targetClassHolder.getNode(abi.target)
        check(tc != null){ "${abi.target} do not exist!" }

        check(tc!!.dm != null && tc.dm!!.contains(abi.realm)){ "${abi.target} is not compatible with ${abi.realm}" }

        val realm = DataRealm.getAllDataRealms().firstOrNull {
            it.id == abi.realm && it.level == abi.level
            && it.ver.major == abi.version.major
            && it.ver.minor == abi.version.minor
            && it.ver.fix == abi.version.fix

        }
        check(realm != null){
            "${abi.realm} - ${abi.level} - ${abi.version.major}.${abi.version.minor}.${abi.version.fix} does not exist"
        }

        val appNew = Repos.appRepo.addAbility(app!!.id, abi)

        // update cache
        _hotCacheApp.remove(appNew.id)
        _hotCacheApp.put(appNew.id, appNew)

        return appNew
    }
}