package hub.gateway.repo

import hub.gateway.mgr.*

interface IOrgRepo {
    /**
     * 创建一个归属于uid的org
     */
    fun createOrg(uid: String, name: String): Org

    /**
     * 查询一个用户下所有的org
     */
    fun getOrgs(uid: String) : List<Org>

    /**
     * 查询单个org
     */
    fun getOrg(uid: String, oid: Int): Org?

    /**
     * 删除org
     */
    fun deleteOrg(uid: String, oid: Int)
}