package hub.gateway.realm

import hub.gateway.realm.define.*

/**
 * 数据领域
 * 数据领域针对一项TargetClass
 *     比如对于“地理位置”DataRealm,
 *     它可以针对“汽车”,也可以针对“树”;
 *     而“充放电”DataRealm对“树”是无效的;
 *     但可以针对“电动汽车”,
 *     也可以针对某些使用电池的“鼠标”`
 */
abstract class DataRealm {
    abstract val id: String
    /**
     * 标准级别 1-精简 2-标准 3-扩展
     */
    abstract val level: Int
    abstract val ver: DataRealmVersion
    abstract val chs: String

    /**
     * 功能
     */
    lateinit var funcs: List<Func>

    companion object {
        fun getAllDataRealms():List<DataRealm>{
            // 这里添加所有定义
            return arrayListOf(HelloWorld_Slim_100())
        }
    }
}

/**
 * 版本号
 * major.minor.fix
 */
class DataRealmVersion(val major:Int = 1, val minor:Int = 0, val fix:Int = 0){
    override fun toString(): String {
        return "$major.$minor.$fix"
    }
}