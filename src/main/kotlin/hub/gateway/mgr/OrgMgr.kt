package hub.gateway.mgr

import hub.gateway.repo.Repos

class OrgMgr:Mgr() {
    // 热对象缓冲
    private val _hotCacheORG = HotCache<String, Org>()

    /**
     * 查询一个用户的所有组织
     */
    fun getOrgs(uid:String):List<Org>{
        return Repos.orgRepo.getOrgs(uid)
    }

    /**
     * 查询一个特定的组织
     */
    fun getOrg(uid:String, oid:Int):Org?{

        val org = _hotCacheORG.get(CacheKey4Org(uid, oid).key){
            _ -> Repos.orgRepo.getOrg(uid, oid)
        }

        return org
    }

    /**
     * 创建组织
     */
    fun createOrg(uid:String, name:String):Org{
        val org = Repos.orgRepo.createOrg(uid, name)

        _hotCacheORG.put(CacheKey4Org(uid, org.id).key, org)

        return org
    }

    /**
     * 删除组织
     */
    fun deleteOrg(uid:String, oid:Int){
        // 如果组织里还有正在运行的APP,不允许删除
        var listApps = Repos.appRepo.getApps(uid, oid)
        check(listApps.isEmpty()){ "组织不能被删除, 因为其中还有${listApps.size}个App" }


        // 删除组织对象
        Repos.orgRepo.deleteOrg(uid, oid)
        _hotCacheORG.remove(CacheKey4Org(uid, oid).key)
    }
}

class CacheKey4Org(val uid:String, val oid:Int){
    val key
        get() = "$uid$oid"
}