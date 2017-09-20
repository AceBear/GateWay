package hub.gateway.ots

import com.alicloud.openservices.tablestore.*
import com.alicloud.openservices.tablestore.model.*
import hub.gateway.Application
import hub.gateway.portal.AliYunConfig

abstract class Repository(tableName:String, timeToLiveSeconds:Int = -1, maxVersion: Int = 1) {
    // OTS表是否已经存在
    private var _isTableExist = false

    // OTS表名
    protected var _tableName = tableName
    // OTS表Options
    protected var _tableOpt = TableOptions(timeToLiveSeconds, maxVersion)
    // OTS主键名
    protected var _pk = "k"
    // OTS客户端
    protected var _ots: SyncClientInterface

    init {
        val aliYunCfg = Application.getCtx().getBean(AliYunConfig::class.java)
        _ots = aliYunCfg.getOTSSyncClient()
        ensureTable()
    }

    // 创建最常用的String类型的PrimaryKey
    protected fun PriKeyStr(value:String): PrimaryKey {
        var build = PrimaryKeyBuilder.createPrimaryKeyBuilder()
        build.addPrimaryKeyColumn(_pk, PrimaryKeyValue.fromString(value))
        return build.build()
    }

    // 最常用的单行记录查询
    protected fun query(pk: PrimaryKey):Row?{
        var criteria = SingleRowQueryCriteria(_tableName, pk)
        criteria.maxVersions = 1

        var resp = _ots.getRow(GetRowRequest(criteria))
        return resp.row
    }

    // 最常用的删除单行记录
    protected fun delete(pk: PrimaryKey){
        var del = RowDeleteChange(_tableName, pk)
        _ots.deleteRow(DeleteRowRequest(del))
    }

    // 确保表存在
    private fun ensureTable(){
        // 先前已经确认表存在
        if(_isTableExist) return

        var reqCheck = DescribeTableRequest(_tableName)

        try {
            _ots.describeTable(reqCheck)
        }catch(ex: TableStoreException){
            if(ex.errorCode == "OTSObjectNotExist") {

                var meta = TableMeta(_tableName)
                meta.addPrimaryKeyColumn(_pk, PrimaryKeyType.STRING)

                var reqCreate = CreateTableRequest(meta, _tableOpt)
                _ots.createTable(reqCreate)

                // 表创建后,OTS需要几秒钟时间来准备资源
                // 如果立即访问,有可能会报告 OTSTableNotReady或OTSPartitionUnavailable
                // 对我们的程序来说,这不是一个大问题,过几秒重试即可

                // 设定标记,以后不用检查了
                _isTableExist = true
            }
            else{
                // 其它错误
                throw ex
            }
        }
    }
}

object Repos {
    val userRepo = UserRepo()
    val sessRepo = SessionRepo()
}