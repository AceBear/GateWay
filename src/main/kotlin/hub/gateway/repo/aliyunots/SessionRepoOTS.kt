package hub.gateway.repo.aliyunots

import com.alicloud.openservices.tablestore.model.*
import hub.gateway.mgr.*
import hub.gateway.repo.ISessionRepo
import java.security.MessageDigest
import java.util.*

/**
 *  前缀4字符的数据区,最多存在24小时
 *  会话主存储区 SESS4|UID32|Token16
 */
class SessionRepoOTS : RepoOTS("session", "k", 3600*24), ISessionRepo {
    // 随机生成器
    private val _rand:Random = Random()

    override fun createSession(uid:String, provider:String, userAgent:String): SessionAgent{
        // 随机产生1个16字符的token
        val token = genToken()

        val pk = PriKeyStr("SESS$uid$token")
        var put = RowPutChange(_tableName, pk)

        put.addColumn("provider", ColumnValue.fromString(provider))
        put.addColumn("user-mgr", ColumnValue.fromString(userAgent))

        _ots.putRow(PutRowRequest(put))

        return SessionAgent(uid, token)
    }

    override fun findSession(uid:String, token:String): SessionAgent?{
        require(uid.length == 32){ "预期uid有32个字符" }
        require(token.length == 16){ "预期token有16个字符" }

        val pk = PriKeyStr("SESS$uid$token")
        val row = query(pk)

        if(row === null) return null

        return SessionAgent(uid, token)
    }

    override fun deleteSession(uid:String, token:String){
        require(uid.length == 32){ "预期uid有32个字符" }
        require(token.length == 16){ "预期token有16个字符" }

        val pk = PriKeyStr("SESS$uid$token")
        delete(pk)
    }

    // 生成Token的算法
    private fun genToken():String{
        val once = _rand.nextInt()

        var md5 = MessageDigest.getInstance("MD5")
        val md5v = md5.digest("$once".toByteArray())

        var b64 = Base64.getEncoder()
        val uid64 = String(b64.encode(md5v))

        check(uid64.length == 24){ "预期uid64有24个字符" }

        return uid64.substring(0, 16)
    }
}