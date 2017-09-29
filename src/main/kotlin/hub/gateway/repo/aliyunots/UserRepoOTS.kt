package hub.gateway.repo.aliyunots

import com.alicloud.openservices.tablestore.model.*
import hub.gateway.mgr.User
import hub.gateway.repo.IUserRepo
import hub.gateway.x3rd.QQUserInfo
import java.security.MessageDigest
import java.time.*
import java.util.*

/**
 *  前缀4字符的数据区
 *  用户主存储区 USER4|UID32
 *  用户QQ OpenId存储区 QQ##|AppId16|OpenId32
 */
class UserRepoOTS:RepoOTS("user", "k", -1, 1), IUserRepo {
    override fun findUidByQQOpenId(qqAppId: String, openId: String): String? {
        require(qqAppId.length == 9){ "预期QQ分配的QQAppID有9个字符" }
        require(openId.length == 32){ "预期QQ分配的OpenID有32个字符" }

        // 从QQ引用数据查找用户主数据
        val pkQQRef = PriKeyStr("QQ##${qqAppId}#######${openId}")
        val rowQQRef = query(pkQQRef)

        // 没有查到记录
        if(rowQQRef === null) return null

        val uid = rowQQRef.getLatestColumn("uid").value.asString()
        assert(uid.isNotEmpty())

        return uid
    }

    override fun deleteQQRef(qqAppId: String, openId: String) {
        require(qqAppId.length == 9){ "预期QQ分配的QQAppID有9个字符" }
        require(openId.length == 32){ "预期QQ分配的OpenID有32个字符" }

        val pk = PriKeyStr("QQ##${qqAppId}#######${openId}")
        delete(pk)
    }

    override fun createUserFromQQ(qqAppId: String, openId: String, info: QQUserInfo): String {
        require(qqAppId.length == 9){ "预期QQ分配的QQAppID有9个字符" }
        require(openId.length == 32){ "预期QQ分配的OpenID有32个字符" }

        val uid = genUID(qqAppId, openId)
        val pk = PriKeyStr("USER${uid}")

        val put = RowPutChange(_tableName, pk)
        put.addColumn("nick", ColumnValue.fromString(info.nickname))

        if(info.gender == "男")
            put.addColumn("gender", ColumnValue.fromString("男"))
        else if(info.gender == "女")
            put.addColumn("gender", ColumnValue.fromString("女"))
        else{
            // 忽略其它的性别
        }

        try {
            val year = Integer.parseInt(info.year)
            put.addColumn("year", ColumnValue.fromLong(year.toLong()))
        }
        catch (ex: NumberFormatException){
            // ignore
        }

        put.addColumn("province", ColumnValue.fromString(info.province))
        put.addColumn("city", ColumnValue.fromString(info.city))
        put.addColumn("head40", ColumnValue.fromString(info.figureurl_qq_1))
        put.addColumn("head100", ColumnValue.fromString(info.figureurl_qq_2))

        val pkvQQRef = "QQ##${qqAppId}#######${openId}"
        put.addColumn("QQ", ColumnValue.fromString(pkvQQRef))

        put.addColumn("created", ColumnValue.fromString(ZonedDateTime.now(ZoneId.of("UTC")).toString()))

        // QQ reference
        val pkQQRef = PriKeyStr(pkvQQRef)

        val putQQRef = RowPutChange(_tableName, pkQQRef)
        putQQRef.addColumn("uid", ColumnValue.fromString(uid))

        // OTS
        val puts = BatchWriteRowRequest()
        puts.addRowChange(put)
        puts.addRowChange(putQQRef)

        _ots.batchWriteRow(puts)

        // 这里有2个写入操作:写入主数据,写入QQ引用数据
        // 有小概率发生1个成功,另1个失败的情况,以后需要再加入2个补充逻辑
        // 补充1: 如果1个失败,那么用删除操作对冲掉成功的那个
        // 补充2: 检测不匹配的数据,删除掉不正确的
        // 补充1能解决大部分问题,但删除操作仍然有可能失败
        // 补充2可能由其它操作(如登录)触发,也可能由定时触发,但都不可能太频繁,只做为补充

        return uid
    }

    override fun findUserByUID(uid:String): User?{
        val pk = PriKeyStr("USER${uid}")
        val row = query(pk)

        if(row === null) return null

        return User(uid, row)
    }

    // 生成UID的算法
    private fun genUID(qqAppId:String, openId:String):String{
        var md5 = MessageDigest.getInstance("MD5")
        val t = "${qqAppId}${openId}${ZonedDateTime.now(ZoneId.of("UTC")).toString()}"
        val md5v = md5.digest(t.toByteArray())

        var b64 = Base64.getEncoder()
        val uid64 = String(b64.encode(md5v))

        check(uid64.length == 24){ "期望uid有24字符" }

        return "${uid64}########"
    }
}