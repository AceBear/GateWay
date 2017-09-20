package hub.gateway.ots

import com.alicloud.openservices.tablestore.model.ColumnValue
import com.alicloud.openservices.tablestore.model.PutRowRequest
import com.alicloud.openservices.tablestore.model.RowPutChange
import java.lang.RuntimeException
import java.security.MessageDigest
import java.util.*

/* 前缀4字符的数据区,最多存在8小时
*  会话主存储区 SESS4|UID32|Token16
*/
class SessionRepo : Repository("session", 3600*24) {
    private val _rand:Random = Random()

    fun createSession(uid:String, provider:String, userAgent:String):String{
        // 随机产生1个16字符的token
        val token = genToken()

        val pk = PriKeyStr("SESS${uid}${token}")
        var put = RowPutChange(_tableName, pk)

        put.addColumn("provider", ColumnValue.fromString(provider))
        put.addColumn("user-agent", ColumnValue.fromString(userAgent))

        _ots.putRow(PutRowRequest(put))

        return token
    }

    fun findSession(uidToken: String): String?{
        val pk = PriKeyStr("SESS${uidToken}")
        val row = query(pk)

        if(row === null) return null

        return uidToken.substring(0, 32)
    }

    // 生成Token的算法
    private fun genToken():String{
        val once = _rand.nextInt()

        var md5 = MessageDigest.getInstance("MD5")
        val t = "${once}"
        val md5v = md5.digest(t.toByteArray())

        var b64 = Base64.getEncoder()
        val uid64 = String(b64.encode(md5v))
        if(uid64.length != 24)
            throw RuntimeException("期望有24字符")

        return uid64.substring(0, 16)
    }
}