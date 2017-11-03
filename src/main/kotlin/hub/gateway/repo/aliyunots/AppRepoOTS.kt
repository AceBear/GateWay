package hub.gateway.repo.aliyunots

import com.alicloud.openservices.tablestore.model.*
import hub.gateway.mgr.App
import hub.gateway.mgr.AppNameOnly
import hub.gateway.repo.IAppRepo
import java.security.MessageDigest
import java.util.*
import com.alicloud.openservices.tablestore.model.ColumnValue
import com.alicloud.openservices.tablestore.model.condition.SingleColumnValueCondition
import hub.gateway.realm.DataRealm
import hub.gateway.repo.AppAbility
import org.slf4j.LoggerFactory


/**
 *  前缀4字符的数据区
 *  APP主存储区     APP#4|APPID32
 *  APP功能区       APP#4|APPID32|BLT#4|TARGET16|REALM16
 *  APP引用区       AREF4|UID32|OID2|APPID32
 */
class AppRepoOTS : RepoOTS("app"), IAppRepo {
    companion object {
        val s_logger = LoggerFactory.getLogger(AppRepoOTS::class.java)
    }

    val _rand = Random()

    override fun createApp(app: App): App {
        require(app.uid.length == 32){ "预期uid有32个字符" }
        require(app.oid in 0x01 .. 0xfe){ "预期oid在1~255之间" }

        require(app.name.isNotBlank()){ "App名称不能是空白" }
        require(app.base.isNotBlank()){ "App基地址不能是空白" }

        // 重名验证,限制相同org下的app不可以重名
        val listApps = getApps(app.uid, app.oid)
        for(apx in listApps){
            if(apx.name.equals(app.name, true))
                throw IllegalArgumentException("App${app.name}已经存在")
        }

        val appId = genAppId(app.uid, app.oid)
        val pk = PriKeyStr("APP#$appId")

        val put = RowPutChange(_tableName, pk)
        put.addColumn("name", ColumnValue.fromString(app.name))
        put.addColumn("base", ColumnValue.fromString(app.base))
        put.addColumn("uid", ColumnValue.fromString(app.uid))
        put.addColumn("oid", ColumnValue.fromLong(app.oid.toLong()))

        val pkRef = PriKeyStr(String.format("AREF${app.uid}%02X${appId}", app.oid))
        val putRef = RowPutChange(_tableName, pkRef)
        putRef.addColumn("name", ColumnValue.fromString(app.name))

        val puts = BatchWriteRowRequest()
        puts.addRowChange(put)
        puts.addRowChange(putRef)

        _ots.batchWriteRow(puts)

        // 这里有2个写入操作:写入主数据,写入引用数据
        // 有小概率发生1个成功,另1个失败的情况,以后需要再加入2个补充逻辑
        // 补充1: 如果1个失败,那么用删除操作对冲掉成功的那个
        // 补充2: 检测不匹配的数据,删除掉不正确的
        // 补充1能解决大部分问题,但删除操作仍然有可能失败
        // 补充2可能由其它操作(如登录)触发,也可能由定时触发,但都不可能太频繁,只做为补充

        return App(appId, app)
    }

    override fun getApps(uid: String, oid: Int): List<AppNameOnly> {
        require(uid.length == 32){ "预期uid有32个字符" }
        require(oid in 0x01 .. 0xfe){ "预期oid在1~255之间" }

        val listApps = ArrayList<AppNameOnly>()

        val rangeQuery = RangeRowQueryCriteria(_tableName)
        var pkStart : PrimaryKey? = PriKeyStr(String.format("AREF${uid}%02X", oid))
        rangeQuery.exclusiveEndPrimaryKey = PriKeyStr(String.format("AREF${uid}%02Xzzzz", oid))
        rangeQuery.maxVersions = 1

        var resp: GetRangeResponse
        do{
            rangeQuery.inclusiveStartPrimaryKey = pkStart
            resp = _ots.getRange(GetRangeRequest(rangeQuery))

            for(row in resp.rows){
                val pkv = row.primaryKey.getPrimaryKeyColumn(_pk).value.asString()

                val appId = pkv.substring(38)
                val name = row.getLatestColumn("name").value.asString()

                val app = AppNameOnly(appId)
                app.name = name
                listApps.add(app)
            }

            pkStart = resp.nextStartPrimaryKey

        }while(pkStart !== null)

        return listApps
    }

