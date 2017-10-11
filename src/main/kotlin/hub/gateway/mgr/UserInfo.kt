package hub.gateway.mgr

open class UserInfo(src: User) {
    val id = src.id
    val nick = src.nick
    val headUrl = src.headUrl
    val gender = src.gender
    val birthYear = src.birthYear
    val city = src.city
    val province = src.province
}

class UserInfoLogin(src: User, val token:String) : UserInfo(src)