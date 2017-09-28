package hub.gateway.repo.aliyunots

import com.alicloud.openservices.tablestore.*
import com.alicloud.openservices.tablestore.model.*
import hub.gateway.Application
import hub.gateway.portal.AliYunConfig
import hub.gateway.repo.Repo

open class RepoOTS(tableName:String, timeToLiveSeconds:Int = -1, maxVersion: Int = 1):Repo() {
    // OTS表名
    protected val _tableName = tableName
    // OTS表Options
    protected val _tableOpt = TableOptions(timeToLiveSeconds, maxVersion)
    // OTS主键名
    protected val _pk = "k"
    // OTS客户端
    protected lateinit var _ots: SyncClientInterface

    override fun ensureInfrastructure() {
        val aliYunCfg = Application.getCtx().getBean(AliYunConfig::class.java)
        _ots = aliYunCfg.getOTSSyncClient()

        var bReady = false
        while(!bReady){
            val reqCheck = DescribeTableRequest(_tableName)
            try {
                _ots.describeTable(reqCheck)
                bReady = true
            }
            catch(ex: TableStoreException){
                if(ex.errorCode == "OTSObjectNotExist") {

                    val meta = TableMeta(_tableName)
                    meta.addPrimaryKeyColumn(_pk, PrimaryKeyType.STRING)

                    val reqCreate = CreateTableRequest(meta, _tableOpt)
                    _ots.createTable(reqCreate)

                    // 表创建后,OTS需要几秒钟时间来准备资源
                    // 如果立即访问,有可能会报告 OTSTableNotReady或OTSPartitionUnavailable
                    Thread.sleep(500)
                }
                else if(ex.errorCode == "OTSTableNotReady" || ex.errorCode == "OTSPartitionUnavailable"){
                    // 再等等
                    Thread.sleep(500)
                }
                else{
                    // 其它错
                    throw ex
                }
            }
        }
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
}