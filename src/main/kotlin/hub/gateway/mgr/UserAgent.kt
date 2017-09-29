package hub.gateway.mgr

import com.alicloud.openservices.tablestore.model.Row
import hub.gateway.x3rd.*

enum class Gender{ Male, Female }

class UserAgent:Agent{
    val id: String
    var nick: String
    var headUrl: String? = null
    var gender: Gender? = null
    var birthYear: Int? = null
    var city: String? = null
    var province: String? = null

    private var qqTicket: QQTicket? = null

    constructor(uid:String, src:Row):super(){
        id = uid
        nick = src.getLatestColumn("nick").value.asString()
        headUrl = src.getLatestColumn("head40")?.value?.asString()

        val strGender = src.getLatestColumn("gender").value.asString()
        if(strGender !== null && strGender.equals("女")) gender = Gender.Female
        else gender = Gender.Male

        birthYear = src.getLatestColumn("year").value?.asLong()?.toInt()
        city = src.getLatestColumn("city")?.value?.asString()
        province = src.getLatestColumn("province")?.value?.asString()
    }

    fun toUser():User{
        return User(this)
    }
}