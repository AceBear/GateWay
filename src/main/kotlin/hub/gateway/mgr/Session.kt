package hub.gateway.mgr

class Session:Agent {
    val uid:String
    val token:String
    val key:String
        get() = "$uid$token"

    constructor(uid:String, token:String):super(){
        this.uid = uid
        this.token = token
    }

    constructor(uidToken: String):super(){
        require(uidToken.length > 32){ "预期uid+token不少于32字符" }

        this.uid = uidToken.substring(0, 32)
        this.token = uidToken.substring(32)
    }
}