    override fun getApp(appId: String): App? {
        require(appId.length == 32){ "预期appId有32个字符" }

        val qry = RangeRowQueryCriteria(_tableName)
        qry.inclusiveStartPrimaryKey = PriKeyStr("APP#$appId")
        qry.exclusiveEndPrimaryKey = PriKeyStr("APP#${appId}z")
        qry.maxVersions = 1

        var app:App? = null
        while(true){
            val resp = _ots.getRange(GetRangeRequest(qry))

            // 以下2种数据都会取出来
            // APP主存储区    APP#4|APPID32
            // APP功能区      APP#4|APPID32|BLT#4|TARGET16|REALM16
            for(row in resp.rows){
                val key = row.primaryKey.getPrimaryKeyColumn(_pk).value.asString()
                if(key.length == 4+32){
                    app = App(appId)
                    app.name = row.getLatestColumn("name").value.asString()
                    app.base = row.getLatestColumn("base").value.asString()
                    app.uid = row.getLatestColumn("uid").value.asString()
                    app.oid = row.getLatestColumn("oid").value.asLong().toInt()
                }
                else{
                    // 按我们指定的查询方式,一定是先返回app
                    // 如果app是空,表明APP主存储区数据丢失
                    if(app == null){
                        s_logger.warn("App信息丢失:$appId")
                        break
                    }

                    // fetch features
                    val target = key.substring(4+32+4, 4+32+4+16).trim('#')
                    val realm = key.substring(4+32+4+16).trim('#')

                    val level = row.getLatestColumn("level").value.asLong().toInt()
                    val version = row.getLatestColumn("version").value.asString()

                    if(!app.abi.containsKey(target)){
                        app.abi.put(target, HashMap<String, DataRealm>())
                    }

                    s_logger.info("$target $realm $level $version")
                    val rlm = DataRealm.getAllDataRealms().first{
                        s_logger.info("${it.id} ${it.level} ${it.ver}")
                        it.id == realm && it.level == level
                        && "${it.ver}" == version
                    }
                    app.abi[target]!!.put(realm, rlm)
                }
            }

            if(resp.nextStartPrimaryKey != null)
                qry.inclusiveStartPrimaryKey = resp.nextStartPrimaryKey
            else
                break
        }

        return app
    }

    override fun deleteApp(uid:String, oid:Int, appId: String) {
        require(uid.length == 32){ "预期uid有32个字符" }
        require(oid in 0x01 .. 0xfe){ "预期oid在1~255之间" }
        require(appId.length == 32){ "预期appId有32个字符" }

        val pkRef = PriKeyStr(String.format("AREF${uid}%02X${appId}", oid))
        val pk = PriKeyStr("APP#$appId")

        val puts = BatchWriteRowRequest()
        puts.addRowChange(RowDeleteChange(_tableName, pkRef))
        puts.addRowChange(RowDeleteChange(_tableName, pk))

        _ots.batchWriteRow(puts)
    }

    override fun modifyApp(app: App) {
        require(app.id.length == 32){ "预期appId有32个字符" }

        require(app.name.isNotBlank()){ "App名称不能是空白" }
        require(app.base.isNotBlank()){ "App基地址不能是空白" }

        // 重名验证,限制相同org下的app不可以重名
        val listApps = getApps(app.uid, app.oid)
        for(apx in listApps){
            if(apx.id == app.id) continue
            if(apx.name.equals(app.name, true))
                throw IllegalArgumentException("App${app.name}已经存在")
        }

        val pk = PriKeyStr("APP#${app.id}")
        val put = RowPutChange(_tableName, pk)
        put.addColumn("name", ColumnValue.fromString(app.name))
        put.addColumn("base", ColumnValue.fromString(app.base))
        put.addColumn("uid", ColumnValue.fromString(app.uid))
        put.addColumn("oid", ColumnValue.fromLong(app.oid.toLong()))

        // 不允许修改uid & oid
        val condition = Condition(RowExistenceExpectation.EXPECT_EXIST)
        condition.columnCondition = SingleColumnValueCondition("uid",
                SingleColumnValueCondition.CompareOperator.EQUAL, ColumnValue.fromString(app.uid))
        condition.columnCondition = SingleColumnValueCondition("oid",
                SingleColumnValueCondition.CompareOperator.EQUAL, ColumnValue.fromLong(app.oid.toLong()))
        put.condition = condition

        _ots.putRow(PutRowRequest(put))
    }

    override fun addAbility(appId: String, abi: AppAbility): App {
        require(appId.length == 32){ "预期appId有32个字符" }

        require(abi.target.isNotBlank()){ "target不能是空白" }
        require(abi.target.length <= 16){ "预期target至多有16个字符" }

        require(abi.realm.isNotBlank()){ "realm不能是空白" }
        require(abi.realm.length <= 16){ "预期realm至多有16个字符" }


        val pkv = "APP#${appId}BLT#${pad(abi.target, 16)}${pad(abi.realm, 16)}"
        val pk = PriKeyStr(pkv)
        val put = RowPutChange(_tableName, pk)
        put.addColumn("level", ColumnValue.fromLong(abi.level.toLong()))
        put.addColumn("version", ColumnValue.fromString("${abi.version}"))

        _ots.putRow(PutRowRequest(put))

        return getApp(appId)!!
    }

    // 生成AID的算法
    private fun genAppId(uid: String, oid: Int):String{
        val md5 = MessageDigest.getInstance("MD5")
        val t = "${uid}${oid}${_rand.nextInt()}"
        val md5v = md5.digest(t.toByteArray())

        val sbHex = StringBuilder()
        md5v.forEach { v -> sbHex.append(String.format("%02x", v)) }

        val aid = sbHex.toString()
        check(aid.length == 32){ "期望appId由32个字符组成" }
        return aid
    }

    // 补足长度
    private fun pad(value: String, len: Int, c: Char = '#'): String{
        require(len > 0){ "len必须大于0" }
        require(value.length <= len){ "value超长" }

        val sbPad = StringBuilder(len)
        sbPad.append(value)
        for(i in 0..len-value.length){
            sbPad.append(c)
        }

        return sbPad.toString()
    }
}