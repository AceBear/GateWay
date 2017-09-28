package hub.gateway.repo

import hub.gateway.agent.*

interface IOrgRepo {
    /**
     * 创建一个归属于uid的org
     */
    fun createOrg(uid: String, name: String): OrgAgent

    /**
     * 查询一个用户下所有的org
     */
    fun getOrgs(uid: String) : List<OrgAgent>

    /**
     * 查询单个org
     */
    fun getOrg(uid: String, oid: Int): OrgAgent?

    /**
     * 删除org
     */
    fun deleteOrg(uid: String, oid: Int)
}