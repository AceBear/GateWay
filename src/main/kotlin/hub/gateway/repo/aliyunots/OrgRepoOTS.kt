package hub.gateway.repo.aliyunots

import com.alicloud.openservices.tablestore.*
import com.alicloud.openservices.tablestore.model.*
import com.alicloud.openservices.tablestore.model.condition.*
import hub.gateway.mgr.*
import hub.gateway.repo.IOrgRepo

/**
 *  前缀4字符的数据区
 *  ORG主存储区     ORG#4|UID32|OID2
 *  BRANCH主存储区  BRCH4|UID32|OID2|BID#4|BID4
 *  BRANCH树形结构  BRCH4|UID32|OID2|BID#4|BID4(|BID#4|BID4)+
 *  BRANCH信息     BRCH4|UID32|OID2|BID#4|BID4|????|????
 *  ORG引用区      UREF4|UID32|UID32|OID2
 */
class OrgRepoOTS : RepoOTS("org"), IOrgRepo {
    override fun createOrg(uid: String, name: String): Org {
        require(uid.length == 32){ "预期uid有32个字符" }
        require(name.isNotBlank()){"组织名称不允许空白"}
        require(name.length == name.trim().length){"组织名称不允许以空白字符开头或结尾"}

        val listOrgs = getOrgs(uid)
        // 重名验证,限制owner为同一用户的Org不可以重名
        for(org in listOrgs){
            if(org.name.equals(name, true))
                throw IllegalArgumentException("组织${name}已经存在")
        }

        // 确定OID序号
        var oid = 1
        val listExistOids = listOrgs.map({o -> o.id})
        for(i in 1..0xff){
            if(!listExistOids.contains(i)){
                oid = i
                break
            }
        }

        check(oid < 0xff){ "一个用户至多只允许创建254个组织" }

        val pkv = String.format("ORG#%s%02X", uid, oid)
        val pk = PriKeyStr(pkv)
        var put = RowPutChange(_tableName, pk)

        put.addColumn("name", ColumnValue.fromString(name))

        put.condition = Condition(RowExistenceExpectation.EXPECT_NOT_EXIST)
        put.condition.columnCondition = SingleColumnValueCondition(_pk, SingleColumnValueCondition.CompareOperator.EQUAL, ColumnValue.fromString(pkv))

        try {
            _ots.putRow(PutRowRequest(put))
        }catch(ex: TableStoreException){
            throw RuntimeException("主键冲突,请重试")
        }

        var org = getOrg(uid, oid)
        return org!!
    }

    override fun getOrgs(uid: String) : List<Org>{
        require(uid.length == 32){ "预期uid有32个字符" }

        var listOrgs = ArrayList<Org>()

        var rangeQuery = RangeRowQueryCriteria(_tableName)
        var pkStart : PrimaryKey? = PriKeyStr("ORG#${uid}01")
        rangeQuery.exclusiveEndPrimaryKey = PriKeyStr("ORG#${uid}zz")
        rangeQuery.maxVersions = 1

        var resp: GetRangeResponse
        do{
            rangeQuery.inclusiveStartPrimaryKey = pkStart
            resp = _ots.getRange(GetRangeRequest(rangeQuery))

            for(row in resp.rows){
                var pkv = row.primaryKey.getPrimaryKeyColumn(_pk).value.asString()
                var org = Org(pkv.substring(pkv.length-2).toInt(16))

                val colName = row.getLatestColumn("name")
                org.name = colName.value.asString()
                org.ts = colName.timestamp
                org.ownerUserId = uid

                listOrgs.add(org)
            }

            pkStart = resp.nextStartPrimaryKey

        }while(pkStart !== null)

        return listOrgs
    }

    override fun getOrg(uid: String, oid: Int): Org?{
        require(uid.length == 32){ "预期uid有32个字符" }
        require(oid > 0x00 && oid < 0xff){ "预期oid在1~254之间" }

        val pkv = String.format("ORG#%s%02X", uid, oid)
        val pk = PriKeyStr(pkv)

        var get = SingleRowQueryCriteria(_tableName, pk)
        get.maxVersions = 1

        val resp = _ots.getRow(GetRowRequest(get))
        val row = resp.row

        if(row === null) return null

        var org = Org(oid)

        val colName = row.getLatestColumn("name")
        org.name = colName.value.asString()
        org.ts = colName.timestamp
        org.ownerUserId = uid

        return org
    }

    override fun deleteOrg(uid: String, oid: Int){
        require(uid.length == 32){ "预期uid有32个字符" }
        require(oid > 0x00 && oid < 0xff){ "预期oid在1~254之间" }

        val pkv = String.format("ORG#%s%02X", uid, oid)
        val pk = PriKeyStr(pkv)

        val del = RowDeleteChange(_tableName, pk)
        _ots.deleteRow(DeleteRowRequest(del))
    }

}