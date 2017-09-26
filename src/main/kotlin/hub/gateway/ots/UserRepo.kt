package hub.gateway.ots

import com.alicloud.openservices.tablestore.model.*
import hub.gateway.mgr.User
import hub.gateway.portal.QQUserInfo
import java.lang.RuntimeException
import java.security.MessageDigest
import java.time.*
import java.util.*

/* 前缀4字符的数据区
*  用户主存储区 USER4|UID32
*  用户QQ OpenId存储区 QQ##|AppId16|OpenId32
*/
class UserRepo : Repository("user", -1, 1) {

    // 按QQ-OpenId检索用户
    fun findUserByQQOpenId(appId:String, openId:String) : User? {
        if(appId.length != 9)
            throw IllegalArgumentException("预期QQ分配的AppID有9个字符")
        if(openId.length != 32)
            throw IllegalArgumentException("预期QQ分配的OpenID有32个字符")

        // 从QQ引用数据查找用户主数据
        val pkQQRef = PriKeyStr("QQ##${appId}#######${openId}")
        val rowQQRef = query(pkQQRef)

        // 没有查到记录
        if(rowQQRef === null) return null


        val uid = rowQQRef.getLatestColumn("uid").value.asString()

        // 检索用户主数据
        val pk = PriKeyStr("USER${uid}")
        val row = query(pk)

        if(row === null){
            // 表明存在QQ登录记录,但对应的用户确不存在
            // 这是一个不正确的状态,删除对应的QQ登录记录
            delete(pkQQRef)
            return null
        }

        var user = User(uid, row)

        return user
    }

    fun findUserByUID(uid:String):User?{
        val pk = PriKeyStr("USER${uid}")
        val row = query(pk)

        if(row === null) return null

        return User(uid, row)
    }

    // 利用QQ授权创建用户
    fun createUserFromQQLogin(appId:String, openId:String, qq: QQUserInfo){
        // 创建2条数据
        // 主数据: USER4|UID32
        // QQ引用: QQ##|AppId16|OpenId32

        // 构造PK
        if(appId.length != 9)
            throw IllegalArgumentException("预期QQ分配的AppID有9个字符")

        if(openId.length != 32)
            throw IllegalArgumentException("预期QQ分配的OpenID有32个字符")

        // Main
        val uid = genUID(appId, openId)
        val pk = PriKeyStr("USER${uid}")

        var put = RowPutChange(_tableName, pk)
        put.addColumn("nick", ColumnValue.fromString(qq.nickname))

        if(qq.gender == "男")
            put.addColumn("gender", ColumnValue.fromString("男"))
        else if(qq.gender == "女")
            put.addColumn("gender", ColumnValue.fromString("女"))
        else{
            // 忽略其它的性别
        }

        try {
            val year = Integer.parseInt(qq.year)
            put.addColumn("year", ColumnValue.fromLong(year.toLong()))
        }
        catch (ex: NumberFormatException){
            // ignore
        }

        put.addColumn("province", ColumnValue.fromString(qq.province))
        put.addColumn("city", ColumnValue.fromString(qq.city))
        put.addColumn("head40", ColumnValue.fromString(qq.figureurl_qq_1))
        put.addColumn("head100", ColumnValue.fromString(qq.figureurl_qq_2))

        val pkvQQRef = "QQ##${appId}#######${openId}"
        put.addColumn("QQ", ColumnValue.fromString(pkvQQRef))

        put.addColumn("created", ColumnValue.fromString(ZonedDateTime.now(ZoneId.of("UTC")).toString()))

        // QQ reference
        val pkQQRef = PriKeyStr(pkvQQRef)

        var putQQRef = RowPutChange(_tableName, pkQQRef)
        putQQRef.addColumn("uid", ColumnValue.fromString(uid))

        // OTS
        var puts = BatchWriteRowRequest()
        puts.addRowChange(put)
        puts.addRowChange(putQQRef)

        _ots.batchWriteRow(puts)

        // 这里有2个写入操作:写入主数据,写入QQ引用数据
        // 有小概率发生1个成功,另1个失败的情况,以后需要再加入2个补充逻辑
        // 补充1: 如果1个失败,那么用删除操作对冲掉成功的那个
        // 补充2: 检测不匹配的数据,删除掉不正确的
        // 补充1能解决大部分问题,但删除操作仍然有可能失败
        // 补充2可能由其它操作(如登录)触发,也可能由定时触发,但都不可能太频繁,只做为补充
    }

    // 生成UID的算法
    private fun genUID(appId:String, openId:String):String{
        var md5 = MessageDigest.getInstance("MD5")
        val t = "${appId}${openId}${ZonedDateTime.now(ZoneId.of("UTC")).toString()}"
        val md5v = md5.digest(t.toByteArray())

        var b64 = Base64.getEncoder()
        val uid64 = String(b64.encode(md5v))
        if(uid64.length != 24)
            throw RuntimeException("期望有24字符")

        return "${uid64}########"
    }
}