package hub.gateway.mgr

class Org(val id:Int): Agent() {
    lateinit var name:String
    var ts:Long = 0L
    lateinit var ownerUserId:String
}

