package hub.gateway.mgr

open class User(src:UserAgent) {
    val id = src.id
    val nick = src.nick
    val headUrl = src.headUrl
    val gender = src.gender
    val birthYear = src.birthYear
    val city = src.city
    val province = src.province
}

class UserLogin(src:UserAgent, val token:String) : User(src)