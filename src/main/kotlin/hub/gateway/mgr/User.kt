package hub.gateway.mgr

import com.alicloud.openservices.tablestore.model.Row

open class User() {
    lateinit var Id: String
    lateinit var Nick: String
    var headUrl: String? = null
    var gender: Gender? = null
    var birthYear: Int? = null
    var city: String? = null
    var province: String? = null

    constructor(id:String, row: Row):this(){
        this.Id = id
        this.Nick = row.getLatestColumn("nick").value.asString()
        this.headUrl = row.getLatestColumn("head40").value.asString()
        val gender = row.getLatestColumn("gender").value.asString()
        if(gender == "男") this.gender = Gender.Male
        else if(gender == "女") this.gender = Gender.Female
        this.province = row.getLatestColumn("province").value.asString()
        this.city = row.getLatestColumn("city").value.asString()
        this.birthYear = row.getLatestColumn("year").value.asLong().toInt()
    }
}

class UserLogin(user: User) : User() {
    lateinit var token: String

    init {
        this.Id = user.Id
        this.Nick = user.Nick
        this.headUrl = user.headUrl
        this.gender = user.gender
        this.birthYear = user.birthYear
        this.city = user.city
        this.province = user.province
    }

}

enum class Gender{ Male, Female }